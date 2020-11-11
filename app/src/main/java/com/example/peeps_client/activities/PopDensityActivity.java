package com.example.peeps_client.activities;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.peeps_client.R;
import com.example.peeps_client.supplementary.LocationJobService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PopDensityActivity extends AppCompatActivity {
    private static final String TAG = "PopDensityActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate().");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_density);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_mapmode)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // check if user has accepted location permissions
        checkLocationPermissions();
        // start background job uploading location info.
        scheduleLocationJobService();

    }

    // start background location upload service
    private void scheduleLocationJobService(){
        Log.d(TAG, "Check LocationJob status.");
        JobScheduler scheduler = (JobScheduler) this.getSystemService( Context.JOB_SCHEDULER_SERVICE );
        boolean hasBeenScheduled = false ;

        // check if job is already scheduled
        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            Log.d(TAG, "LocationJob contents:"+jobInfo.getId());
            if ( jobInfo.getId() == 35800 ) { //JOB_ID = 35800 (note:arbitrary)

                hasBeenScheduled = true ;
                break ;
            }
        }
        // return if already scheduled
        if (hasBeenScheduled){
            Log.d(TAG, "LocationJob already scheduled.");
            return;
        }
        // schedule new location JobService
        ComponentName componentName = new ComponentName(this, LocationJobService.class);
        JobInfo info = new JobInfo.Builder(35800, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setPersisted(true)             // persists after reboot
                .setPeriodic(15*60*1000)        // repeats at 15 min intervals
                .build();
        Log.d(TAG, "LocationJob scheduling in process.");
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule((info));
        if(resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "LocationJob scheduled.");
        }else{
            Log.d(TAG, "LocationJob failed to schedule.");
        }
    }

    // check if user has given app location Permissions
    public void checkLocationPermissions() {
        ///Check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {//Can add more as per requirement

            //Request if not found
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    123);
        }
    }

}