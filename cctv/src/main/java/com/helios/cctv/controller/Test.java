package com.helios.cctv.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

public class Test{
    public static void main(String[] args) throws IOException {

        String api_key = "test";

        StringBuilder urlBuilder = new StringBuilder("https://openapi.its.go.kr:9443/cctvInfo"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode(api_key, "UTF-8")); /*공개키*/
        urlBuilder.append("&" + URLEncoder.encode("type","UTF-8") + "=" + URLEncoder.encode("all", "UTF-8")); /*도로유형*/
        urlBuilder.append("&" + URLEncoder.encode("cctvType","UTF-8") + "=" + URLEncoder.encode("2", "UTF-8")); /*CCTV유형*/
        urlBuilder.append("&" + URLEncoder.encode("minX","UTF-8") + "=" + URLEncoder.encode("126.8", "UTF-8")); /*최소경도영역*/
        urlBuilder.append("&" + URLEncoder.encode("maxX","UTF-8") + "=" + URLEncoder.encode("127.2", "UTF-8")); /*최대경도영역*/
        urlBuilder.append("&" + URLEncoder.encode("minY","UTF-8") + "=" + URLEncoder.encode("37.4", "UTF-8")); /*최소위도영역*/
        urlBuilder.append("&" + URLEncoder.encode("maxY","UTF-8") + "=" + URLEncoder.encode("37.7", "UTF-8")); /*최대위도영역*/
        urlBuilder.append("&" + URLEncoder.encode("getType","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*출력타입*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "text/xml;charset=UTF-8");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        // 예쁘게 출력
        try {
            JSONObject json = new JSONObject(sb.toString());
            System.out.println(json.toString(4)); // 들여쓰기 4칸
        } catch (Exception e) {
            System.out.println("응답을 JSON으로 파싱할 수 없습니다:");
            System.out.println(sb.toString());
        }
    }
}

