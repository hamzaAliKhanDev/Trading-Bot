package com.deltaexchange.trade.service;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deltaexchange.trade.config.DeltaConfig;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StartBot {

    @Autowired
    PriceService priceService;

    @Autowired
    PositionService positionService;

    @Autowired
    DeltaConfig config;

    private static final Logger consoleLogger = LogManager.getLogger("Console");
    private static final Logger errorLogger = LogManager.getLogger("Error");

    public void startBotMain() {

        consoleLogger.info("::::::::::::::::Bot Started:::::::::::::");

        Flux.interval(Duration.ofSeconds(config.getLoopInterval()))
            .flatMap(tick -> 
                priceService.getLatestPrice(config.getSymbol())  // Mono<Double>
                    .flatMap(price -> {
                        consoleLogger.info("[BOT] Current BTC Price:::{}", price);

                        // Chain positionService AFTER price is fetched
                        return positionService.getBTCPositionDetails()
                                .doOnNext(position -> {
                                    consoleLogger.info("[BOT] Position data received for tick {}: {}", tick, position);
                                    
                                    // Example Strategy:
                                    // if (price < 60000) {
                                    //     consoleLogger.info("[BOT] Price below threshold — would place BUY order here.");
                                    //     orderService.placeOrder("buy", config.getOrderSize(), null, "market");
                                    // }
                                });
                    })
            )
            .doOnError(e -> errorLogger.error("[ERROR]:::::", e))
            .subscribe(); // Start consuming the stream
            }
}
