package com.example.myproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class GpsTracker implements LocationListener {
    private final Context mContext;
    private LocationManager locationManager;
    private Location location;
    private double latitude;
    private double longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

    public GpsTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // GPS 및 네트워크 제공자가 사용 불가능할 때 처리할 로직 추가
            } else {
                // 위치 권한 확인
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    // GPS 위치 업데이트 요청
                    if (isGPSEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d("GpsTracker", "GPS Latitude: " + latitude + ", Longitude: " + longitude);
                        }
                    }

                    // 네트워크 위치 업데이트 요청 (필요시 주석 처리)
                    if (isNetworkEnabled && location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d("GpsTracker", "Network Latitude: " + latitude + ", Longitude: " + longitude);
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return location;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d("GpsTracker", "Updated Latitude: " + latitude + ", Longitude: " + longitude);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Provider가 비활성화될 때 호출됨
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Provider가 활성화될 때 호출됨
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Provider의 상태가 변경될 때 호출됨
    }
}
