package com.example.dianasoponar.pollutionmap.Utils;

import android.location.Address;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.dianasoponar.pollutionmap.Models.PollutionLevel;
import com.example.dianasoponar.pollutionmap.Models.RatingPoint;
import com.example.dianasoponar.pollutionmap.Models.SensorPoint;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Globals {
    public static Address currentAddress;

    //add Firebase Database stuff
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseSensors;
    public static DatabaseReference mDatabaseRatings;
    public static List<SensorPoint> mSensorPointsList;
    public static List<RatingPoint> mRatingPointList;
    public static PopupWindow chartSensorPopUp;
    public static List<SensorPoint> mSensorLocations;
    public static ListView mListView;


    public static void getRatings() {
        mDatabaseRatings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRatingPointList = new ArrayList<>();
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    RatingPoint ratingPoint = new RatingPoint();

                    ratingPoint.setId(childSnapshot.getKey());
                    ratingPoint.setCoordinates(new LatLng((double) childSnapshot.child("coordinates").child("latitude").getValue(),
                            (double) childSnapshot.child("coordinates").child("longitude").getValue()));
                    ratingPoint.setArea(childSnapshot.child("area").getValue().toString());
                    ratingPoint.setRating(Double.parseDouble(childSnapshot.child("rating").getValue().toString()));
                    ratingPoint.setDate(Integer.parseInt(childSnapshot.child("dateTime").child("day").getValue().toString()),
                            Integer.parseInt(childSnapshot.child("dateTime").child("month").getValue().toString()),
                            Integer.parseInt(childSnapshot.child("dateTime").child("year").getValue().toString()),
                            Integer.parseInt(childSnapshot.child("dateTime").child("hour").getValue().toString()),
                            Integer.parseInt(childSnapshot.child("dateTime").child("minute").getValue().toString()));

                    mRatingPointList.add(ratingPoint);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void getSensors() {
        mSensorPointsList = new ArrayList<>();
        mDatabaseSensors.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSensorPointsList = new ArrayList<>();
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    SensorPoint location = new SensorPoint();

                    location.setCoordinates(new LatLng((double) childSnapshot.child("coordinates").child("latitude").getValue(),
                            (double) childSnapshot.child("coordinates").child("longitude").getValue()));
                    location.setId(childSnapshot.getKey());
                    location.setArea(childSnapshot.child("area").getValue().toString());

                    List<PollutionLevel> pollutionLevels = new ArrayList();
                    for (DataSnapshot childPollutionLevels : childSnapshot.child("pollutionLevels").getChildren()) {
                        PollutionLevel level = new PollutionLevel();

                        level.setId(childPollutionLevels.getKey());
                        level.setLevel(Double.valueOf(childPollutionLevels.child("level").getValue().toString()));
                        level.setDate(Integer.parseInt(childPollutionLevels.child("dateTime").child("day").getValue().toString()),
                                Integer.parseInt(childPollutionLevels.child("dateTime").child("month").getValue().toString()),
                                Integer.parseInt(childPollutionLevels.child("dateTime").child("year").getValue().toString()),
                                Integer.parseInt(childPollutionLevels.child("dateTime").child("hour").getValue().toString()),
                                Integer.parseInt(childPollutionLevels.child("dateTime").child("minute").getValue().toString()));

                        pollutionLevels.add(level);
                    }

                    location.setPollutionLevels(pollutionLevels);

                    mSensorPointsList.add(location);
                }
            }
            //Log.d(TAG, "Value is: " + values);

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

