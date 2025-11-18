package com.deltaexchange.trade.service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;

public class DeltaCancelOrdersForProduct {

   private static final String API_KEY = "QkcozvN7XM6uzcRC5lREH6rdDXQI4i";
    private static final String API_SECRET = "IeWvF36I8wABBseGQwKZ9OoR3THYbDt78jfeCU8aj78iuXSCBbeOYHGtDaC5";

    private static final String BASE_URL = "https://api.india.delta.exchange";

    private static final int PRODUCT_ID = 27;

    public static void main(String[] args) {
        DeltaCancelOrdersForProduct client = new DeltaCancelOrdersForProduct();
        client.cancelAllOrdersForProduct();
    }

    public void cancelAllOrdersForProduct() {
        try {
            // Step 1 → Fetch all open orders for this product
            JSONArray openOrders = getOpenOrdersForProduct(PRODUCT_ID);

            if (openOrders.length() == 0) {
                System.out.println("No open orders found for product_id = " + PRODUCT_ID);
                return;
            }

            System.out.println("Found " + openOrders.length() + " orders. Cancelling...");

            // Step 2 → Cancel one by one
            for (int i = 0; i < openOrders.length(); i++) {
                JSONObject ord = openOrders.getJSONObject(i);
                int orderId = ord.getInt("id");

                System.out.println("\nCancelling Order ID: " + orderId);
                cancelSingleOrder(orderId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------
    // GET OPEN ORDERS FOR product_id = 27
    // -------------------------------------------------------
    private JSONArray getOpenOrdersForProduct(int productId) {
        JSONArray output = new JSONArray();

        try {
            String endpoint = "/v2/orders";
            String query = "state=open&product_id=" + productId;

            long ts = Instant.now().getEpochSecond();

            String prehash = "GET" + ts + endpoint + "?" + query;

            String signature = hmacSHA256(prehash, API_SECRET);

            String url = BASE_URL + endpoint + "?" + query;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("api-key", API_KEY)
                    .header("signature", signature)
                    .header("timestamp", String.valueOf(ts))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            System.out.println("GET Status: " + res.statusCode());
            System.out.println("GET Response: " + res.body());

            if (res.statusCode() == 200) {
                JSONObject json = new JSONObject(res.body());
                output = json.getJSONArray("result");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    // -------------------------------------------------------
    // CANCEL SINGLE ORDER BY order_id
    // -------------------------------------------------------
    private void cancelSingleOrder(int orderId) {
        try {
            String endpoint = "/v2/orders";
            JSONObject body = new JSONObject();
            body.put("id", orderId);
            body.put("product_id", 27);

            long ts = Instant.now().getEpochSecond();
            String prehash = "DELETE" + ts + endpoint + body;
            String signature = hmacSHA256(prehash, API_SECRET);

            String url = BASE_URL + endpoint;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("api-key", API_KEY)
                    .header("signature", signature)
                    .header("timestamp", String.valueOf(ts))
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            System.out.println("DELETE Status: " + res.statusCode());
            System.out.println("DELETE Response: " + res.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------
    // SIGNATURE
    // -------------------------------------------------------
    private String hmacSHA256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

