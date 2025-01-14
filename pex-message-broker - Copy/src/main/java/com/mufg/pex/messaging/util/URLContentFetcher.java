package com.mufg.pex.messaging.util;

import com.mufg.pex.messaging.domain.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class URLContentFetcher {

    public static Response fetchContentAndHeaders(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        Response response = new Response();

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Print response headers
            System.out.println("Response Headers:");
            for (Map.Entry<String, java.util.List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    System.out.println(header.getKey() + ": " + header.getValue());
                    response.addHeader(header.getKey(), header.getValue().get(0));
                }
            }

            // Get response content
            int responseCode = connection.getResponseCode();
            response.code(responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                // Print response content
                System.out.println("\nResponse Content:");
                System.out.println(content.toString());
                response.body(content.toString());

                return response.build();
            } else {
                System.out.println("Failed to fetch content. HTTP response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static void main(String[] args) {
        String url = "https://www.example.com"; // Replace with your target URL
        fetchContentAndHeaders(url);
    }
}
