package com.example.dianasoponar.pollutionmap;

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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.dianasoponar.pollutionmap.Models.RatingPoint;
import com.example.dianasoponar.pollutionmap.Models.SensorPoint;
import com.example.dianasoponar.pollutionmap.Utils.BottomNavigationViewHelper;
import com.example.dianasoponar.pollutionmap.Utils.LocationListAdapter;
import com.example.dianasoponar.pollutionmap.Utils.OnGeocoderFinishedListener;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import static com.example.dianasoponar.pollutionmap.Utils.Globals.*;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeFragment";

    private FusedLocationProviderClient mFusedLocationClient;

    //Variables
    private List<SensorPoint> mSensorLocations;
    private static final int ACTIVITY_NUM = 0;

    //Widgets
    private static Context mContext;
    private ListView mListView;
    private RatingBar ratingBar;
    private Button submitRating;
    private TextView messageRating;
    private PopupWindow chartPopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mContext = HomeActivity.this;
        mRatingPointList = new ArrayList<>();
        mSensorPointsList = new ArrayList<>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        mListView = (ListView) findViewById(R.id.listView);
        mSensorLocations = new ArrayList<>();
        // Initialize RatingBar
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        submitRating = (Button) findViewById(R.id.submitRating);
        messageRating = (TextView) findViewById(R.id.messageRating);
        //setup widgets
        LocationListAdapter adapter = new LocationListAdapter(mContext,
                R.layout.layout_location, mSensorLocations);
        mListView.setAdapter(adapter);

        findViewById(R.id.activity_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chartPopUp != null && chartPopUp.isShowing()) {
                    chartPopUp.dismiss();
                }

                if(chartSensorPopUp != null && chartSensorPopUp.isShowing()) {
                    chartSensorPopUp.dismiss();
                }
            }
        });

        setupBottomNavigationView();
        initFirebase();
        getLocation();
    }

    public void getLocation() {
        final TextView areaTextView = (TextView) findViewById(R.id.areaTextView);

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
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            getAddress(location, new OnGeocoderFinishedListener() {
                                @Override
                                public void onFinished(final List<Address> results) {
                                    currentAddress = results.get(0);
                                    areaTextView.setText(currentAddress.getThoroughfare());

                                    areaTextView.setTextSize(30);
                                    areaTextView.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
                                    areaTextView.setTextColor(Color.BLACK);

                                    setSensorData();
                                }
                            });
                        }
                    }
                });
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

    public void setSensorData(){
        for (SensorPoint sensor : mSensorPointsList) {
            if (Math.abs(sensor.getCoordinates().latitude - currentAddress.getLatitude()) <= 0.008 && Math.abs(sensor.getCoordinates().longitude - currentAddress.getLongitude()) <= 0.008) {

                mSensorLocations.add(sensor);
                LocationListAdapter adapter = new LocationListAdapter(mContext,
                        R.layout.layout_location, mSensorLocations);
                mListView.setAdapter(adapter);
            }
        }
    }

    /**
     * Display rating by calling getRating() method.
     * @param view
     */
    public void rateMe(View view){
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date

        DatabaseReference newChildRef = mDatabaseRatings.push();

        String newPostKey = newChildRef.getKey();
        // Create the data we want to update
        Map newPost = new HashMap();
        newPost.put("area", currentAddress.getThoroughfare());

        Map newPostCoordinates = new HashMap();
        newPostCoordinates.put("latitude", currentAddress.getLatitude());
        newPostCoordinates.put("longitude", currentAddress.getLongitude());
        newPost.put("coordinates", newPostCoordinates);

        Map newPostDateTime = new HashMap();
        newPostDateTime.put("day", calendar.get(Calendar.DAY_OF_MONTH));
        newPostDateTime.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
        newPostDateTime.put("minute", calendar.get(Calendar.MINUTE));
        newPostDateTime.put("month", calendar.get(Calendar.MONTH));
        newPostDateTime.put("year", calendar.get(Calendar.YEAR));
        newPost.put("dateTime", newPostDateTime);

        newPost.put("rating", (double)ratingBar.getRating());

        Map updatedUserData = new HashMap();
        updatedUserData.put(newPostKey, newPost);
        // Do a deep-path update
        mDatabaseRatings.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Error updating data: " + databaseError.getMessage());
                }
            }
        });

        ratingBar.setVisibility(View.INVISIBLE);
        submitRating.setVisibility(View.INVISIBLE);
        messageRating.setVisibility(View.VISIBLE);
        messageRating.setText("Your rating is: "+ratingBar.getRating());

        //Toast.makeText(getApplicationContext(), String.valueOf(ratingBar.getRating()), Toast.LENGTH_LONG).show();
    }

    public void showAnalysis(View view){

        View popupContent = getLayoutInflater().inflate(R.layout.layout_chart, null);
        chartPopUp = new PopupWindow();

        //popup should wrap content view
        chartPopUp.setWindowLayoutMode(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        chartPopUp.setHeight(250);
        chartPopUp.setWidth(350);

        //set content and background
        chartPopUp.setContentView(popupContent);
        chartPopUp.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_layout));

        chartPopUp.showAtLocation(popupContent, Gravity.CENTER,0,0);

        // in this example, a LineChart is initialized from xml
        BarChart chart = (BarChart) popupContent.findViewById(R.id.chart);

        List<BarEntry> entries = new ArrayList<BarEntry>();
        ArrayList<String> labels = new ArrayList<String>();
        int index=-1;
        mRatingPointList.sort(Comparator.comparing(RatingPoint::getRating).reversed());
        for (RatingPoint obj : mRatingPointList) {
            if (index<6) {
                index++;
                entries.add(new BarEntry(index, obj.getRating().floatValue()));
                labels.add(obj.getArea());
            }
        }

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        chart.getXAxis().setLabelRotationAngle(-90);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setTextSize(5f);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);

        chart.setExtraBottomOffset(-70f);

        BarDataSet dataset = new BarDataSet(entries, "Legend Text here");

        dataset.setDrawValues(false);

        BarData data = new BarData(dataset);
        chart.setData(data);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        chart.animateY(1000);
    }

    public void initFirebase(){
        //enable disk persistence
        if (FirebaseApp.getApps(HomeActivity.this).isEmpty()) {
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

    public static void getAddress(final Location location, final OnGeocoderFinishedListener listener) {
        new AsyncTask<Void, Integer, List<Address>>() {
            @Override
            protected List<Address> doInBackground(Void... arg0) {
                Geocoder coder = new Geocoder(mContext, Locale.ENGLISH);
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
}
