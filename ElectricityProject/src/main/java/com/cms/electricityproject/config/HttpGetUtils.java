package com.cms.electricityproject.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 封装简单 HTTP GET 请求及响应读取。
 */
public class HttpGetUtils {

    public static String sendGetRequest(String urlStr, Map<String, String> params) throws IOException {
        StringBuilder urlWithParams = new StringBuilder(urlStr);
        if (params != null &&!params.isEmpty()) {
            urlWithParams.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();
                urlWithParams.append(URLEncoder.encode(paramName, StandardCharsets.UTF_8))
                           .append("=")
                           .append(URLEncoder.encode(paramValue, StandardCharsets.UTF_8))
                           .append("&");
            }
            urlWithParams.setLength(urlWithParams.length() - 1); // 去掉最后一个多余的&
        }

        URL url = new URL(urlWithParams.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("HTTP GET request failed, response code: " + responseCode);
        }
    }
}