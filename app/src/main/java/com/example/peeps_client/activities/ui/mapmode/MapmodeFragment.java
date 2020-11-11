package com.example.peeps_client.activities.ui.mapmode;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.peeps_client.R;
import com.example.peeps_client.supplementary.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class MapmodeFragment extends Fragment {
    private static final String TAG = "MapmodeFragment";

    //JsonParser
    final JSONParser jsonParser = new JSONParser();
    //JSON TAG
    private static final String TAG_SUCCESS = "success";

    //Emulator IP for getting location
    private static String url_mapmode;


    //GUI
    TextView txtLocIn1,txtLocIn2,txtLocIn3,txtLocIn4;
    Button btnCalulate;
    TextView txtTimeOut, txtPeopleOut;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_mapmode, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set server ip
        url_mapmode = "http://"+getResources().getString(R.string.serverIP)+"/peeps-server/best_route.php";

        //setup locations
        float travelLocation0[] = {(float) -33.972373,(float) 18.472881};
        float travelLocation1[] = {(float) -33.926661,(float) 18.447864}; //1h
        float travelLocation2[] = {(float) -33.923781,(float) 18.516924}; //1h
        float travelLocation3[] = {(float) -33.900911,(float) 18.627698}; //2h + x = 4h

        float travelLocations[][] = {travelLocation0,travelLocation1,travelLocation2,travelLocation3};

        txtLocIn1 = view.findViewById(R.id.txtLocIn1);
        txtLocIn2 = view.findViewById(R.id.txtLocIn2);
        txtLocIn3 = view.findViewById(R.id.txtLocIn3);
        txtLocIn4 = view.findViewById(R.id.txtLocIn4);
        txtTimeOut = view.findViewById(R.id.txtTimeOut);
        txtPeopleOut = view.findViewById(R.id.txtPeopleOut);
        //set test
        txtLocIn1.setText(travelLocations[0][0]+", "+travelLocations[0][1]);
        txtLocIn2.setText(travelLocations[1][0]+", "+travelLocations[1][1]);
        txtLocIn3.setText(travelLocations[2][0]+", "+travelLocations[2][1]);
        txtLocIn4.setText(travelLocations[3][0]+", "+travelLocations[3][1]);

        btnCalulate = view.findViewById(R.id.btnCalculate);
        btnCalulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optimalTime(travelLocations);
            }
        });




    }

    private void optimalTime(float[][] travelLocations){


        double travelTimes[] = new double[travelLocations.length];
        travelTimes[0] = 0;
        for (int i = 1; i < travelTimes.length; i++) {
            travelTimes[i] = travelTimes[i-1]+ timeToDestination(travelLocations[i-1],travelLocations[i]);
        }

        int intTravelTimes[] = new int[travelTimes.length];
        for (int i = 0; i < travelTimes.length; i++) {
            intTravelTimes[i] = (int) Math.floor(travelTimes[i]);
        }

//        for (int i = 0; i < travelTimes.length; i++) {
//            System.out.println("Location: "+travelLocations[i].toString()+" at time: "+intTravelTimes[i]);
//        }


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("locations", new JSONArray(travelLocations));
            jsonObject.put("times", new JSONArray(intTravelTimes));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(){
            @Override
            public void run() {

                Log.d(TAG,"Making Http Post Request");
                JSONObject JSONReturn = jsonParser.makeHttpRequest(url_mapmode, "POST", jsonObject);

                if (JSONReturn==null){
                    Log.d(TAG, " Post response: "+"JSON returned NULL");
                    return;
                }
                Log.d(TAG, " Post response: "+JSONReturn.toString());
                try {
                    int success = JSONReturn.getInt(TAG_SUCCESS);
                    if (success == 1){
                        //succeeded
                        int populationLocationTimeArray[][] = new int[travelLocations.length][15];

                        for (int i = 8; i < 23; i++) {

                            for (int j = 0; j < travelLocations.length; j++) {
                                populationLocationTimeArray[j][i-8] = JSONReturn.getInt("n"+i+"_"+(i+1)+"_loc"+j);
                            }

                        }

                        ArrayList<String> results = optimalRoute(populationLocationTimeArray,intTravelTimes, travelLocations);
                        String people = results.get(results.size()-1);
                        results.remove(results.size()-1);
                        String leaveTime="";
                        for (int i = 0; i < results.size(); i++) {
                            if (i != 0) {
                                leaveTime = leaveTime.concat(" ,");
                            }
                            leaveTime = leaveTime.concat(results.get(i));
                        }


                        String finalLeaveTime = leaveTime;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtTimeOut.setText(finalLeaveTime);
                                txtPeopleOut.setText(people +" people");
                            }
                        });


                    } else{
                        //failed

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
    //calculate time to destination
    double timeToDestination(float[] loc1, float[] loc2){
        double earthRadius = 6371000; //meters
        double walkingSpeed = 5.0;
        double dLat = Math.toRadians(loc2[0]-loc1[0]); //lat2-lat1
        double dLng = Math.toRadians(loc2[1]-loc1[1]);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(loc1[0])) * Math.cos(Math.toRadians(loc2[0])) *
                Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (double) (earthRadius * c) /1000; // km


        return dist/walkingSpeed;
    }

    ArrayList<String> optimalRoute(int[][] populationLocationTimeArray, int[] intTravelTimes, float[][] travelLocations){
        int peopleSum[] = new int[populationLocationTimeArray[0].length-intTravelTimes[intTravelTimes.length-1]];
        int largestSum = 0;
        //for time slot from (8 -> 9) to [(23 -> 24) minus time to destination]
        for (int i = 0; i < populationLocationTimeArray[0].length-intTravelTimes[intTravelTimes.length-1]; i++) {

            peopleSum[i] = 0;
            largestSum = 0;

            //for each route between destinations:
            for (int j = 0; j < travelLocations.length; j++) {

                peopleSum[i]+=populationLocationTimeArray[j][i+intTravelTimes[j]];
            }
            if (peopleSum[i] > largestSum) {
                largestSum= peopleSum[i];
            }

        }
        System.out.println("peopleSumArray: "+ Arrays.toString(peopleSum));

        ArrayList<String> idealStartTimes = new ArrayList<String>();

        int small = 1000000000;
        idealStartTimes.add("");
        //determine which time is shortest
        for (int i = 0; i < peopleSum.length; i++) { //for each start time
            if (peopleSum[i] <= small) {
                idealStartTimes.set(0,""+(i+8)+":00 - "+(i+9)+":00");
                small = peopleSum[i];
            }
        }
        idealStartTimes.add(""+small);

        return idealStartTimes;
    }
}