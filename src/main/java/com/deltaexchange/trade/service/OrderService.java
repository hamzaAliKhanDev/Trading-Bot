package com.deltaexchange.trade.service;

import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deltaexchange.trade.config.DeltaConfig;
import com.deltaexchange.trade.util.DeltaSignatureUtil;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
	private static final Logger consoleLogger = LogManager.getLogger("Console");
	private static final Logger errorLogger = LogManager.getLogger("Error");
	
    @Autowired private WebClientService webClientService;
    @Autowired private DeltaConfig config;

    public void placeOrder(String side, double size, Double limitPrice, String orderType) {
        String path = "/v2/orders";
        long timestamp = Instant.now().toEpochMilli();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("timestamp", timestamp);
        params.put("symbol", config.getSymbol());
        params.put("side", side);
        params.put("size", size);
        params.put("order_type", orderType);
        if (limitPrice != null) params.put("limit_price", limitPrice);

        String signature = DeltaSignatureUtil.signRequest(params, config.getApiSecret());
        params.put("signature", signature);

        String response = webClientService
                .buildClient(config.getBaseUrl(), config.getApiKey())
                .post()
                .uri(path)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        consoleLogger.info("Response of OrderService:::::{}",response);    }
}
