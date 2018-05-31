package com.example.dianasoponar.pollutionmap;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dianasoponar.pollutionmap.Models.LocationPoint;
import com.example.dianasoponar.pollutionmap.Utils.BottomNavigationViewHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Date;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final int ACTIVITY_NUM = 1;

    private GoogleMap mMap;

    //add Firebase Database stuff
    public FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //enable disk persistence
        if (FirebaseApp.getApps(MapActivity.this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = mFirebaseDatabase.getReference("locations");

        //The Firebase Realtime Database synchronizes and stores a local copy of the data for active listeners
        mDatabase.keepSynced(true);

        //mMapView.onResume(); // needed to get the map to display immediately

        setupBottomNavigationView();

        getData();
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
        mMap.setMyLocationEnabled(true);

        // For dropping a marker at a point on the Map
        LatLng cluj = new LatLng(46.7712101, 23.6236353);
        //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(cluj).zoom(13).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //googleMap.addMarker(new MarkerOptions().position(new LatLng(-34, 151)).title("53").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_icon)));

        // Set a listener for info window events.
        //googleMap.setOnInfoWindowClickListener(this);
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
        if(Integer.parseInt(text) <= 300){
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_green).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Integer.parseInt(text) > 300 && Integer.parseInt(text) <= 600) {
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_palegreen).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Integer.parseInt(text) > 600 && Integer.parseInt(text) <= 900) {
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_yellow).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Integer.parseInt(text) > 900 && Integer.parseInt(text) <= 1200) {
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_orange).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Integer.parseInt(text) > 1200) {
            bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_marker_icon_red).copy(Bitmap.Config.ARGB_8888, true);
        }

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
        return new BitmapDrawable(this.getResources(),getResizedBitmap(bm, 150, 150));
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


    private void getData(){

        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {

                    MarkerOptions markerOptions = new MarkerOptions();

                    markerOptions.position(new LatLng((double)childSnapshot.child("coordinates").child("latitude").getValue(),
                                    (double)childSnapshot.child("coordinates").child("longitude").getValue()))
                            .title((String)childSnapshot.child("area").getValue())
                            .icon(BitmapDescriptorFactory.fromBitmap(writeOnDrawable(childSnapshot.child("pollutionLevel").getValue().toString()).getBitmap()));

                    LocationPoint info = new LocationPoint();
                    info.setArea("aaaa");
                    info.setPollutionLevel(425);

                    CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(MapActivity.this);
                    mMap.setInfoWindowAdapter(customInfoWindow);

                    Marker m = mMap.addMarker(markerOptions);
                    m.setTag(info);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to read values.", Toast.LENGTH_LONG).show();                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
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
}
