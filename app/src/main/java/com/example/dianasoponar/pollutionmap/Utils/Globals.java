package com.example.dianasoponar.pollutionmap.Utils;

import android.location.Address;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Globals {
    public static Address currentAddress;

    //add Firebase Database stuff
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseLocations;
    public static DatabaseReference mDatabaseRatings;

}
