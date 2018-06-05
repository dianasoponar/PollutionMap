package com.example.dianasoponar.pollutionmap;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.dianasoponar.pollutionmap.Models.LocationPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.layout_info_window, null);

        TextView pollutionLevel = view.findViewById(R.id.pollutionLevelInfoWindow);
        TextView index = view.findViewById(R.id.indexInfoWindow);
        TextView recommendations = view.findViewById(R.id.recommendationsInfoWindow);
        TextView area = view.findViewById(R.id.areaInfoWindow);
        TextView timestamp = view.findViewById(R.id.timestampInfoWindow);

        LocationPoint infoWindowData = (LocationPoint) marker.getTag();

        pollutionLevel.setText(Double.valueOf(infoWindowData.getPollutionLevel()).toString());
        index.setText("LOW");
        recommendations.setText("Stay inside!");
        area.setText(marker.getTitle());
        timestamp.setText("12:31");

        return view;    }
}
