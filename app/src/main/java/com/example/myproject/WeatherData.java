package com.example.myproject;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
public class WeatherData {
    // 하늘 상태, 기온, 풍속, 강수확률, 적설량, 습도
    private String sky, temperature, wind, rain, snow, humidity;

    public String lookUpWeather(final String date, final String time, final String nx, final String ny) {
        final String baseDate = date; // 2022xxxx 형식을 사용해야 함
        final String baseTime = timeChange(time); // 0500 형식을 사용해야 함
        final String type = "json";

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // API 엔드포인트와 서비스 키 설정
                    String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
                    String serviceKey = "인증키";

                    // URL 생성
                    StringBuilder urlBuilder = new StringBuilder(apiUrl);
                    urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
                    urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("14", "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));

                    // URL 객체 생성
                    URL url = new URL(urlBuilder.toString());
                    Log.i("URL", "url : " + url);
                    Log.d("x", nx);
                    Log.d("y", ny);
                    Log.d("date", date);
                    Log.d("time", time);


                    // HttpURLConnection 설정
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-type", "application/json");

                    // 응답 코드 확인 및 스트림 선택
                    BufferedReader rd;
                    if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }

                    // 응답 데이터 읽기
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    rd.close();
                    conn.disconnect();
                    String result = sb.toString();
                    Log.i("Weather Data", result);

                    // JSON 파싱
                    // response 키를 가지고 데이터를 파싱
                    JSONObject jsonObj_1 = new JSONObject(result);
                    String response = jsonObj_1.getString("response");

                    // response 로 부터 body 찾기
                    JSONObject jsonObj_2 = new JSONObject(response);
                    String body = jsonObj_2.getString("body");

                    // body 로 부터 items 찾기
                    JSONObject jsonObj_3 = new JSONObject(body);
                    String items = jsonObj_3.getString("items");
                    Log.i("ITEMS", items);

                    // items로 부터 itemlist 를 받기
                    JSONObject jsonObj_4 = new JSONObject(items);
                    JSONArray jsonArray = jsonObj_4.getJSONArray("item");

                    sky = "";
                    temperature = "";
                    wind = "";
                    rain = "";
                    snow = "";
                    humidity = "";

                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObj_4 = jsonArray.getJSONObject(i);
                        String fcstValue = jsonObj_4.getString("fcstValue");
                        String category = jsonObj_4.getString("category");

                        if (category.equals("SKY")) { // 현 하늘 상태 코드는 1, 2, 3만 제공
                            if (fcstValue.equals("1")) {
                                sky = "맑음 ";
                            } else if (fcstValue.equals("2")) {
                                sky = "구름조금 ";
                            } else if (fcstValue.equals("3")) {
                                sky = "구름많음 ";
                            } else if (fcstValue.equals("4")) {
                                sky = "흐림 ";
                            }
                        }

                        if (category.equals("TMP")) {
                            temperature = fcstValue + "º ";
                        }

                        if (category.equals("WSD")) {
                            wind = fcstValue + "m/s ";
                        }

                        if (category.equals("POP")) {
                            rain = fcstValue + "% ";
                        }
                        if (category.equals("SNO")) {
                            snow = fcstValue + " ";
                        }
                        if (category.equals("REH")) {
                            humidity = fcstValue + "%";
                        }
                    }
                    Log.d("Weather Data", "Sky: " + sky + "Rain: " + rain + "Temperature: " + temperature +
                            "Wind: " + wind + "Snow: " + snow + "Humidity: " + humidity);

                } catch (Exception e) {
                    Log.e("WeatherData", "Error", e);
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 최종 결과 반환
        return sky + rain + temperature + wind + snow + humidity;
    }

    private String timeChange(String time) {
        // 시간 데이터를 3시간 단위로 변환하는 로직
        switch (time) {
            case "0200":
            case "0300":
            case "0400":
                return "0200";
            case "0500":
            case "0600":
            case "0700":
                return "0500";
            case "0800":
            case "0900":
            case "1000":
                return "0800";
            case "1100":
            case "1200":
            case "1300":
                return "1100";
            case "1400":
            case "1500":
            case "1600":
                return "1400";
            case "1700":
            case "1800":
            case "1900":
                return "1700";
            case "2000":
            case "2100":
            case "2200":
                return "2000";
            case "2300":
            case "0000":
            case "0100":
                return "2300";
            default:
                return time;
        }
    }
}
