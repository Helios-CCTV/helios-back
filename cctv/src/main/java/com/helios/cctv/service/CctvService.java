package com.helios.cctv.service;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class CctvService {

    @Value("${CCTV_API_KEY}")
    private String apiKey;

    public ApiResponse<String> getCctv(GetCctvRequest getCctvRequest) {
        StringBuilder sb = new StringBuilder();
        try {
            String minX = Float.toString(getCctvRequest.getMinX());
            String maxX = Float.toString(getCctvRequest.getMaxX());
            String minY = Float.toString(getCctvRequest.getMinY());
            String maxY = Float.toString(getCctvRequest.getMaxY());
            StringBuilder urlBuilder = new StringBuilder("https://openapi.its.go.kr:9443/cctvInfo");
            urlBuilder.append("?" + URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode(apiKey, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("cctvType", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("minX", "UTF-8") + "=" + URLEncoder.encode(minX, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("maxX", "UTF-8") + "=" + URLEncoder.encode(maxX, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("minY", "UTF-8") + "=" + URLEncoder.encode(minY, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("maxY", "UTF-8") + "=" + URLEncoder.encode(maxY, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("getType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "text/xml;charset=UTF-8");

            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();

            // JSON 예쁘게 포맷
            JSONObject json = new JSONObject(sb.toString());
            return ApiResponse.ok(json.toString(4),200);

        } catch (Exception e) {
            return ApiResponse.fail("Error fetching CCTV info: " + e.getMessage(),500);


        }
    }
}
