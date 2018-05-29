package com.example.dianasoponar.pollutionmap.Models;

public class LocationPoint {
    private String area;
    private Coordinates coordinates;
    private String date;
    private Integer pollutionLevel;

    public LocationPoint(String area, Coordinates coordinates, String date, Integer pollutionLevel){
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

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String getDate() {
        return date;
    }

    public String getTime(){
        return "";
    }

    public Integer getPollutionLevel() {
        return pollutionLevel;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPollutionLevel(Integer pollutionLevel) {
        this.pollutionLevel = pollutionLevel;
    }
}
