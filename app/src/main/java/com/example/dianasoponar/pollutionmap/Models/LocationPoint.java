package com.example.dianasoponar.pollutionmap.Models;

import com.google.android.gms.maps.model.LatLng;

public class LocationPoint {
    private String area;
    private LatLng coordinates;
    private String date;
    private Double pollutionLevel;

    public LocationPoint(String area, LatLng coordinates, String date, Double pollutionLevel){
        this.area=area;
        this.coordinates=coordinates;
        this.date=date;
        this.pollutionLevel=pollutionLevel;
    }

    public LocationPoint(){

    }

    public String getArea() {
        return area;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public String getDate() {
        return date;
    }

    public String getTime(){
        return "";
    }

    public Double getPollutionLevel() {
        return pollutionLevel;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPollutionLevel(Double pollutionLevel) {
        this.pollutionLevel = pollutionLevel;
    }
}
