package com.example.dianasoponar.pollutionmap;

import android.bluetooth.BluetoothClass;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.dianasoponar.pollutionmap.Utils.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.model.ChannelFeed;
import com.macroyau.thingspeakandroid.model.Feed;

import java.util.List;

public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "HomeFragment";

    private static final int ACTIVITY_NUM = 2;

    private TextView mSensorPollutionLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mSensorPollutionLevel = (TextView)findViewById(R.id.sensorPollutionLevel);

        setupBottomNavigationView();
        getSensorData();
    }

    public void getSensorData(){

        ThingSpeakChannel tsChannel = new ThingSpeakChannel(475700);
        tsChannel.setChannelFeedUpdateListener(new ThingSpeakChannel.ChannelFeedUpdateListener() {
            @Override
            public void onChannelFeedUpdated(long channelId, String channelName, ChannelFeed channelFeed) {
                // Make use of your Channel feed here!
                List<Feed> feeds =  channelFeed.getFeeds();

                mSensorPollutionLevel.setText(feeds.get(feeds.size()-1).getField1());

                long id = channelId;
            }
        });
        tsChannel.loadChannelFeed();

    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(DeviceActivity.this, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
