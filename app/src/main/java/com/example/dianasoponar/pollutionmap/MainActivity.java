package com.example.dianasoponar.pollutionmap;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final java.lang.String TAG = "MainActivity";

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;

    //add Firebase Database stuff
    public FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabase;

    //widgets
    private BottomNavigationViewEx mBottomNavigationViewEx;
    private Boolean mLocationPermissionGranted;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.bottom_nav_home:{
                Log.d(TAG, "onNavigationItemSelected: HomeFragment.");
                HomeFragment homeFragment = new HomeFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_content_frame, homeFragment, getString(R.string.tag_fragment_home));
                transaction.addToBackStack(getString(R.string.tag_fragment_home));
                transaction.commit();
                item.setChecked(true);
                break;
            }
            case R.id.bottom_nav_device:{
                Log.d(TAG, "onNavigationItemSelected: DeviceFragment.");
                DeviceFragment deviceFragment = new DeviceFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_content_frame, deviceFragment, getString(R.string.tag_fragment_device));
                transaction.addToBackStack(getString(R.string.tag_fragment_device));
                transaction.commit();
                item.setChecked(true);
                break;
            }
            case R.id.bottom_nav_map:{
                Log.d(TAG, "onNavigationItemSelected: MapFragment.");
                MapFragment mapFragment = new MapFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_content_frame, mapFragment, getString(R.string.tag_fragment_map));
                transaction.addToBackStack(getString(R.string.tag_fragment_map));
                transaction.commit();
                item.setChecked(true);
                break;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomNavigationViewEx = findViewById(R.id.bottom_nav_view);
        mBottomNavigationViewEx.getMenu().getItem(1).setChecked(true);

        mBottomNavigationViewEx.setOnNavigationItemSelectedListener(this);

        //enable disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = mFirebaseDatabase.getReference();

        //The Firebase Realtime Database synchronizes and stores a local copy of the data for active listeners
        mDatabase.keepSynced(true);

        initBottomNavigationView();
        init();
        getLocationPermission();
    }

    private void initBottomNavigationView(){
        Log.d(TAG, "initBottomNavigationView: initializing the bottom navigation view.");
    }

    private void init(){
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_frame, homeFragment, getString(R.string.tag_fragment_home));
        transaction.addToBackStack(getString(R.string.tag_fragment_home));
        transaction.commit();

        mBottomNavigationViewEx.getMenu().getItem(1).setChecked(true);
    }

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }
}
