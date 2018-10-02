package com.example.dianasoponar.pollutionmap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.dianasoponar.pollutionmap.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import static com.example.dianasoponar.pollutionmap.Utils.Globals.getRatings;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.getSensors;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.mDatabaseRatings;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.mDatabaseSensors;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.mFirebaseDatabase;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.mRatingPointList;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.mSensorLocations;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.mSensorPointsList;

public class MainActivity extends AppCompatActivity {
    private static final java.lang.String TAG = "MainActivity";

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;

    //widgets
    private BottomNavigationViewEx mBottomNavigationViewEx;
    private Boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRatingPointList = new ArrayList<>();
        mSensorPointsList = new ArrayList<>();
        mSensorLocations = new ArrayList<>();


        if (checkPermissions()){
            initFirebase();
            startActivity(new Intent(this, HomeActivity.class));
            //getLocation();
        }
        else{
            if (checkPermissions()){
                initFirebase();
                startActivity(new Intent(this, HomeActivity.class));
                //getLocation();
            }
        }

    }

    public void initFirebase(){
        //enable disk persistence
        if (FirebaseApp.getApps(MainActivity.this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseSensors = mFirebaseDatabase.getReference("sensors");
        mDatabaseRatings = mFirebaseDatabase.getReference("ratings");

        //The Firebase Realtime Database synchronizes and stores a local copy of the data for active listeners
        mDatabaseSensors.keepSynced(true);
        mDatabaseRatings.keepSynced(true);

        getRatings();
        getSensors();
    }


    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }
}
