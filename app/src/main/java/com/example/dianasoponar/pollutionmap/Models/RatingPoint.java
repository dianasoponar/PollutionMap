package com.example.dianasoponar.pollutionmap.Models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RatingPoint{
    private String id;
    private String area;
    private LatLng coordinates;
    private Date date;
    private Double rating;

    public RatingPoint(String area, LatLng coordinates, Double rating){
        this.area=area;
        this.coordinates=coordinates;
        this.rating=rating;
    }

    public RatingPoint(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArea() {
        return area;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public Date getDate() {
        return date;
    }

    public String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }

    public Double getRating() {
        return rating;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public void setDate(int day, int month, int year, int hour, int minute) {

        Calendar myCal = Calendar.getInstance();
        myCal.set(year, month-1, day, hour, minute);
        this.date = myCal.getTime();
    }

    public void setRating(Double pollutionLevel) {
        this.rating = pollutionLevel;
    }
}