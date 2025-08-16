package com.deltaexchange.trade.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeSet;

public class DeltaSignatureUtil {
	
	private static final Logger consoleLogger = LogManager.getLogger("Console");
	private static final Logger errorLogger = LogManager.getLogger("Error");
	
    public static String signRequest(Map<String, Object> params, String secret) {
        try {
            StringBuilder payload = new StringBuilder();
            for (String key : new TreeSet<>(params.keySet())) {
                payload.append(key).append("=").append(params.get(key)).append("&");
            }
            payload.deleteCharAt(payload.length() - 1);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign request", e);
        }
    }
}

