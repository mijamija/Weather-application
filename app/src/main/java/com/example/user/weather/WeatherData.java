package com.example.user.weather;

import java.io.Serializable;
import java.util.Date;

public class WeatherData implements Serializable {

    private double dewPoint;
    private double humidity;
    private double temperature;

    private double fog;
    private double lowClouds;
    private double mediumClouds;
    private double highClouds;

    private Date dateAndTime;

    private Geolocation coordinates;


    public WeatherData() {
    }

    public WeatherData(double dewPoint, double humidity, double temperature, double fog, double lowClouds, double mediumClouds, double highClouds) {
        this.dewPoint = dewPoint;
        this.humidity = humidity;
        this.temperature = temperature;
        this.fog = fog;
        this.lowClouds = lowClouds;
        this.mediumClouds = mediumClouds;
        this.highClouds = highClouds;
    }

    public double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getFog() {
        return fog;
    }

    public void setFog(double fog) {
        this.fog = fog;
    }

    public double getLowClouds() {
        return lowClouds;
    }

    public void setLowClouds(double lowClouds) {
        this.lowClouds = lowClouds;
    }

    public double getMediumClouds() {
        return mediumClouds;
    }

    public void setMediumClouds(double mediumClouds) {
        this.mediumClouds = mediumClouds;
    }

    public double getHighClouds() {
        return highClouds;
    }

    public void setHighClouds(double highClouds) {
        this.highClouds = highClouds;
    }

    public Date getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(Date dateAndTime) {
        this.dateAndTime = dateAndTime;
    }


    public Geolocation getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Geolocation coordinates) {
        this.coordinates = coordinates;
    }
}
