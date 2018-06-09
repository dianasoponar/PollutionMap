/*
package com.example.dianasoponar.pollutionmap;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import static com.example.dianasoponar.pollutionmap.no_use.MainActivity.mDatabase;

public class MapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapFragment";
    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //View view = inflater.inflate(R.layout.fragment_map, container, false);

        //mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng cluj = new LatLng(46.7712101, 23.6236353);
                //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(cluj).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                //googleMap.addMarker(new MarkerOptions().position(new LatLng(-34, 151)).title("53").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_icon)));

                // Set a listener for info window events.
                //googleMap.setOnInfoWindowClickListener(this);
            }
        });

        this.getData();


        return view;
    }

    public BitmapDrawable writeOnDrawable(String text){

        /*
        GREEN -> Very Low: 0 - 300 particles/0.01 cubic feet (Excellent)
        PALE GREEN -> Low: 301 - 600 particles/0.01 cubic feet (Good)
        YELLOW -> Medium: 601 - 900 particles/0.01 cubic feet (Good -> Fair)
        ORANGE -> High: 901 - 1200 particles/0.01 cubic feet (Fair)
        RED -> Very High: > 1200 particles/0.01 cubic feet (Poor)
         */
        /*Bitmap bm = null;
        if(Integer.parseInt(text) <= 300){
            bm = BitmapFactory.decodeResource(this.getActivity().getResources(), R.drawable.ic_marker_icon_green).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Integer.parseInt(text) > 300 && Integer.parseInt(text) <= 600) {
            bm = BitmapFactory.decodeResource(this.getActivity().getResources(), R.drawable.ic_marker_icon_palegreen).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Integer.parseInt(text) > 600 && Integer.parseInt(text) <= 900) {
            bm = BitmapFactory.decodeResource(this.getActivity().getResources(), R.drawable.ic_marker_icon_yellow).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Integer.parseInt(text) > 900 && Integer.parseInt(text) <= 1200) {
            bm = BitmapFactory.decodeResource(this.getActivity().getResources(), R.drawable.ic_marker_icon_orange).copy(Bitmap.Config.ARGB_8888, true);
        }
        else if(Integer.parseInt(text) > 1200) {
            bm = BitmapFactory.decodeResource(this.getActivity().getResources(), R.drawable.ic_marker_icon_red).copy(Bitmap.Config.ARGB_8888, true);
        }

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        Canvas canvas = new Canvas(bm);

        // Change the position of text here
        canvas.drawText(text,bm.getWidth()/2 //x position
                , bm.getHeight()/2  // y position
                , paint);
        return new BitmapDrawable(this.getActivity().getResources(),bm);
    }

    private void getDate(){

        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng((double)childSnapshot.child("coordinates").child("latitude").getValue(),
                                                (double)childSnapshot.child("coordinates").child("longitude").getValue()))
                                        .title((String)childSnapshot.child("area").getValue())
                                        .icon(BitmapDescriptorFactory.fromBitmap(writeOnDrawable(childSnapshot.child("pollutionLevel").getValue().toString()).getBitmap())));
                }
                //Log.d(TAG, "Value is: " + values);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this.getContext(), "Info window clicked",
                Toast.LENGTH_SHORT).show();
    }
}
*/