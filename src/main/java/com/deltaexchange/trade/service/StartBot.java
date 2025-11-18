package com.deltaexchange.trade.service;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deltaexchange.trade.config.DeltaConfig;
import com.deltaexchange.trade.config.DeltaDto;

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
    private DeltaDto globalVar;

    @Autowired
    private CheckAndOrderService orderService;

    private static final Logger consoleLogger = LogManager.getLogger("Console");
    private static final Logger errorLogger = LogManager.getLogger("Error");
    private static final Logger transactionLogger = LogManager.getLogger("Transaction");

    public void startBotMain() {

        consoleLogger.info("::::::::::::::::Bot Started:::::::::::::");

        Flux.interval(Duration.ofSeconds(config.getLoopInterval()))
                .flatMap(tick -> {

                    // If needed later:
                    // return priceService.getLatestPrice(config.getSymbol())
                    // .flatMap(price -> {

                    return positionService.getBTCPositionDetails()
                            .doOnNext(position -> {

                                consoleLogger.info("[BOT] Position data received for tick {}: {}", tick, position);

                                if (position != null) {

                                    JSONObject positionServiceResponse = new JSONObject(position.toString());

                                    boolean apiSuccess = positionServiceResponse.getBoolean("success");

                                    if (apiSuccess) {

                                        JSONObject result = positionServiceResponse.getJSONObject("result");

                                        if (result != null && !result.isEmpty()) {
                                            String entryPriceStr = result.getString("entry_price");
                                            if(entryPriceStr==null || entryPriceStr.isEmpty()){
                                                consoleLogger.info("[BOT] No EntryPrice found. Going for next tick::::::::");
                                                entryPriceStr="";
                                            }else{
                                            Double entryPrice = Double.valueOf(entryPriceStr);

                                            int size = result.getInt("size");
                                            consoleLogger.info("Current TP Price::::{}",globalVar.getTpPrice());
                                            consoleLogger.info("Current Avg Price::::{}",globalVar.getAvgPrice());
                                            if (globalVar.getTpPrice() == 0
                                                    || globalVar.getAvgPrice() == 0
                                                    || entryPrice.equals(globalVar.getTpPrice())
                                                    || entryPrice.equals(globalVar.getAvgPrice())) {

                                                /**
                                                 * 1. Cancel All Orders
                                                 * 2. Set leverage
                                                 * 3. Place the TP and Avg Order
                                                 */
                                                transactionLogger.info("::::::::::::::::::::::::::::::::::New Order execution Started:::::::::::::::::::::::::::::::::::");
                                                transactionLogger.info("Details of Current Order:- \n EntryPrice->{}, \n Size->{}, \n CurrentTpPrice->{}, \n CurrentAvgPrice->{}:::::",entryPrice,size,globalVar.getTpPrice(),globalVar.getAvgPrice());

                                                orderService.executionMain(entryPriceStr, size);
                                                transactionLogger.info("::::::::::::::::::::::::::::::::::New Order execution Ended:::::::::::::::::::::::::::::::::::");

                                            } else {
                                                consoleLogger.info(
                                                        "::::::::::::::No TP/Avg price met. Going for another tick:::::::::::");
                                            }

                                        }
                                        }else{
                                            consoleLogger.info("[BOT] Result JSON found empty or null in position service response. Going for another tick:::::::::::");
                                        }

                                    } else {
                                        consoleLogger.info(
                                                ":::::::::::Position Service API Failed with success flag as False::::::::::::");
                                    }

                                } else {
                                    consoleLogger.info(
                                            ":::::::::::::No response returned from position service:::::::::");
                                }
                            });
                    // });
                })
                .doOnError(e -> errorLogger.error("[ERROR]:::::", e))
                .subscribe(); // Start consuming the stream 
    }
}
