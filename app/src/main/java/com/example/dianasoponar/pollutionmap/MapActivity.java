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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
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

import com.example.dianasoponar.pollutionmap.Models.RatingPoint;
import com.example.dianasoponar.pollutionmap.Models.SensorPoint;
import com.example.dianasoponar.pollutionmap.Utils.BottomNavigationViewHelper;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.dianasoponar.pollutionmap.Utils.Globals.*;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final int ACTIVITY_NUM = 1;

    private GoogleMap mMap;
    private ArrayList<WeightedLatLng> heatMapGreenData;
    private ArrayList<WeightedLatLng> heatMapRedData;
    TileOverlay mOverlayGreen;
    TileOverlay mOverlayRed;
    private Context mContext;
    private PopupWindow popup;
    private LatLng popupLatLng;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;
    private List<Marker> markerList;

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
        if (checkPermissions()) {
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
        }
        mMap.setMyLocationEnabled(true);

        // For dropping a marker at a point on the Map
        LatLng cluj = new LatLng(46.7712101, 23.6236353);
        //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(cluj).zoom(15).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //set onMapClick listener and add the popup
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                if(popup != null && popup.isShowing()){
                    popup.dismiss();
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
                        m.setVisible(cameraPosition.zoom>12);
                        //8 here is your zoom level, you can set it as your need.
                    }
                }
            });


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
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        Canvas canvas = new Canvas(bm);

        // Change the position of text here
        canvas.drawText(text,bm.getWidth()/2 //x position
                , bm.getHeight()/2  // y position
                , paint);
        return new BitmapDrawable(this.getResources(),getResizedBitmap(bm, 180, 180));
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
        Bitmap bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_star_icon_yellow).copy(Bitmap.Config.ARGB_8888, true), 150, 150, true);

        for (RatingPoint info : mRatingPointList){
            MarkerOptions markerOptions = new MarkerOptions();

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
        newPost.put("area", currentAddress.getThoroughfare());

        Map newPostCoordinates = new HashMap();
        newPostCoordinates.put("latitude", popupLatLng.latitude);
        newPostCoordinates.put("longitude",popupLatLng.longitude);
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
