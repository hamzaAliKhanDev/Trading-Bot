package com.deltaexchange.trade.service;

import com.deltaexchange.trade.config.DeltaConfig;
import com.deltaexchange.trade.util.DeltaSignatureUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PositionService {

    private static final Logger consoleLogger = LogManager.getLogger("Console");
    private static final Logger errorLogger = LogManager.getLogger("Error");

    @Autowired private WebClientService webClientService;
    @Autowired private DeltaConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    public Mono<JsonNode> getBTCPositionDetails() {
        String path = "/v2/positions/margined";
        long timestamp = Instant.now().toEpochMilli();

        Map<String, Object> params = new TreeMap<>();
        params.put("timestamp", timestamp);

        String signature = DeltaSignatureUtil.signRequest(params, config.getApiSecret());
        String query = "timestamp=" + timestamp + "&signature=" + signature;

        WebClient client = webClientService.buildClient(config.getBaseUrl(), config.getApiKey());

        return client.get()
                .uri(path + "?" + query)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    consoleLogger.info("Response of PositionDetailsService:::::{}", response);
                    try {
                        JsonNode result = mapper.readTree(response).get("result");
                        for (JsonNode pos : result) {
                            if (config.getSymbol().equalsIgnoreCase(pos.get("symbol").asText())) {
                                double size = pos.get("size").asDouble();
                                double avgPrice = pos.get("entry_price").asDouble();
                                String side = pos.get("side").asText();

                                consoleLogger.info(
                                        "Size: {} | Avg Price: {} | Side: {}",
                                        size,
                                        avgPrice,
                                        side.equalsIgnoreCase("buy") ? "LONG" : "SHORT"
                                );
                            }
                        }
                        return result;
                    } catch (Exception e) {
                        errorLogger.error("Error occurred in PositionDetailsService:::::", e);
                        throw new RuntimeException("Failed to parse position details", e);
                    }
                });
    }
}
