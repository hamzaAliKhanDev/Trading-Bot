package com.deltaexchange.trade.service;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deltaexchange.trade.config.DeltaConfig;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class StartBot {

    @Autowired
    private PositionService positionService;

    @Autowired
    private DeltaConfig config;

    @Autowired
    private CheckAndOrderService orderService;

    @Autowired
    private GetOpenOrdersService getOpenOrdersService;

    private static final Logger consoleLogger = LogManager.getLogger("Console");
    private static final Logger errorLogger = LogManager.getLogger("Error");
    private static final Logger transactionLogger = LogManager.getLogger("Transaction");

    public void startBotMain() {

        consoleLogger.info("::::::::::::::::Bot Started:::::::::::::");

        Flux.interval(Duration.ofSeconds(config.getLoopInterval()))
                .flatMap(tick -> positionService.getBTCPositionDetails()
                        .doOnNext(position -> {

                            consoleLogger.info("[BOT] Position data received for tick {}: {}", tick, position);

                            if (position != null) {

                                JSONObject positionServiceResponse = new JSONObject(position.toString());

                                boolean apiSuccess = positionServiceResponse.getBoolean("success");

                                if (apiSuccess) {

                                    JSONObject result = positionServiceResponse.getJSONObject("result");

                                    if (result != null && !result.isEmpty()) {

                                        // ENTRY PRICE — must be final or effectively final
                                        final String[] entryPriceStr = { result.optString("entry_price", "") };

                                        if (entryPriceStr[0] == null || entryPriceStr[0].isEmpty()) {
                                            consoleLogger
                                                    .info("[BOT] No EntryPrice found. Going for next tick::::::::");
                                            entryPriceStr[0] = "";
                                        } else {

                                            final long[] entryPrice = { (long) Double.parseDouble(entryPriceStr[0]) };

                                            // SIZE — also used inside nested lambda
                                            final int[] size = { result.getInt("size") };

                                            long tpPrice = 0;
                                            long avgPrice = 0;
                                            String tpOrderType = null;
                                            String avgOrderType = null;
                                            int tpOrderLimit = 0;
                                            int avgOrderLimit = 0;

                                            if (size[0] == 2) {
                                                tpPrice = entryPrice[0] + 500;
                                                tpOrderType = "sell";
                                                tpOrderLimit = 4;

                                                avgPrice = entryPrice[0] - 750;
                                                avgOrderType = "buy";
                                                avgOrderLimit = 4;

                                            } else if (size[0] == -2) {
                                                tpPrice = entryPrice[0] - 500;
                                                tpOrderType = "buy";
                                                tpOrderLimit = 4;

                                                avgPrice = entryPrice[0] + 750;
                                                avgOrderType = "sell";
                                                avgOrderLimit = 4;

                                            } else if (size[0] == 6) {
                                                tpPrice = entryPrice[0] + 500;
                                                tpOrderType = "sell";
                                                tpOrderLimit = 8;

                                                avgPrice = entryPrice[0] - 750;
                                                avgOrderType = "buy";
                                                avgOrderLimit = 12;

                                            } else if (size[0] == -6) {
                                                tpPrice = entryPrice[0] - 500;
                                                tpOrderType = "buy";
                                                tpOrderLimit = 8;

                                                avgPrice = entryPrice[0] + 750;
                                                avgOrderType = "sell";
                                                avgOrderLimit = 12;

                                            } else if (size[0] == 18) {
                                                tpPrice = entryPrice[0] + 200;
                                                tpOrderType = "sell";
                                                tpOrderLimit = 18;

                                                avgPrice = entryPrice[0] - 750;
                                                avgOrderType = "buy";
                                                avgOrderLimit = 36;

                                            } else if (size[0] == -18) {
                                                tpPrice = entryPrice[0] - 200;
                                                tpOrderType = "buy";
                                                tpOrderLimit = 18;

                                                avgPrice = entryPrice[0] + 750;
                                                avgOrderType = "sell";
                                                avgOrderLimit = 36;
                                            }else if (size[0] == 54) {
                                                tpPrice = entryPrice[0] + 200;
                                                tpOrderType = "sell";
                                                tpOrderLimit = 54;

                                                avgPrice = entryPrice[0] - 750;
                                                avgOrderType = "buy";
                                                avgOrderLimit = 108;

                                            } else if (size[0] == -54) {
                                                tpPrice = entryPrice[0] - 200;
                                                tpOrderType = "buy";
                                                tpOrderLimit = 54;

                                                avgPrice = entryPrice[0] + 750;
                                                avgOrderType = "sell";
                                                avgOrderLimit = 108;
                                            }else if (size[0] == 162) {
                                                tpPrice = entryPrice[0] + 125;
                                                tpOrderType = "sell";
                                                tpOrderLimit = 162;

                                                avgPrice = entryPrice[0] - 750;
                                                avgOrderType = "buy";
                                                avgOrderLimit = 243;

                                            } else if (size[0] == -162) {
                                                tpPrice = entryPrice[0] - 125;
                                                tpOrderType = "buy";
                                                tpOrderLimit = 162;

                                                avgPrice = entryPrice[0] + 750;
                                                avgOrderType = "sell";
                                                avgOrderLimit = 243;
                                            }else if (size[0] == 405) {
                                                tpPrice = entryPrice[0] + 125;
                                                tpOrderType = "sell";
                                                tpOrderLimit = 405;

                                                avgPrice = entryPrice[0] - 750;
                                                avgOrderType = "buy";
                                                avgOrderLimit = 810;

                                            } else if (size[0] == -405) {
                                                tpPrice = entryPrice[0] - 125;
                                                tpOrderType = "buy";
                                                tpOrderLimit = 405;

                                                avgPrice = entryPrice[0] + 750;
                                                avgOrderType = "sell";
                                                avgOrderLimit = 810;
                                            }else if (size[0] == 1215) {
                                                tpPrice = entryPrice[0] + 100;
                                                tpOrderType = "sell";
                                                tpOrderLimit = 1215;

                                                avgPrice = entryPrice[0] - 750;
                                                avgOrderType = "buy";
                                                avgOrderLimit = 1822;

                                            } else if (size[0] == -1215) {
                                                tpPrice = entryPrice[0] - 100;
                                                tpOrderType = "buy";
                                                tpOrderLimit = 1215;

                                                avgPrice = entryPrice[0] + 750;
                                                avgOrderType = "sell";
                                                avgOrderLimit = 1822;
                                            }else if (size[0] == 3037) {
                                                tpPrice = entryPrice[0] + 100;
                                                tpOrderType = "sell";
                                                tpOrderLimit = 3037;

                                            } else if (size[0] == -3037) {
                                                tpPrice = entryPrice[0] - 100;
                                                tpOrderType = "buy";
                                                tpOrderLimit = 3037;
                                            }

                                            // WRAP TP/AVG PRICE etc. in final arrays if used inside lambda
                                            final long[] finalTpPrice = { tpPrice };
                                            final long[] finalAvgPrice = { avgPrice };
                                            final String[] finalTpOrderType = { tpOrderType };
                                            final String[] finalAvgOrderType = { avgOrderType };
                                            final int[] finalTpOrderLimit = { tpOrderLimit };
                                            final int[] finalAvgOrderLimit = { avgOrderLimit };

                                            if (finalTpPrice[0] != 0) {

                                                getOpenOrdersService.getOpenOrdersForBTC()
                                                        .subscribe(openOrderResult -> {

                                                            if (openOrderResult != null) {

                                                                JSONObject getOrdersServiceResponse = new JSONObject(
                                                                        openOrderResult.toString());

                                                                boolean apiSuccessOrder = getOrdersServiceResponse
                                                                        .getBoolean("success");

                                                                if (apiSuccessOrder) {

                                                                    JSONArray resultArr = getOrdersServiceResponse
                                                                            .getJSONArray("result");

                                                                    boolean isTpPlaced = false;
                                                                    boolean isAvgPlaced = false;

                                                                    if (resultArr != null && !resultArr.isEmpty()) {

                                                                        for (int i = 0; i < resultArr.length(); i++) {

                                                                            JSONObject json = resultArr
                                                                                    .getJSONObject(i);

                                                                            int orderSize = json.getInt("size");
                                                                            String side = json.getString("side");
                                                                            long limitPrice = (long) Double.parseDouble(json.getString("limit_price"));

                                                                            if (orderSize == finalTpOrderLimit[0]
                                                                                    && side.equalsIgnoreCase(
                                                                                            finalTpOrderType[0])
                                                                                    && limitPrice == finalTpPrice[0]) {

                                                                                isTpPlaced = true;

                                                                            } else if (orderSize == finalAvgOrderLimit[0]
                                                                                    && side.equalsIgnoreCase(
                                                                                            finalAvgOrderType[0])
                                                                                    && limitPrice == finalAvgPrice[0]) {

                                                                                isAvgPlaced = true;
                                                                            }
                                                                        }
                                                                        consoleLogger.info("Boolean Flags::::TP-->{}:::::Avg-->{}",isTpPlaced,isAvgPlaced);
                                                                        if (isTpPlaced && isAvgPlaced) {
                                                                            consoleLogger.info(
                                                                                    "[BOT] TP and Avg Orders already placed. Going for next tick::::::::::::::::");
                                                                        } else {

                                                                            transactionLogger.info(
                                                                                    "::::::::::::::::::::::::::::::::::New Order execution Started:::::::::::::::::::::::::::::::::::");
                                                                            transactionLogger.info(
                                                                                    "Details of Current Order:- \n EntryPrice->{}, \n Size->{}:::::",
                                                                                    entryPrice[0], size[0]);

                                                                            orderService.executionMain(entryPriceStr[0],
                                                                                    size[0]);

                                                                        }
                                                                    }else{
                                                                        consoleLogger.info("[BOT] No Open Order found. Placing new Orders::::::::::");
                                                                        transactionLogger.info(
                                                                                    "::::::::::::::::::::::::::::::::::New Order execution Started:::::::::::::::::::::::::::::::::::");
                                                                            transactionLogger.info(
                                                                                    "Details of Current Order:- \n EntryPrice->{}, \n Size->{}:::::",
                                                                                    entryPrice[0], size[0]);

                                                                            orderService.executionMain(entryPriceStr[0],
                                                                                    size[0]);
                                                                    }

                                                                } else {
                                                                    consoleLogger.info(
                                                                            "[BOT] Get Active Orders API Failed. Going for next tick:::::::::::");
                                                                }

                                                            } else {
                                                                consoleLogger.info(
                                                                        "[BOT] Empty or null response found for Get Active Orders API. Going for next tick::::::::::");
                                                            }

                                                        });

                                            } else {
                                                consoleLogger
                                                        .info("::::::::::::::Size of order is beyond 3037. Going for another tick:::::::::::");
                                            }

                                        }

                                    } else {
                                        consoleLogger.info(
                                                "[BOT] Result JSON found empty or null in position service response. Going for another tick:::::::::::");
                                    }

                                } else {
                                    consoleLogger.info(
                                            ":::::::::::Position Service API Failed with success flag as False::::::::::::");
                                }

                            } else {
                                consoleLogger
                                        .info(":::::::::::::No response returned from position service:::::::::");
                            }
                        }))
                .doOnError(e -> errorLogger.error("[ERROR]:::::", e))
                .subscribe();
    }
}
