package com.example.dianasoponar.pollutionmap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.dianasoponar.pollutionmap.Models.PollutionLevel;
import com.example.dianasoponar.pollutionmap.Models.RatingPoint;
import com.example.dianasoponar.pollutionmap.Models.SensorPoint;
import com.example.dianasoponar.pollutionmap.Utils.BottomNavigationViewHelper;
import com.example.dianasoponar.pollutionmap.Utils.OnGeocoderFinishedListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
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


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapActivity";
    private static final int ACTIVITY_NUM = 1;

    private GoogleMap mMap;
    private ArrayList<WeightedLatLng> heatMapGreenData;
    private ArrayList<WeightedLatLng> heatMapRedData;
    TileOverlay mOverlayGreen;
    TileOverlay mOverlayRed;
    private static Context mContext;
    private PopupWindow popup;
    private LatLng popupLatLng;
    private List<Marker> markerList;
    public Marker mLastShownInfoWindowMarker;
    boolean isMarkerOpen = false;

    //widgets
    private Switch heatmapSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mContext = this;

        heatMapGreenData = new ArrayList<WeightedLatLng>();
        heatMapRedData = new ArrayList<WeightedLatLng>();
        markerList = new ArrayList<>();

        //mMapView.onResume(); // needed to get the map to display immediately

        setupBottomNavigationView();

        heatmapSwitch = (Switch) findViewById(R.id.switchHeatmap);

        heatmapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showHeatMap();
                } else {
                    hideHeatMap();
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // For showing a move to my location button
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
            googleMap.setMyLocationEnabled(true);
        mMap.setMyLocationEnabled(true);

        LatLng currentLocation;

        if (currentAddress!=null){
            // For dropping a marker at a point on the Map
            currentLocation = new LatLng(currentAddress.getLatitude(), currentAddress.getLongitude());
        }
        else{
            currentLocation = new LatLng(46.7698435,23.5888696 );
            Toast.makeText(this.getBaseContext(), "Failed to load location!", Toast.LENGTH_LONG).show();
        }

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(15).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //set onMapClick listener and add the popup
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                if(popup != null && popup.isShowing()){
                    popup.dismiss();
                } else if (chartSensorPopUp!=null && chartSensorPopUp.isShowing()){
                    chartSensorPopUp.dismiss();
                    mLastShownInfoWindowMarker.hideInfoWindow();
                    isMarkerOpen = false;
                } else if (mLastShownInfoWindowMarker != null && isMarkerOpen){
                    mLastShownInfoWindowMarker.hideInfoWindow();
                    isMarkerOpen = false;
                } else{
                    View popupContent = getLayoutInflater().inflate(R.layout.layout_rating, null);
                    popup = new PopupWindow();
                    popupLatLng = point;

                    //popup should wrap content view
                    popup.setWindowLayoutMode(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    popup.setHeight(250);
                    popup.setWidth(350);

                    //set content and background
                    popup.setContentView(popupContent);
                    popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_layout));

                    popup.showAtLocation(popupContent, Gravity.CENTER,0,0);
                }
            }
        });

        mMap.setOnCameraChangeListener(new
            GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    for(Marker m:markerList){
                        m.setVisible(cameraPosition.zoom>13);
                        //8 here is your zoom level, you can set it as your need.
                    }
                }
            });

        // override markerclicklistener to store lastShownInfoWindowMarker in
        // the activity where back button will be used
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mLastShownInfoWindowMarker = marker;
                isMarkerOpen = true;
                return false; // false keeps the standard behavior
            }
        });

        mMap.setOnInfoWindowClickListener(this);

        setSensorMarkers();
        getRatingData();
    }

    public BitmapDrawable writeOnDrawable(String text){
        /**
         * GREEN -> Very Low: 0 - 300 particles/0.01 cubic feet (Excellent)
         * PALE GREEN -> Low: 301 - 600 particles/0.01 cubic feet (Good)
         * YELLOW -> Medium: 601 - 900 particles/0.01 cubic feet (Good -> Fair)
         * ORANGE -> High: 901 - 1200 particles/0.01 cubic feet (Fair)
         * RED -> Very High: > 1200 particles/0.01 cubic feet (Poor)
         */
        Bitmap bm = null;
        if(Double.parseDouble(text) <= 300){
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_green).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Double.parseDouble(text) > 300 && Double.parseDouble(text) <= 600) {
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_palegreen).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Double.parseDouble(text) > 600 && Double.parseDouble(text) <= 900) {
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_yellow).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Double.parseDouble(text) > 900 && Double.parseDouble(text) <= 1200) {
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_orange).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Double.parseDouble(text) > 1200) {
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_red).copy(Bitmap.Config.ARGB_8888, true);
        }

        bm.setDensity(255);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        Canvas canvas = new Canvas(bm);

        // Change the position of text here
        canvas.drawText(text,bm.getWidth()/2 //x position
                , bm.getHeight()/2  // y position
                , paint);
        return new BitmapDrawable(this.getResources(),getResizedBitmap(bm, 180,160));
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


    private void setSensorMarkers(){
        for (SensorPoint info : mSensorPointsList) {

            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.position(new LatLng(info.getCoordinates().latitude, info.getCoordinates().longitude))
                    .title(info.getArea())
                    .icon(BitmapDescriptorFactory.fromBitmap(writeOnDrawable(info.getPollutionLevels().get(info.getPollutionLevels().size() - 1).getLevel().toString()).getBitmap()));

            CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(MapActivity.this);
            mMap.setInfoWindowAdapter(customInfoWindow);

            Marker m = mMap.addMarker(markerOptions);

            markerList.add(m);
            m.setTag(info);
        }
    }

    private void getRatingData(){
        // Read from the database

        for (RatingPoint info : mRatingPointList){
            MarkerOptions markerOptions = new MarkerOptions();

            Bitmap bm = null;

            if(info.getRating() <= 1){
                bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_star_icon_red).copy(Bitmap.Config.ARGB_8888, true), 200, 200, true);
            }
            else if(info.getRating() > 1 && info.getRating() <= 2) {
                bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_star_icon_orange).copy(Bitmap.Config.ARGB_8888, true), 200, 200, true);
            }
            else if(info.getRating() > 2 && info.getRating() <= 3) {
                bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_star_icon_yellow).copy(Bitmap.Config.ARGB_8888, true), 200, 200, true);
            }
            else if(info.getRating() > 3 && info.getRating() <= 4) {
                bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_star_icon_palegreen).copy(Bitmap.Config.ARGB_8888, true), 200, 200, true);
            }
            else if(info.getRating() > 4) {
                bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_star_icon_green).copy(Bitmap.Config.ARGB_8888, true), 200, 200, true);
            }

            markerOptions.position(new LatLng(info.getCoordinates().latitude, info.getCoordinates().longitude))
                    .title(info.getArea())
                    .icon(BitmapDescriptorFactory.fromBitmap(bm));

            CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(MapActivity.this);
            mMap.setInfoWindowAdapter(customInfoWindow);

            Marker m = mMap.addMarker(markerOptions);
            markerList.add(m);
            m.setTag(info);

            if(info.getRating()<2.5){
                heatMapRedData.add(new WeightedLatLng(new LatLng(info.getCoordinates().latitude, info.getCoordinates().longitude),
                        Math.abs(info.getRating()-5)));
            } else{
                heatMapGreenData.add(new WeightedLatLng(new LatLng(info.getCoordinates().latitude, info.getCoordinates().longitude),
                        info.getRating()));
            }
        }
    }

    public void showHeatMap() {
        // Create the gradient.
        int[] colorsGreen = {
                Color.argb(0,180, 229, 134), //transparent pale green
                Color.rgb(102, 225, 0) // green
        };

        int[] colorsRed = {
                Color.argb(0,200, 100, 0), //transparent pale green
                Color.rgb(255, 49, 0) // green
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradientGreen = new Gradient(colorsGreen, startPoints);
        Gradient gradientRed = new Gradient(colorsRed, startPoints);


        if (heatMapGreenData!=null){
            // Create a heat map tile provider, passing it the latlngs of the police stations.
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().weightedData(heatMapGreenData).gradient(gradientGreen).build();
            mProvider.setRadius(50);
            // Add a tile overlay to the map, using the heat map tile provider.
            mOverlayGreen = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
        if (heatMapRedData!=null){
            // Create a heat map tile provider, passing it the latlngs of the police stations.
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().weightedData(heatMapRedData).gradient(gradientRed).build();
            mProvider.setRadius(50);
            // Add a tile overlay to the map, using the heat map tile provider.
            mOverlayRed = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
    }

    public void hideHeatMap(){
        if (heatMapGreenData!=null) {
            mOverlayGreen.remove();
            mOverlayGreen.clearTileCache();
        }
        if (heatMapRedData!=null) {
            mOverlayRed.remove();
            mOverlayRed.clearTileCache();
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(MapActivity.this, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Display rating by calling getRating() method.
     * @param view
     */
    public void rateMe(View view){


        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());


        try {
            addresses = geocoder.getFromLocation(popupLatLng.latitude, popupLatLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize RatingBar
        RatingBar ratingBar = (RatingBar) popup.getContentView().findViewById(R.id.ratingBar);
        Button submitRating = (Button) popup.getContentView().findViewById(R.id.submitRating);
        TextView messageRating = (TextView) popup.getContentView().findViewById(R.id.messageRating);

        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date

        DatabaseReference newChildRef = mDatabaseRatings.push();

        String newPostKey = newChildRef.getKey();
        // Create the data we want to update
        Map newPost = new HashMap();
        newPost.put("area", addresses.get(0).getThoroughfare());

        Map newPostCoordinates = new HashMap();
        newPostCoordinates.put("latitude", popupLatLng.latitude);
        newPostCoordinates.put("longitude",popupLatLng.longitude);
        newPost.put("coordinates", newPostCoordinates);

        Map newPostDateTime = new HashMap();
        newPostDateTime.put("day", calendar.get(Calendar.DAY_OF_MONTH));
        newPostDateTime.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
        newPostDateTime.put("minute", calendar.get(Calendar.MINUTE));
        newPostDateTime.put("month", calendar.get(Calendar.MONTH)+1);
        newPostDateTime.put("year", calendar.get(Calendar.YEAR));
        newPost.put("dateTime", newPostDateTime);

        newPost.put("rating", Double.parseDouble(String.format("%.1f", ratingBar.getRating())));

        Map updatedUserData = new HashMap();
        updatedUserData.put(newPostKey, newPost);
        // Do a deep-path update
        mDatabaseRatings.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Error updating data: " + databaseError.getMessage());
                }
                mMap.clear();
                getRatingData();
                getSensors();
                if (heatmapSwitch.isActivated()){
                    showHeatMap();
                }
            }
        });

        ratingBar.setVisibility(View.INVISIBLE);
        submitRating.setVisibility(View.INVISIBLE);
        messageRating.setVisibility(View.VISIBLE);
        messageRating.setText("Your rating is: "+String.format("%.1f", ratingBar.getRating()));

        //Toast.makeText(getApplicationContext(), String.valueOf(ratingBar.getRating()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (marker.getTag().getClass() == (new SensorPoint()).getClass()) {
            for (SensorPoint info : mSensorPointsList) {

                if (info.getArea().equals(((SensorPoint) marker.getTag()).getArea())) {

                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupContent = inflater.inflate(R.layout.layout_line_chart, null);
                    chartSensorPopUp = new PopupWindow();

                    //popup should wrap content view
                    chartSensorPopUp.setWindowLayoutMode(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    chartSensorPopUp.setHeight(250);
                    chartSensorPopUp.setWidth(350);

                    //set content and background
                    chartSensorPopUp.setContentView(popupContent);
                    chartSensorPopUp.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.round_layout));

                    chartSensorPopUp.showAtLocation(popupContent, Gravity.CENTER, 0, 0);

                    // in this example, a LineChart is initialized from xml
                    LineChart chart = (LineChart) popupContent.findViewById(R.id.lineChart);

                    List<Entry> entries = new ArrayList<Entry>();
                    ArrayList<String> labels = new ArrayList<String>();
                    int index = -1;
                    info.getPollutionLevels().sort(Comparator.comparing(PollutionLevel::getTime));
                    for (PollutionLevel obj : info.getPollutionLevels()) {
                        if (index < 6) {
                            index++;
                            entries.add(new Entry(index, obj.getLevel().floatValue()));
                            labels.add(obj.getTime());
                        }
                    }


                    chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP_INSIDE);
                    chart.getAxisRight().setEnabled(false);
                    chart.getXAxis().setTextSize(12f);
                    chart.getXAxis().setGranularity(1f);
                    chart.getXAxis().setGranularityEnabled(true);

                    chart.setExtraBottomOffset(-70f);

                    LineDataSet dataset = new LineDataSet(entries, "");

                    //dataset.setDrawValues(false);

                    LineData data = new LineData(dataset);
                    chart.setData(data);
                    chart.getDescription().setText("");
                    dataset.setColors(ColorTemplate.COLORFUL_COLORS);
                    chart.animateY(1000);
                }
            }
        }

    }
}
