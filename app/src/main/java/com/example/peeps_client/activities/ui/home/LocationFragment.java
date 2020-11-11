package com.example.peeps_client.activities.ui.home;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peeps_client.R;
import com.example.peeps_client.supplementary.JSONParser;
import com.example.peeps_client.supplementary.SQLiteDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationFragment extends Fragment{
    private static final String TAG = "HomeFragment";

//    private LocationViewModel locationViewModel;

    //images
    int images[] = {R.drawable.ic_24px_home,R.drawable.ic_24px_business,R.drawable.ic_24px_shopping,R.drawable.ic_24px_location};
    int imagest[] ={R.drawable.ic_24px_home};
    RecyclerView recyclerView;
    //text
    String titles[] = {"Home","Work","Checkers","Park"};
    String states[] = {"busy","normal","busy","quiet","quiet","quiet","quiet","quiet"};

    RecyclerAdapter recyclerAdapter;
    Context context;

    //Emulator IP for getting location
    private static String url_pop_data;
    // JSON tags
    private static final String TAG_LOCATIONS = "locations";
    private static final String TAG_SUCCESS = "success";
    //JsonParser
    final JSONParser jsonParser = new JSONParser();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);

        //set server ip
        url_pop_data = "http://"+getResources().getString(R.string.serverIP)+"/peeps-server/get_pop_data.php";

        setHasOptionsMenu(true); // remake toolbar

        context = getContext();

        recyclerView = getView().findViewById(R.id.rv_saved);

        // read stored locations
        String[][] savedLocationLonLat = readFromDB(view);
        //get location data from server
        getPopDensity(savedLocationLonLat);

        //test
//        populateRecycler(new int[][]{{1,1,1,5,3,2,1,1,3,4,2,5,6,4,2,2},{0,0,0,0,0,0,0,0,0,0,2,5,6,4,2,2},{1,1,1,5,3,2,1,1,3,4,2,5,6,4,2,2},{1,1,1,5,3,2,1,1,3,4,2,5,6,4,2,2},{1,1,1,5,3,2,1,1,3,4,2,5,6,4,2,2}}); //test

    }

    // Add custom toolbar with 'add item' button
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(TAG,"onCreateOptionsMenu(...) called");
        inflater.inflate(R.menu.home_fragment_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);


    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addSavedButton) {
            // Add new saved location


            FragmentManager fragmentManager = getFragmentManager();
            AddLocationFragment newFragment = new AddLocationFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            item.setVisible(false);
            transaction.add(((ViewGroup)getView().getParent()).getId(), newFragment).addToBackStack(null).commit();
            fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
                    readFromDB(getView());
                    item.setVisible(true);
                    super.onFragmentStopped(fm, f);
                }
            }, true);



        }else if(id == R.id.deleteAllButton){
            deleteAllFromDB();
            readFromDB(getView());
        }
        return super.onOptionsItemSelected(item);
    }


    private String[][] readFromDB(View view) {

        SQLiteDatabase database = new SQLiteDBHelper(context).getReadableDatabase(); //error needs fixing

        String[] projection = {
                SQLiteDBHelper.LOCATION_COLUMN_NAME,
                SQLiteDBHelper.LOCATION_COLUMN_LAT,
                SQLiteDBHelper.LOCATION_COLUMN_LONG,
                SQLiteDBHelper.LOCATION_COLUMN_IMAGE
        };


        Cursor cursor = database.query(
                SQLiteDBHelper.LOCATION_TABLE_NAME,   // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause (selection)
                null,                            // The values for the WHERE clause (selectionArgs)
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // don't sort
        );

        Log.d(TAG, "The total cursor count is " + cursor.getCount());

        printDatabase(cursor);

        titles = cursorToString(cursor, SQLiteDBHelper.LOCATION_COLUMN_NAME);
        Integer[] img = cursorToInt(cursor, SQLiteDBHelper.LOCATION_COLUMN_IMAGE);
                images = new int[img.length];
        for (int i = 0; i < img.length; i++) {
            images[i] = img[i];
        }

        String[] latString = cursorToString(cursor, SQLiteDBHelper.LOCATION_COLUMN_LAT);
        String[] lonString = cursorToString(cursor, SQLiteDBHelper.LOCATION_COLUMN_LONG);

        cursor.close();
        database.close();

        return new String[][]{latString, lonString};
    }

    private void getPopDensity(String[][] savedLocationLonLat){
        Log.d(TAG,"getPopDensity(...) Called");
        JSONObject jsonObject = new JSONObject();
        int locCount = savedLocationLonLat[0].length;

        //convert to correct format
        String[][] locs = new String[locCount][2];
        for (int i = 0; i < locCount; i++) {
            locs[i] = new String[]{savedLocationLonLat[0][i],savedLocationLonLat[1][i]};
        }

        try {
            jsonObject.put(TAG_LOCATIONS, new JSONArray(locs));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(){
            @Override
            public void run() {

                Log.d(TAG,"Making Http Post Request");
                JSONObject JSONReturn = jsonParser.makeHttpRequest(url_pop_data, "POST", jsonObject);

                if (JSONReturn==null){
                    Log.d(TAG, " Post response: "+"JSON returned NULL");
                    return;
                }
                Log.d(TAG, " Post response: "+JSONReturn.toString());
                try {
                    int success = JSONReturn.getInt(TAG_SUCCESS);
                    if (success == 1){
                        //succeeded

                        int populationTimeArray[][] = new int[locs.length][15];

                        for (int i = 8; i < 23; i++) {
                            for (int j = 0; j < locs.length; j++) {
                                populationTimeArray[j][i-8] = JSONReturn.getInt("n"+i+"_"+(i+1)+"_loc"+j);
                            }

                        }

                        //fill recylerView
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                System.out.println(Arrays.toString(populationTimeArray));
                                populateRecycler(populationTimeArray); //change
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

    private void populateRecycler(int populationTimeArray[][]){
        recyclerAdapter = new RecyclerAdapter(getContext(), titles, states, images,populationTimeArray,recyclerView); // (getContext(), titles, states, images,recyclerView)
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void deleteAllFromDB() {
        SQLiteDBHelper sqldb = new SQLiteDBHelper(getContext());
        sqldb.onUpgrade(sqldb.getReadableDatabase(),0,0);

    }



    String[] cursorToString(Cursor cursor, String column){
        cursor.moveToFirst();
        ArrayList<String> names = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getString(cursor.getColumnIndex(column)));
            cursor.moveToNext();
        }
        return names.toArray(new String[names.size()]);
    }
    Integer[] cursorToInt(Cursor cursor, String column){
        cursor.moveToFirst();
        ArrayList<Integer> names = new ArrayList<Integer>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getInt(cursor.getColumnIndex(column)));
            cursor.moveToNext();
        }
        return names.toArray(new Integer[names.size()]);
    }

    void printDatabase(Cursor cursor){
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Log.d(TAG,"Name: "+cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.LOCATION_COLUMN_NAME))+", Image: "+cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.LOCATION_COLUMN_IMAGE)));
            cursor.moveToNext();
        }

    }

}