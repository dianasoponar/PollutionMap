package com.example.dianasoponar.pollutionmap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.dianasoponar.pollutionmap.Models.PollutionLevel;
import com.example.dianasoponar.pollutionmap.Models.RatingPoint;
import com.example.dianasoponar.pollutionmap.Models.SensorPoint;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.example.dianasoponar.pollutionmap.Utils.Globals.chartSensorPopUp;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.mSensorPointsList;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = "CustomInfoWindowGoogleMap";

    private Context mContext;

    public CustomInfoWindowGoogleMap(Context ctx){
        mContext = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        View view = null;
        if (marker.getTag().getClass() == (new SensorPoint()).getClass()){
            SensorPoint infoWindowData = (SensorPoint) marker.getTag();
            view = ((Activity) mContext).getLayoutInflater()
                    .inflate(R.layout.layout_info_window, null);
            TextView pollutionLevel = view.findViewById(R.id.pollutionLevelInfoWindow);
            TextView index = view.findViewById(R.id.indexInfoWindow);
            TextView recommendations = view.findViewById(R.id.recommendationsInfoWindow);
            TextView area = view.findViewById(R.id.areaInfoWindow);
            TextView timestamp = view.findViewById(R.id.timestampInfoWindow);
            ImageView alert = view.findViewById(R.id.imageAlert);

            //set the latest index
            PollutionLevel latestLevel = infoWindowData.getPollutionLevels().get(infoWindowData.getPollutionLevels().size()-1);
            pollutionLevel.setText(Double.valueOf(latestLevel.getLevel()).toString());

            //set the pollution level
            /**
             * GREEN -> Very Low: 0 - 300 Good
             * PALE GREEN -> Low: 301 - 600 Moderate
             * YELLOW -> Medium: 601 - 900 Unhealthy for Sensitive Groups
             * ORANGE -> High: 901 - 1200 Unhealthy
             * RED -> Very High: > 1200 Very Unhealthy
             */
            if(Double.valueOf(pollutionLevel.getText().toString()) <= 300){
                index.setText("Very Low");
                recommendations.setText("Enjoy your usual outdoor activities.");
                alert.setImageResource(R.drawable.ic_alert_icon_green);
            }
            else if(Double.valueOf(pollutionLevel.getText().toString()) > 300 && Double.valueOf(pollutionLevel.getText().toString()) <= 600) {
                index.setText("Low");
                recommendations.setText("Air quality is acceptable.");
                alert.setImageResource(R.drawable.ic_alert_icon_palegreen);
            }
            else if(Double.valueOf(pollutionLevel.getText().toString()) > 600 && Double.valueOf(pollutionLevel.getText().toString()) <= 900) {
                index.setText("Medium");
                recommendations.setText("May be unhealthy for sensitive groups");
                alert.setImageResource(R.drawable.ic_alert_icon_yellow);
            }
            else if(Double.valueOf(pollutionLevel.getText().toString()) > 900 && Double.valueOf(pollutionLevel.getText().toString()) <= 1200) {
                index.setText("High");
                recommendations.setText("Unhealthy. Try to reduce you outdoor activities.");
                alert.setImageResource(R.drawable.ic_alert_icon_orange);
            }
            else if(Double.valueOf(pollutionLevel.getText().toString()) > 1200) {
                index.setText("VeryHigh");
                recommendations.setText("Health warnings of emergency conditions.");
                alert.setImageResource(R.drawable.ic_alert_icon_red);
            }

            area.setText(marker.getTitle());

            //set the timestamp difference
            String timestampDifference = getTimestampDifference(latestLevel.getDate());
            if(!timestampDifference.equals("0")){
                if (timestampDifference.equals("1")){
                    timestamp.setText(timestampDifference + " day ago, " + latestLevel.getTime());
                }
                else{
                    timestamp.setText(timestampDifference + " days ago, " + latestLevel.getTime());
                }
            }else{
                timestamp.setText("today at " + latestLevel.getTime());
            }

            return view;
        }
        else{
            RatingPoint infoWindowData = (RatingPoint) marker.getTag();
            view = ((Activity) mContext).getLayoutInflater()
                    .inflate(R.layout.layout_info_window_rating, null);

            TextView index = view.findViewById(R.id.indexInfoWindow);
            TextView area = view.findViewById(R.id.areaInfoWindow);
            TextView timestamp = view.findViewById(R.id.timestampInfoWindow);

            index.setText(Double.valueOf(infoWindowData.getRating()).toString());
            area.setText(marker.getTitle());

            //set the timestamp difference
            String timestampDifference = getTimestampDifference(infoWindowData.getDate());
            if(!timestampDifference.equals("0")){
                if (timestampDifference.equals("1")){
                    timestamp.setText(timestampDifference + " day ago, " + infoWindowData.getTime());
                }
                else{
                    timestamp.setText(timestampDifference + " days ago, " + infoWindowData.getTime());
                }
            }else{
                timestamp.setText("today at " + infoWindowData.getTime());
            }
            return view;
        }
    }

    @Override
    public View getInfoContents(Marker marker) { return null; }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(Date pointDate){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String locationTimestamp = sdf.format(pointDate);
        try{
            timestamp = sdf.parse(locationTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }
}
