package com.example.dianasoponar.pollutionmap;

import android.location.Address;

import java.util.List;

public abstract class OnGeocoderFinishedListener {
    public abstract void onFinished(List<Address> results);
}