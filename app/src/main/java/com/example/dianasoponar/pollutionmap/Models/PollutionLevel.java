package com.example.dianasoponar.pollutionmap.Models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PollutionLevel {
    private String id;
    private Date date;
    private Double level;

    PollutionLevel(Date date, Double level){
        this.date = date;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PollutionLevel(){}

    public Date getDate() {
        return date;
    }

    public Double getLevel() {
        return level;
    }

    public void setDate(int day, int month, int year, int hour, int minute) {

        Calendar myCal = Calendar.getInstance();
        myCal.set(year, month-1, day, hour, minute);
        this.date = myCal.getTime();
    }

    public void setLevel(Double level) {
        this.level = level;
    }

    public String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }
}
