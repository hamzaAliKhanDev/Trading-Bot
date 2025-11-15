package com.deltaexchange.trade.service;

import com.deltaexchange.trade.config.DeltaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceService {
	
	private static final Logger consoleLogger = LogManager.getLogger("Console");
	private static final Logger errorLogger = LogManager.getLogger("Error");
	
	 @Autowired private WebClientService webClientService;
	 @Autowired private DeltaConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    public Mono<Double> getLatestPrice(String symbol) {
    String url = "/v2/tickers/" + symbol;
    String fullUrl = config.getBaseUrl() + url;
    consoleLogger.info("Calling Delta API with URL:::::{}", fullUrl);

    return webClientService
            .buildClient(config.getBaseUrl())
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(response -> consoleLogger.info("Response of PriceService:::::{}", response))
            .map(response -> {
                try {
                    JsonNode json = mapper.readTree(response).get("result");
                    return json.get("spot_price").asDouble();
                } catch (Exception e) {
                    errorLogger.error("Error occurred in PriceService:::::", e);
                    throw new RuntimeException("Failed to parse latest price", e);
                }
            });
}
}
