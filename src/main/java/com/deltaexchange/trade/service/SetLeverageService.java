package com.deltaexchange.trade.service;

import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.deltaexchange.trade.config.DeltaConfig;
import com.deltaexchange.trade.util.DeltaSignatureUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
public class SetLeverageService {
        private static final Logger consoleLogger = LogManager.getLogger("Console");
    private static final Logger errorLogger = LogManager.getLogger("Error");

    @Autowired
    private WebClientService webClientService;
    @Autowired
    private DeltaConfig config;
    @Autowired
    private DeltaSignatureUtil signRequest;
    private final ObjectMapper mapper = new ObjectMapper();

    public Mono<JsonNode> setOrderLeverage(int leverage) {
        try {
            String endpoint = "/products/"+config.getProductId()+"/orders/leverage";
            String query = "";

            long timestamp = Instant.now().getEpochSecond();
            
            StringBuilder prehash = new StringBuilder();
            prehash.append("POST").append(timestamp).append(endpoint).append("?").append(query);
            String signature = signRequest.hmacSHA256(prehash.toString(), config.getApiSecret());

            StringBuilder endpointWithParams = new StringBuilder();
            endpointWithParams.append(endpoint).append("?").append(query);

            JSONObject inputJson = new JSONObject();
            inputJson.put("leverage", leverage);

            WebClient client = webClientService.buildClient(config.getBaseUrl());

            return client.post()
                    .uri(endpointWithParams.toString())
                    .header("api-key", config.getApiKey())
                    .header("signature", signature)
                    .header("timestamp", String.valueOf(timestamp))
                    .header("Accept", "application/json")
                    .bodyValue(inputJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        consoleLogger.info("Response of setLeverage Service:::::{}", response);
                        try {
                            JsonNode json = mapper.readTree(response);
                            return json;
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to setLeverage Service response", e);
                        }
                    });
        } catch (Exception e) {
            errorLogger.error("Error occured in setLeverage Service:::", e);
        }
        return null;
    }
}
