package com.example.myproject;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


public class MainActivity extends AppCompatActivity {
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    String address;
    String x, y, date, time, weather;
    String sky, rain_probability, temperature, wind, snow, humidity_percentage; // 날씨 저장 변수 맑음 0% 30℃ 2.8m/s 적설없음 30%
    private boolean isFirstColor = false;
    private EditText edtTxt_Memo;
    private Button btn_SaveMemo;
    private Button btn_ViewMemo;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* 위치 권한 확인 */
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        /* 시간에 따라 라이트/다크 모드 설정하기 */
        int currentHour = getCurrentHour();
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main);
        if (currentHour >= 7 && currentHour < 19) // 아침 7시~저녁 6시 59분(라이트 모드)
            mainLayout.setBackgroundColor(Color.parseColor("#F2F8D4"));
        else                                    // (다크 모드)
            mainLayout.setBackgroundColor(Color.parseColor("#777775"));

        /* 메모 저장하기 */
        edtTxt_Memo = findViewById(R.id.EdtTxt_memo);
        btn_SaveMemo = findViewById(R.id.Btn_memo);
        btn_ViewMemo = findViewById(R.id.Btn_ViewMemo);
        calendar = Calendar.getInstance();

        btn_SaveMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        btn_ViewMemo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showDatePickerForView();
            }
        });

        /* GPS로 위치 읽어오기 */
        gpsTracker = new GpsTracker(MainActivity.this);
        TextView TxtVw_address = (TextView) findViewById(R.id.TxtVw_address);
        double latitude = gpsTracker.getLatitude(); // 위도
        double longitude = gpsTracker.getLongitude(); // 경도

        address = getCurrentAddress(latitude, longitude);
        Log.d("MainActivity", "Latitude: " + latitude + ", Longitude: " + longitude);

        Log.d("주소", address);
        String[] local = address.split(" ");
        TxtVw_address.setText(local[1] + " " + local[2] + local[3]); // oo시 oo구/oo도 oo시...등 출력
        Toast.makeText(MainActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

        /* 날씨 정보 불러오기 */
        String localName = local[2];
        Log.d("local[2]", localName);
        readExcel(localName);   // 엑셀에서 x, y(격자값) 저장
        Thread weatherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                /* 날씨 데이터 읽기 */
                fetchWeatherData(x, y); // 해당 격자값으로 날씨 데이터 읽기
                String[] weather_status = weather.split(" ");
                sky = weather_status[0];
                rain_probability = weather_status[1].replace("%", "");
                temperature = weather_status[2].replace("º", "");
                wind = weather_status[3].replace("m/s", "");
                snow = weather_status[4];
                humidity_percentage = weather_status[5].replace("%", "");

                // 메인 스레드에서 UI 업데이트를 위해 runOnUiThread 사용
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView TxtVw_Temp = (TextView) findViewById(R.id.TxtVw_Temp); // 온도
                        TextView TxtVw_Temp_stat = (TextView) findViewById(R.id.TxtVw_Temp_stat); // 기온_상태
                        ImageView ImgVw_sky = (ImageView) findViewById(R.id.ImgVw_sky); // 하늘 상태(이미지)
                        TextView TxtVw_sky = (TextView) findViewById(R.id.TxtVw_sky); // 하늘 상태(텍스트)
                        ImageView ImgVw_wind = (ImageView) findViewById(R.id.ImgVw_wind); // 풍속(이미지)
                        TextView TxtVw_rain = (TextView) findViewById(R.id.TxtVw_rain); // 비 올 확률
                        TextView TxtVw_hum = (TextView) findViewById(R.id.TxtVw_hum); // 습도
                        TextView TxtVw_wind = (TextView) findViewById(R.id.TxtVw_wind); // 풍속(텍스트)
                        TextView TxtVw_clothes = (TextView) findViewById(R.id.TxtVw_clothes); // 옷차림(텍스트)
                        ImageView ImgVw_clothes = (ImageView) findViewById(R.id.ImgVw_clothes); // 옷차림(이미지)
                        TextView TxtVw_notice = (TextView) findViewById(R.id.TxtVw_rain_stat); // 공지 부분(텍스트)
                        ImageView ImgVw_notice = (ImageView) findViewById(R.id.ImgVw_rain_stat); // 공지 부분(이미지)

                        /* UI 날씨값 업데이트 */
                        TxtVw_Temp.setText(temperature + "º");
                        TxtVw_sky.setText(sky);
                        TxtVw_rain.setText(rain_probability + "%");
                        TxtVw_hum.setText(humidity_percentage + "%");
                        TxtVw_wind.setText(wind + "m/s");

                        /* temperature 값에 따른 기온 상태, 옷차림 추천 */
                        if (Integer.parseInt(temperature) <= -5) {
                            TxtVw_Temp_stat.setText("매우 추움");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_9);
                            TxtVw_clothes.setText("롱패딩 • 숏패딩 • 바라클라바 • 패딩 팬츠 • 핸드 워머 • 머플러 • 무스탕 • 레그 워머 • 귀마개 • 붙이는 핫팩");
                        } else if (Integer.parseInt(temperature) <= 0) {
                            TxtVw_Temp_stat.setText("매우 추움");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_9);
                            TxtVw_clothes.setText("롱패딩 • 숏패딩 • 바라클라바 • 패딩 팬츠 • 핸드 워머 • 머플러 • 무스탕 • 레그 워머 • 귀마개 • 붙이는 핫팩");
                        } else if (Integer.parseInt(temperature) <= 4) {
                            TxtVw_Temp_stat.setText("추움");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_8);
                            TxtVw_clothes.setText("숏패딩 • 기모 이너티 • 겨울 코트 • 머플러 • 무스탕 • 레그 워머 • 기모 팬츠 • 바라클라바 • 퍼 자켓");
                        } else if (Integer.parseInt(temperature) <= 8) {
                            TxtVw_Temp_stat.setText("추움");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_7);
                            TxtVw_clothes.setText("더블 코트 • 히트텍 • 레깅스 • 니트 • 무스탕 • 울 코트 • 기모 팬츠 • 두꺼운 자켓 • 퀄팅 패딩 자켓");
                        } else if (Integer.parseInt(temperature) <= 11) {
                            TxtVw_Temp_stat.setText("서늘함");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_6);
                            TxtVw_clothes.setText("플리스 자켓 • 트랜치 코트 • 두꺼운 후드집업 • 니트 • 환절기 코트 • 블루종 자켓 • 코튼 팬츠 • 스카프");
                        } else if (Integer.parseInt(temperature) <= 16) {
                            TxtVw_Temp_stat.setText("서늘함");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_5);
                            TxtVw_clothes.setText("바시티 자켓 • 니트 • 두꺼운 가디건 • 청바지 • 후드집업 • 라이더 자켓 • 패딩 베스트 • 기모 후드티 • 야상");
                        } else if (Integer.parseInt(temperature) <= 19) {
                            TxtVw_Temp_stat.setText("시원함");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_4);
                            TxtVw_clothes.setText("후드티 • 맨투맨 • 얇은 자켓 • 가디건 • 청바지 • 트레이닝 자켓 • 블레이저 • 청자켓 • 후드집업 • 트위드 자켓");
                        } else if (Integer.parseInt(temperature) <= 22) {
                            TxtVw_Temp_stat.setText("적당함");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_3);
                            TxtVw_clothes.setText("스웨트셔츠 • 나일론 자켓 • 얇은 가디건 • 청바지 • 가을용 슬랙스 • 롱 스커트 • 얇은 후드티 • 셔츠");
                        } else if (Integer.parseInt(temperature) <= 27) {
                            TxtVw_Temp_stat.setText("따뜻함");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_2);
                            TxtVw_clothes.setText("반팔 • 얇은 여름용 가디건 • 얇은 셔츠 • 반바지 • 통풍성 바지 • 면바지 • 원피스 • 미디 스커트 • 청치마");
                        } else if (Integer.parseInt(temperature) <= 30) {
                            TxtVw_Temp_stat.setText("더움");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_1);
                            TxtVw_clothes.setText("민소매 • 반팔 • 숏팬츠 • 스커트 • 샌들 • 냉장고 바지 • 버뮤다 팬츠 • 반팔 원피스 ");
                        } else {
                            TxtVw_Temp_stat.setText("매우 더움");
                            ImgVw_clothes.setImageResource(R.drawable.clothes_1);
                            TxtVw_clothes.setText("민소매 • 반팔 • 숏팬츠 • 스커트 • 샌들 • 냉장고 바지 • 버뮤다 팬츠 • 반팔 원피스 ");
                        }

                        // 하늘 상태에 따른 하늘 이미지 변경
                        if (sky.equals("맑음"))
                            ImgVw_sky.setImageResource(R.drawable.sky_1);
                        else if (sky.equals("구름많음"))
                            ImgVw_sky.setImageResource(R.drawable.sky_3);
                        else if (sky.equals("흐림"))
                            ImgVw_sky.setImageResource(R.drawable.sky_4);

                        // 풍속에 따른 풍속 이미지 변경
                        if (Double.parseDouble(wind) <= 5)
                            ImgVw_wind.setImageResource(R.drawable.wind_1); // 하
                        else if (Double.parseDouble(wind) <= 10)
                            ImgVw_wind.setImageResource(R.drawable.wind_2); // 중
                        else
                            ImgVw_wind.setImageResource(R.drawable.wind_3); // 강

                        TxtVw_Temp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(!isFirstColor) {
                                    if (Integer.parseInt(temperature) <= 0)
                                        TxtVw_Temp.setTextColor(Color.parseColor("#FFACD4D9"));
                                    else if (Integer.parseInt(temperature) <= 8)
                                        TxtVw_Temp.setTextColor(Color.parseColor("#FFA5D8DE"));
                                    else if (Integer.parseInt(temperature) <= 16)
                                        TxtVw_Temp.setTextColor(Color.parseColor("#FFA8E4D1"));
                                    else if (Integer.parseInt(temperature) <= 19)
                                        TxtVw_Temp.setTextColor(Color.parseColor("#FFC2DC99"));
                                    else if (Integer.parseInt(temperature) <= 22)
                                        TxtVw_Temp.setTextColor(Color.parseColor("#FFEDCA96"));
                                    else if (Integer.parseInt(temperature) <= 27)
                                        TxtVw_Temp.setTextColor(Color.parseColor("#FFCF9587"));
                                    else if (Integer.parseInt(temperature) <= 30)
                                        TxtVw_Temp.setTextColor(Color.parseColor("#FFE4683F"));
                                    else
                                        TxtVw_Temp.setTextColor(Color.parseColor("#FFED2A0C"));
                                    isFirstColor = !isFirstColor;
                                }
                                else {
                                    TxtVw_Temp.setTextColor(Color.parseColor("#787777"));
                                    isFirstColor = !isFirstColor;
                                }
                            }
                        });

                        /* 비 올 확률에 따른 텍스트, 이미지 변경 */
                        if (Integer.parseInt(rain_probability) <= 50) {
                            // 만약 습도에 이상이 있다면
                            if (Integer.parseInt(humidity_percentage) >= 70) {
                                TxtVw_notice.setText("현재 습도가 높은 편이에요.\n통풍이 잘되도록 해주세요.");
                                ImgVw_notice.setImageResource(R.drawable.hum_high);
                            } else if (Integer.parseInt(humidity_percentage) <= 40) {
                                TxtVw_notice.setText("현재 건조한 편이에요.\n물을 보충하여 컨디션을\n조절해요.");
                                ImgVw_notice.setImageResource(R.drawable.hum_low);
                            } else {
                                TxtVw_notice.setText("비가 올 것 같지 않아요.\n오늘 하루도 행복하세요.");
                                ImgVw_notice.setImageResource(R.drawable.rain_0);
                            }
                        } else if (Integer.parseInt(rain_probability) <= 70) {
                            TxtVw_notice.setText("비가 올지도 몰라요.\n우산 챙기는 거 어떠세요?");
                            ImgVw_notice.setImageResource(R.drawable.rain_1);
                        } else if (Integer.parseInt(rain_probability) <= 90) {
                            TxtVw_notice.setText("비가 올 것 같아요.\n우산을 챙기세요.");
                            ImgVw_notice.setImageResource(R.drawable.rain_2);
                        } else {
                            TxtVw_notice.setText("비가 올 거에요.\n우산 꼭! 챙기세요.");
                            ImgVw_notice.setImageResource(R.drawable.rain_3);
                        }
                    }
                });
            }
        });
        weatherThread.start();
    }


    //퍼미션 요청의 결과 리턴 메소드
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // 위치 권한을 가지고 있는지 체크하는 메소드
    void checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    // GPS를 주소로 변환하는 메소드
    public String getCurrentAddress( double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
            Log.d("LocationInfo", "Latitude: " + latitude + ", Longitude: " + longitude);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // 격자값, 현재 날자, 시간에 대한 날씨 정보를 반환하는 메소드
    public void fetchWeatherData(String x, String y) {
        WeatherData wd = new WeatherData();
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
        String getDate = simpleDateFormat1.format(mDate);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH");
        String getTime = simpleDateFormat2.format(mDate) + "00";
        date = getDate;
        time = getTime;
        weather = wd.lookUpWeather(date, time, x, y);
        Log.d("현재날씨",weather);
    }

    // 엑셀 파일 읽기 - x,y
    // 엑셀 파일 읽기 메소드
    public void readExcel(String localName) {
        Log.d("세부 주소: ", localName);
        try {
            InputStream is = getBaseContext().getResources().getAssets().open("location.xls");
            Workbook wb = Workbook.getWorkbook(is);

            if (wb != null) {
                Sheet sheet = wb.getSheet(0);
                if (sheet != null) {
                    int colTotal = sheet.getColumns();
                    int rowIndexStart = 1;
                    int rowTotal = sheet.getColumn(colTotal - 1).length;

                    for (int row = rowIndexStart; row < rowTotal; row++) {
                        String contents = sheet.getCell(3, row).getContents();
                        if (contents.contains(localName)) {
                            x = sheet.getCell(5, row).getContents();
                            y = sheet.getCell(6, row).getContents();
                            row = rowTotal;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }
        // x, y 변수에 값이 설정됨
        Log.i("격자값", "x = " + x + "  y = " + y);
    }
    
    // 현재 시간의 시간을 숫자 형태로 반환하는 메소드
    private int getCurrentHour() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    // DatePicker을 띄우는 메소드 - saveMemoToFile을 위한
    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                saveMemoToFile();
            }
        }, year, month, day);

        datePickerDialog.show();
    }
    // DatePicker을 띄우는 메소드 - showMemoFromFile을 위한
    private void showDatePickerForView() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                showMemoFromFile();
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    // 메모한 것을 파일처리하여 저장하는 메소드
    private void saveMemoToFile() {
        String memo = edtTxt_Memo.getText().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());

        String fileName = "Memo_" + date + ".txt";

        try (FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE)) {
            fos.write(memo.getBytes());
            Log.d("MainActivity", "Memo saved to " + fileName);
        } catch (IOException e) {
            Log.e("MainActivity", "Error saving memo", e);
        }
    }

    // 메모가 저장된 파일 열고 메모를 확인하는 메소드
    private void showMemoFromFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());

        String fileName = "Memo_" + date + ".txt";

        try (FileInputStream fis = openFileInput(fileName)) {
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            String memo = new String(buffer);

            // AlertDialog를 통해 메모를 보여줍니다.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("메모 보기");
            builder.setMessage(memo);
            builder.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showEditDialog(fileName, memo);
                }
            });
            builder.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(MainActivity.this);
                    confirmBuilder.setTitle("메모 삭제");
                    confirmBuilder.setMessage("정말로 이 메모를 삭제하시겠습니까?");
                    confirmBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMemoFile(fileName);
                        }
                    });
                    confirmBuilder.setNegativeButton("아니오", null);
                    confirmBuilder.show();
                }
            });
            builder.setNeutralButton("닫기", null);
            builder.show();

        } catch (IOException e) {
            Log.e("MainActivity", "Error reading memo", e);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("메모 보기");
            builder.setMessage("해당 날짜의 메모가 없습니다.");
            builder.setPositiveButton("확인", null);
            builder.show();
        }
    }

    // 메모를 수정하는 메소드
    private void showEditDialog(String fileName, String memo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("메모 수정");

        final EditText input = new EditText(this);
        input.setText(memo);
        builder.setView(input);

        builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedMemo = input.getText().toString();
                try (FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE)) {
                    fos.write(updatedMemo.getBytes());
                    Log.d("MainActivity", "Memo updated in " + fileName);
                } catch (IOException e) {
                    Log.e("MainActivity", "Error updating memo", e);
                }
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    // 메모를 삭제하는 메소드
    private void deleteMemoFile(String fileName) {
        boolean deleted = deleteFile(fileName);
        if (deleted) {
            Log.d("MainActivity", "Memo deleted: " + fileName);
        } else {
            Log.e("MainActivity", "Error deleting memo: " + fileName);
        }
    }
}