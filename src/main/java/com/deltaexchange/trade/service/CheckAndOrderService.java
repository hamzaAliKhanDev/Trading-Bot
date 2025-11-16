package com.deltaexchange.trade.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deltaexchange.trade.config.DeltaDto;

@Service
public class CheckAndOrderService {

    @Autowired
    private CancelOrderService cancelAllOrders;
    @Autowired
    private SetLeverageService setOrderLeverage;
    @Autowired
    private PlaceOrderService placeOrder;
    @Autowired
    private DeltaDto globalVars;

    private static final Logger consoleLogger = LogManager.getLogger("Console");
    private static final Logger errorLogger = LogManager.getLogger("Error");
    private static final Logger transactionLogger = LogManager.getLogger("Transaction");

    public JSONObject executionMain(String entryPrice, int size) {
        try {
            // Cancels All Orders
            cancelAllOrders.cancelExistingOrders().subscribe(cancelOrdersNode -> {
                consoleLogger.info("cancelOrdersNode:::::{}", cancelOrdersNode);

                JSONObject cancelOrdersResponse = new JSONObject(cancelOrdersNode.toString());
                boolean apiSuccess = cancelOrdersResponse.getBoolean("success");

                if (!apiSuccess) {
                    consoleLogger.info(":::::::::Cancel Order service returned success false::::::::::::");
                } else {
                    transactionLogger.info(
                            "Cancelled All Previous Orders for EntryPrice->{}, Size->{}:::::",
                            entryPrice,
                            size);
                }
            });

            // Set Leverage of Orders
            int leverage = returnLeverage(size);
            setOrderLeverage.setOrderLeverage(leverage).subscribe(setLeverageNode -> {
                JSONObject setLeverageResponse = new JSONObject(setLeverageNode.toString());
                boolean apiSuccess = setLeverageResponse.getBoolean("success");

                if (!apiSuccess) {
                    consoleLogger.info(":::::::::Set Leverage Service returned success false::::::::::::");
                } else {
                    transactionLogger.info(
                            "Leverage Set Successfully Orders for EntryPrice->{}, Size->{}, Leverage->{}:::::",
                            entryPrice,
                            size,
                            leverage);
                }
            });

            // Place Orders
            placeOrder(entryPrice, size);

        } catch (Exception e) {
            errorLogger.error("Error occured in Check and Order Service:::::", e);
        }

        return null;
    }

    public int returnLeverage(int size) {
        int leverage = 0;
        switch (size) {
            case 2:
                leverage = 10;
                break;
            case -2:
                leverage = 10;
                break;
            case 6:
                leverage = 10;
                break;
            case -6:
                leverage = 10;
                break;
            case 18:
                leverage = 25;
                break;
            case -18:
                leverage = 125;
                break;
            default:
                leverage = 10;
                break;
        }
        return leverage;
    }

    public void placeOrder(String entryPrice, int size) {

    double entryPriceDouble = Double.parseDouble(entryPrice);

    switch (size) {

        case 2:
            executeOrder(String.valueOf(entryPriceDouble + 500), 4, "sell");
            globalVars.setTpPrice(entryPriceDouble + 500);

            executeOrder(String.valueOf(entryPriceDouble - 750), 4, "buy");
            globalVars.setAvgPrice(entryPriceDouble - 750);
            break;

        case -2:
            executeOrder(String.valueOf(entryPriceDouble - 500), 4, "buy");
            globalVars.setTpPrice(entryPriceDouble - 500);

            executeOrder(String.valueOf(entryPriceDouble + 750), 4, "sell");
            globalVars.setAvgPrice(entryPriceDouble + 750);
            break;

        case 6:
            executeOrder(String.valueOf(entryPriceDouble + 500), 8, "sell");
            globalVars.setTpPrice(entryPriceDouble + 500);

            executeOrder(String.valueOf(entryPriceDouble - 750), 12, "buy");
            globalVars.setAvgPrice(entryPriceDouble - 750);
            break;

        case -6:
            executeOrder(String.valueOf(entryPriceDouble - 500), 8, "buy");
            globalVars.setTpPrice(entryPriceDouble - 500);

            executeOrder(String.valueOf(entryPriceDouble + 750), 12, "sell");
            globalVars.setAvgPrice(entryPriceDouble + 750);
            break;

        case 18:
            executeOrder(String.valueOf(entryPriceDouble + 200), 18, "sell");
            globalVars.setTpPrice(entryPriceDouble + 200);

            executeOrder(String.valueOf(entryPriceDouble - 750), 36, "buy");
            globalVars.setAvgPrice(entryPriceDouble - 750);
            break;

        case -18:
            executeOrder(String.valueOf(entryPriceDouble - 200), 18, "buy");
            globalVars.setTpPrice(entryPriceDouble - 200);

            executeOrder(String.valueOf(entryPriceDouble + 750), 36, "sell");
            globalVars.setAvgPrice(entryPriceDouble + 750);
            break;
    }
}

    public void executeOrder(String limitPrice, int size, String side) {

    placeOrder.placeOrder(limitPrice, size, side).subscribe(placeOrderNode -> {

        JSONObject placeOrderResponse = new JSONObject(placeOrderNode.toString());
        boolean apiSuccess = placeOrderResponse.getBoolean("success");

        if (!apiSuccess) {
            consoleLogger.info(
                    ":::::::::Place Order service returned false for LimitPrice->{}, Size->{}, Side->{}:::::::",
                    limitPrice, size, side
            );
        } else {
            transactionLogger.info(
                    "Order Placed Successfully with Details:- \n Side->{}, \n LimitPrice->{}, \n Size->{}:::::",
                    side, limitPrice, size
            );
        }

    }, error -> {
        errorLogger.error("Error placing order:", error);
    });
}


}
