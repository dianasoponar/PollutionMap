package com.example.dianasoponar.pollutionmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.dianasoponar.pollutionmap.Utils.BottomNavigationViewHelper;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.model.ChannelFeed;
import com.macroyau.thingspeakandroid.model.Feed;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.dianasoponar.pollutionmap.Utils.Globals.currentAddress;
import static com.example.dianasoponar.pollutionmap.Utils.Globals.mDatabaseLocations;

public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "HomeFragment";

    private static final int ACTIVITY_NUM = 2;

    private TextView mSensorPollutionLevel;
    private Button submitPollutionLevelButton;
    private TextView messageSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        submitPollutionLevelButton = (Button) findViewById(R.id.submitPollutionLevel);
        mSensorPollutionLevel = (TextView)findViewById(R.id.sensorPollutionLevel);
        messageSubmitButton = (TextView) findViewById(R.id.messageSubmit);

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
                }
        });
        tsChannel.loadChannelFeed();

    }

    /**
     * BottomNavigationView setup
     */
    public void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(DeviceActivity.this, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    public void submitPollutionButton(View view){
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date

        DatabaseReference newChildRef = mDatabaseLocations.push();

        String newPostKey = newChildRef.getKey();
        // Create the data we want to update
        Map newPost = new HashMap();
        newPost.put("area", currentAddress.getThoroughfare() + " "
                + currentAddress.getSubThoroughfare() + ", "
                + currentAddress.getLocality() + ", "
                + currentAddress.getCountryName());

        Map newPostCoordinates = new HashMap();
        newPostCoordinates.put("latitude", currentAddress.getLatitude());
        newPostCoordinates.put("longitude", currentAddress.getLongitude());
        newPost.put("coordinates", newPostCoordinates);

        Map newPostDateTime = new HashMap();
        newPostDateTime.put("day", calendar.get(Calendar.DAY_OF_MONTH));
        newPostDateTime.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
        newPostDateTime.put("minute", calendar.get(Calendar.MINUTE));
        newPostDateTime.put("month", calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        newPostDateTime.put("year", calendar.get(Calendar.YEAR));
        newPost.put("dateTime", newPostDateTime);

        newPost.put("pollutionLevel", Double.parseDouble(mSensorPollutionLevel.getText().toString()));

        Map updatedUserData = new HashMap();
        updatedUserData.put(newPostKey, newPost);
        // Do a deep-path update
        mDatabaseLocations.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Error updating data: " + databaseError.getMessage());
                }
            }
        });

        submitPollutionLevelButton.setVisibility(View.INVISIBLE);
        messageSubmitButton.setVisibility(View.VISIBLE);
        messageSubmitButton.setText("Your data has been submitted!");
    }
}
