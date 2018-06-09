package com.example.dianasoponar.pollutionmap.Models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SensorPoint{
    private String id;
    private String area;
    private LatLng coordinates;
    private List<PollutionLevel> pollutionLevels;

    public SensorPoint(String area, LatLng coordinates, List<PollutionLevel> pollutionLevels){
        this.area=area;
        this.coordinates=coordinates;
        this.pollutionLevels=pollutionLevels;
    }

    public SensorPoint(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getArea() {
        return area;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }


    public List<PollutionLevel> getPollutionLevels() {
        return pollutionLevels;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public void setPollutionLevels(List<PollutionLevel> pollutionLevels) {
        this.pollutionLevels = pollutionLevels;
    }
}