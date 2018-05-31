package com.example.dianasoponar.pollutionmap;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dianasoponar.pollutionmap.Models.Coordinates;
import com.example.dianasoponar.pollutionmap.Models.LocationPoint;
import com.example.dianasoponar.pollutionmap.Utils.BottomNavigationViewHelper;
import com.example.dianasoponar.pollutionmap.Utils.LocationListAdapter;
import com.example.dianasoponar.pollutionmap.Utils.OnGeocoderFinishedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeFragment";

    private FusedLocationProviderClient mFusedLocationClient;
    private static final int ACTIVITY_NUM = 0;

    //Variables
    private List<LocationPoint> mLocations;
    static private Address currentAddress;

    //Widgets
    private Context mContext;
    private ListView mListView;
    public RatingBar ratingBar;

    //Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mListView = (ListView)findViewById(R.id.listView);
        mLocations = new ArrayList<>();

        // Initialize RatingBar
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        mContext=HomeActivity.this;

        //enable disk persistence
        if (FirebaseApp.getApps(HomeActivity.this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = mFirebaseDatabase.getReference("locations");

        //The Firebase Realtime Database synchronizes and stores a local copy of the data for active listeners
        mDatabase.keepSynced(true);

        //setup widgets
        LocationListAdapter adapter = new LocationListAdapter(mContext,
                R.layout.layout_location, mLocations);
        mListView.setAdapter(adapter);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

        setupBottomNavigationView();


    }

    public void getLocation(){
        final TextView areaTextView = (TextView)findViewById(R.id.areaTextView);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //get first location
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            getCityName(location, new OnGeocoderFinishedListener() {
                                @Override
                                public void onFinished(final List<Address> results) {
                                    currentAddress = results.get(0);
                                    areaTextView.setText(currentAddress.getThoroughfare() + " "
                                            + currentAddress.getSubThoroughfare() + ", "
                                            + currentAddress.getLocality() + ", "
                                            + currentAddress.getCountryName());
                                    areaTextView.setTextSize(30);
                                    areaTextView.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                                    areaTextView.setTextColor(Color.BLACK);

                                    setupFirebase();
                                }
                            });
                        }
                    }
                });
    }

    public void getCityName(final Location location, final OnGeocoderFinishedListener listener) {
        new AsyncTask<Void, Integer, List<Address>>() {
            @Override
            protected List<Address> doInBackground(Void... arg0) {
                Geocoder coder = new Geocoder(HomeActivity.this, Locale.ENGLISH);
                List<Address> results = null;
                try {
                    results = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    // nothing
                }
                return results;
            }

            @Override
            protected void onPostExecute(List<Address> results) {
                if (results != null && listener != null) {
                    listener.onFinished(results);
                }
            }
        }.execute();
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(HomeActivity.this, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    public void setupFirebase(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    LocationPoint location = new LocationPoint();

                    location.setCoordinates(new Coordinates((double)childSnapshot.child("coordinates").child("latitude").getValue(), (double)childSnapshot.child("coordinates").child("longitude").getValue()));
                    if (Math.abs(location.getCoordinates().getLatitude()-currentAddress.getLatitude())<=0.008 && Math.abs(location.getCoordinates().getLongitude()-currentAddress.getLongitude())<=0.008)
                    {
                        location.setArea(childSnapshot.child("area").getValue().toString());
                        location.setPollutionLevel(Integer.valueOf(childSnapshot.child("pollutionLevel").getValue().toString()));
                        location.setDate(childSnapshot.child("dateTime").getValue().toString());

                        mLocations.add(location);
                        LocationListAdapter adapter = new LocationListAdapter(mContext,
                                R.layout.layout_location, mLocations);
                        mListView.setAdapter(adapter);
                    }
                }
                //Log.d(TAG, "Value is: " + values);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Display rating by calling getRating() method.
     * @param view
     */
    public void rateMe(View view){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("ratings");

        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date

        DatabaseReference newChildRef = myRef.push();

        newChildRef.child("area").setValue(currentAddress.getThoroughfare() + " "
                + currentAddress.getSubThoroughfare() + ", "
                + currentAddress.getLocality() + ", "
                + currentAddress.getCountryName());
        newChildRef.child("coordinates").child("latitude").setValue(currentAddress.getLatitude());
        newChildRef.child("coordinates").child("longitude").setValue(currentAddress.getLongitude());
        newChildRef.child("dateTime").child("day").setValue(calendar.get(Calendar.DAY_OF_MONTH));
        newChildRef.child("dateTime").child("hour").setValue(calendar.get(Calendar.HOUR_OF_DAY));
        newChildRef.child("dateTime").child("minute").setValue(calendar.get(Calendar.MINUTE));
        newChildRef.child("dateTime").child("month").setValue(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        newChildRef.child("dateTime").child("year").setValue(calendar.get(Calendar.YEAR));
        newChildRef.child("rating").setValue(ratingBar.getRating());

        //Toast.makeText(getApplicationContext(), String.valueOf(ratingBar.getRating()), Toast.LENGTH_LONG).show();
    }

}
