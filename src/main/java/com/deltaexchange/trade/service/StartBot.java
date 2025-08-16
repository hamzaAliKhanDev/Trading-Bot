package com.deltaexchange.trade.service;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deltaexchange.trade.config.DeltaConfig;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

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
		
		// double price = priceService.getLatestPrice(config.getSymbol());
		// consoleLogger.info("Current BTC price:::::{}",price);
        // positionService.getBTCPositionDetails();
        
        
       Flux.interval(Duration.ofSeconds(config.getLoopInterval()))
        .flatMap(tick -> priceService.getLatestPrice(config.getSymbol())) // returns Mono<Double>
        .doOnNext(price -> {
            consoleLogger.info("[BOT] Current BTC Price:::{}", price);

 //positionService.getBTCPositionDetails(); 
 // Example Strategy: Buy if price < $60,000 
 // if (price < 60000) 
 //{ 
	// System.out.println("[BOT] Price below threshold — would place BUY order here."); 
 // orderService.placeOrder("buy", config.getOrderSize(), null, "market"); 
 // } 
 //System.out.println("----------------------------------");
        })
        .doOnError(e -> errorLogger.error("[ERROR]:::::", e))
        .subscribe(); // Start consuming the stream (non-blocking)
        
	}

}
