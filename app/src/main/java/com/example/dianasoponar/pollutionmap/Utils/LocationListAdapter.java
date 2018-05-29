package com.example.dianasoponar.pollutionmap.Utils;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dianasoponar.pollutionmap.Models.LocationPoint;
import com.example.dianasoponar.pollutionmap.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LocationListAdapter extends ArrayAdapter<LocationPoint> {

    private static final String TAG = "LocationListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;

    public LocationListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<LocationPoint> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private static class ViewHolder{
        TextView area, pollutionLevel, healthRecommendations, timestamp;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.area = (TextView) convertView.findViewById(R.id.area);
            holder.pollutionLevel = (TextView) convertView.findViewById(R.id.pollutionLevel);
            holder.healthRecommendations = (TextView) convertView.findViewById(R.id.healthRecommendations);
            holder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        //set the area
        holder.area.setText(getItem(position).getArea());

        //set the area
        holder.pollutionLevel.setText(getItem(position).getPollutionLevel().toString());

        //TODO set healthRecommendations
        holder.healthRecommendations.setText("GOOD");

        //set the timestamp difference
        String timestampDifference = getTimestampDifference(getItem(position));
        if(!timestampDifference.equals("0")){
            if (timestampDifference.equals("1")){
                holder.timestamp.setText(timestampDifference + " day ago, " + getItem(position).getTime());
            }
            else{
                holder.timestamp.setText(timestampDifference + " days ago, " + getItem(position).getTime());
            }
        }else{
            holder.timestamp.setText("today");
        }


        return convertView;
    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(LocationPoint locationPoint){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String locationTimestamp = locationPoint.getDate();
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
