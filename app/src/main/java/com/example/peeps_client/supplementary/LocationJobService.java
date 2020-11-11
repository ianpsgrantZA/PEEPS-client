package com.example.peeps_client.supplementary;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.peeps_client.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class LocationJobService extends JobService {
    private static final String TAG = "LocationJobService";
    private boolean jobCancelled = false;

    //Location Services
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //JsonParser
    final JSONParser jsonParser = new JSONParser();

    // JSON tags
    private static final String TAG_USER_ID = "user_id";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_NEARBY = "nearby";
    private static final String TAG_LOCATION_LAT = "location_lat";
    private static final String TAG_LOCATION_LON = "location_lon";

    //URL
    private static String url_store_location;

    //locationInfo
    boolean locationFound = false;
    LocationCallback mLocationCallback;

    // Username info
    SharedPreferences sharedPreferences;
    String SPLocation = "com.example.peeps_client";


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started.");

        //set server ip
        url_store_location = "http://"+getApplicationContext().getResources().getString(R.string.serverIP)+"/peeps-server/store_location.php";

        sendLocation(jobParameters);
        return true;
    }

    private void sendLocation(JobParameters jobParameters) {
        if (jobCancelled) {
            //not needed
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation(jobParameters);


    }

    public boolean checkPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {//Can add more as per requirement

            Log.d(TAG, "checkPermissions: client does not have location permissions.");
            return false;
        }
        return true;
    }


    public void sendLocationData(Location currentLocation) {
        Log.d(TAG, "sendLocationData: method called.");
        if (currentLocation == null) {
            Log.d(TAG, "location: null");
            return;
        }
        Log.d(TAG, "location: " + currentLocation.toString());

        //transmit data
        JSONObject jsonObject = new JSONObject();
        try {

            sharedPreferences = getSharedPreferences(SPLocation,MODE_PRIVATE);
            jsonObject.put(TAG_USER_ID, sharedPreferences.getString(SPLocation+".username","unknown_name"));
//            jsonObject.put(TAG_USER_ID,"unknown_name");

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
            String formatted = format.format(currentLocation.getTime());
            jsonObject.put(TAG_TIMESTAMP, formatted);

//              jsonObject.put(TAG_NEARBY, 0);
            jsonObject.put(TAG_LOCATION_LAT, currentLocation.getLatitude());
            jsonObject.put(TAG_LOCATION_LON, currentLocation.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JSONObject JSONReturn = jsonParser.makeHttpRequest(url_store_location, "POST", jsonObject);

        Log.d(TAG, "sendLocationData() end");
    }

    public void getLastLocation(final JobParameters jobParameters) {
        Log.d(TAG, "getLastLocation() called");
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "getLastLocation() onLocationResult()");
                locationFound = true;
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d(TAG, "getLastLocation() location: " + location);
                        if (checkPermission()) {
                            mFusedLocationProviderClient
                                    .removeLocationUpdates(mLocationCallback);
                            Log.d(TAG, "getLastLocation() stopTask: ");
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();

                                    sendLocationData(location);

                                    Log.d(TAG, "Job finished.");
                                    jobFinished(jobParameters, false);

                                }
                            }.start();

                        }
                    }
                }

            }
        };
        Log.d(TAG, "getLastLocation() check permission " + checkPermission());
        if (checkPermission()) {
//            LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
//            getFusedLocationProviderClient(context).removeLocationUpdates(mLocationCallback);
        }
        Log.d(TAG, "getLastLocation() finished requesting updates");

    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before completion.");
        jobCancelled = true;
        return false; //maybe true
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "taskRemoved");
    }
}
