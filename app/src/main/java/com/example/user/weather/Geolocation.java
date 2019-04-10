package com.example.user.weather;

import android.support.annotation.NonNull;

public class Geolocation implements Comparable<Geolocation> {

    private double latitude;
    private double longitude;

    public Geolocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Geolocation() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    @Override
    public int compareTo(@NonNull Geolocation geolocation) {

        if(round(geolocation.getLatitude(), 2) == round(this.latitude, 2)
            && round(geolocation.getLongitude(), 2) == round(this.longitude, 2)){
            return 1;
        }else {
            return 0;
        }
    }

    public static double round(double value, int places) {

        if(places >= 0) {
            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        }

        return -1;
    }
}
