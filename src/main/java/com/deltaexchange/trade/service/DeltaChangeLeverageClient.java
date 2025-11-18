package com.deltaexchange.trade.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DeltaChangeLeverageClient {

    // TODO: PUT YOUR KEYS HERE
     private static final String API_KEY = "QkcozvN7XM6uzcRC5lREH6rdDXQI4i";
    private static final String API_SECRET = "IeWvF36I8wABBseGQwKZ9OoR3THYbDt78jfeCU8aj78iuXSCBbeOYHGtDaC5";


    private static final String BASE_URL = "https://api.india.delta.exchange";

    public static void main(String[] args) {
        try {
            DeltaChangeLeverageClient client = new DeltaChangeLeverageClient();
            client.changeLeverage(27, 10);  // productId = 27, leverage = 10
        } catch (Exception e) {
            System.out.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void changeLeverage(int productId, int leverage) {
        try {
            String endpoint = "/v2/products/" + productId + "/orders/leverage";
            String body = "{\"leverage\":" + leverage + "}";

            long timestamp = Instant.now().getEpochSecond();

            // CREATE SIGNATURE STRING
            String prehash = "POST" + timestamp + endpoint + body;

            System.out.println("=== DEBUG SIGNING INFO ===");
            System.out.println("Timestamp    : " + timestamp);
            System.out.println("Prehash      : " + prehash);

            String signature = hmacSHA256(prehash, API_SECRET);
            System.out.println("Signature(hex): " + signature);
            System.out.println("=== END DEBUG ===");

            String url = BASE_URL + endpoint;

            HttpClient http = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("api-key", API_KEY)
                    .header("signature", signature)
                    .header("timestamp", String.valueOf(timestamp))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            System.out.println("Sending POST request to: " + url);
            System.out.println("Request Body: " + body);

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("HTTP Status : " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(response.body());

            if (response.statusCode() == 401) {
                System.out.println("ERROR 401: Unauthorized");
                System.out.println("Possible reasons:");
                System.out.println("1) Wrong signature");
                System.out.println("2) Wrong timestamp (must match Delta server time)");
                System.out.println("3) Wrong API key or secret");
                System.out.println("4) Your IP is not whitelisted in Delta Exchange India");
            }

        } catch (Exception e) {
            System.out.println("Exception calling changeLeverage(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String hmacSHA256(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKey);

            byte[] hashBytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            System.out.println("Error generating HMAC: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
