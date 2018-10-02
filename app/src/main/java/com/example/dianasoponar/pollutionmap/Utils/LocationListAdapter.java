package com.example.dianasoponar.pollutionmap.Utils;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.dianasoponar.pollutionmap.Models.PollutionLevel;
import com.example.dianasoponar.pollutionmap.Models.SensorPoint;
import com.example.dianasoponar.pollutionmap.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.example.dianasoponar.pollutionmap.Utils.Globals.mSensorPointsList;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.chartSensorPopUp;


public class LocationListAdapter extends ArrayAdapter<SensorPoint> {

    private static final String TAG = "LocationListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;

    public LocationListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<SensorPoint> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private static class ViewHolder{
        TextView area, pollutionLevel, healthRecommendations, timestamp;
        ImageView alert;
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
            holder.alert = (ImageView) convertView.findViewById(R.id.imageAlertAdapter);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        //set the area
        holder.area.setText(getItem(position).getArea());

        //set the pollution level
        /**
         * GREEN -> Very Low: 0 - 300 Good
         * PALE GREEN -> Low: 301 - 600 Moderate
         * YELLOW -> Medium: 601 - 900 Unhealthy for Sensitive Groups
         * ORANGE -> High: 901 - 1200 Unhealthy
         * RED -> Very High: > 1200 Very Unhealthy
         */

        //latest level
        PollutionLevel latestLevel = getItem(position).getPollutionLevels().get(getItem(position).getPollutionLevels().size()-1);

        holder.pollutionLevel.setText(Double.valueOf(latestLevel.getLevel()).toString());
        if(Double.valueOf(holder.pollutionLevel.getText().toString()) <= 300){
            holder.healthRecommendations.setText("Enjoy your usual outdoor activities.");
            holder.alert.setImageResource(R.drawable.ic_alert_icon_green);
        }
        else if(Double.valueOf(holder.pollutionLevel.getText().toString()) > 300 && Double.valueOf(holder.pollutionLevel.getText().toString()) <= 600) {
            holder.healthRecommendations.setText("Air quality is acceptable.");
            holder.alert.setImageResource(R.drawable.ic_alert_icon_palegreen);
        }
        else if(Double.valueOf(holder.pollutionLevel.getText().toString()) > 600 && Double.valueOf(holder.pollutionLevel.getText().toString()) <= 900) {
            holder.healthRecommendations.setText("May be unhealthy for sensitive groups");
            holder.alert.setImageResource(R.drawable.ic_alert_icon_yellow);
        }
        else if(Double.valueOf(holder.pollutionLevel.getText().toString()) > 900 && Double.valueOf(holder.pollutionLevel.getText().toString()) <= 1200) {
            holder.healthRecommendations.setText("Unhealthy. Try to reduce you outdoor activities.");
            holder.alert.setImageResource(R.drawable.ic_alert_icon_orange);
        }
        else if(Double.valueOf(holder.pollutionLevel.getText().toString()) > 1200) {
            holder.healthRecommendations.setText("Health warnings of emergency conditions.");
            holder.alert.setImageResource(R.drawable.ic_alert_icon_red);
        }

        //set the timestamp difference
        String timestampDifference = getTimestampDifference(latestLevel);
        if(!timestampDifference.equals("0")){
            if (timestampDifference.equals("1")){
                holder.timestamp.setText(timestampDifference + " day ago, " + latestLevel.getTime());
            }
            else{
                holder.timestamp.setText(timestampDifference + " days ago, " + latestLevel.getTime());
            }
        }else{
            holder.timestamp.setText("today at " + latestLevel.getTime());
        }


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chartSensorPopUp != null && chartSensorPopUp.isShowing()) {
                    chartSensorPopUp.dismiss();
                }
                else{
                    for (SensorPoint info : mSensorPointsList){
                        TextView area = (TextView) v.findViewById(R.id.area);

                        if(info.getArea().equals(area.getText().toString())){

                            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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

                            chartSensorPopUp.showAtLocation(popupContent, Gravity.CENTER,0,0);

                            // in this example, a LineChart is initialized from xml
                            LineChart chart = (LineChart) popupContent.findViewById(R.id.lineChart);

                            List<Entry> entries = new ArrayList<>();
                            ArrayList<String> labels = new ArrayList<>();
                            int index=-1;
                            info.getPollutionLevels().sort(Comparator.comparing(PollutionLevel::getTime));
                            for (PollutionLevel obj : info.getPollutionLevels()) {
                                if (index<6) {
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

                            LineDataSet dataset = new LineDataSet(entries,"");
                            chart.getDescription().setText("");

                            //dataset.setDrawValues(false);

                            LineData data = new LineData(dataset);
                            chart.setData(data);
                            dataset.setColors(ColorTemplate.COLORFUL_COLORS);
                            chart.animateY(1000);
                        }
                    }
                }
            }
        });


        return convertView;
    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(PollutionLevel locationPoint){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String locationTimestamp = sdf.format(locationPoint.getDate());
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
