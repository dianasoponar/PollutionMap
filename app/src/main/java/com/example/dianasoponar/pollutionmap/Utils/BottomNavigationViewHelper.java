package com.example.dianasoponar.pollutionmap.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.dianasoponar.pollutionmap.DeviceActivity;
import com.example.dianasoponar.pollutionmap.HomeActivity;
import com.example.dianasoponar.pollutionmap.MapActivity;
import com.example.dianasoponar.pollutionmap.R;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx){
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                    case R.id.bottom_nav_home:
                        Intent intent1 = new Intent(context, HomeActivity.class);//ACTIVITY_NUM = 0
                        context.startActivity(intent1);
                        //callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.bottom_nav_map:
                        Intent intent2  = new Intent(context, MapActivity.class);//ACTIVITY_NUM = 1
                        context.startActivity(intent2);
                        //callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.bottom_nav_device:
                        Intent intent3 = new Intent(context, DeviceActivity.class);//ACTIVITY_NUM = 2
                        context.startActivity(intent3);
                        //callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }


                return false;
            }
        });
    }
}