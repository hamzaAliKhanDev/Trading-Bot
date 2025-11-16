package com.deltaexchange.trade.service;

import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.deltaexchange.trade.config.DeltaConfig;
import com.deltaexchange.trade.util.DeltaSignatureUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
public class CancelOrderService {

    private static final Logger consoleLogger = LogManager.getLogger("Console");
    private static final Logger errorLogger = LogManager.getLogger("Error");
    

    @Autowired
    private WebClientService webClientService;
    @Autowired
    private DeltaConfig config;
    @Autowired
    private DeltaSignatureUtil signRequest;
    private final ObjectMapper mapper = new ObjectMapper();

    public Mono<JsonNode> cancelExistingOrders() {
        try {
            String endpoint = "/v2/orders/all";
            String query = "";

            long timestamp = Instant.now().getEpochSecond();
            
            StringBuilder prehash = new StringBuilder();
            prehash.append("DELETE").append(timestamp).append(endpoint).append("?").append(query);
            String signature = signRequest.hmacSHA256(prehash.toString(), config.getApiSecret());

            StringBuilder endpointWithParams = new StringBuilder();
            endpointWithParams.append(endpoint).append("?").append(query);
            WebClient client = webClientService.buildClient(config.getBaseUrl());
            consoleLogger.info("before canel all orders::");
            return client.delete()
                    .uri(endpointWithParams.toString())
                    .header("api-key", config.getApiKey())
                    .header("signature", signature)
                    .header("timestamp", String.valueOf(timestamp))
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        consoleLogger.info("Response of cancel existing orders:::::{}", response);
                        try {
                            JsonNode json = mapper.readTree(response);
                            return json;
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse cancel orders response", e);
                        }
                    });
        } catch (Exception e) {
            errorLogger.error("Error occured in cancel all orders:::", e);
        }
        return null;
    }

}
