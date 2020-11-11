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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peeps_client.R;
import com.example.peeps_client.supplementary.JSONParser;
import com.example.peeps_client.supplementary.SQLiteDBHelper;

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
    private static final String TAG_COORDINATES = "coordinates";
    private static final String TAG_LATITUDES = "location_lat";
    private static final String TAG_LONGITUDES = "location_lon";
    private static final String TAG_SUCCESS = "success";
    //JsonParser
    final JSONParser jsonParser = new JSONParser();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        locationViewModel =
//                ViewModelProviders.of(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
//        locationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//            }
//        });
//
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

//        populateRecycler(new int[]{1,1,1,5,3,2,1,1,3,4,2,5,6,4,2,2});

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


    private void saveToDB(String name,float lat,float lon, int imageID) {
        SQLiteDatabase database = new SQLiteDBHelper(getContext()).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteDBHelper.LOCATION_COLUMN_NAME, name);
        values.put(SQLiteDBHelper.LOCATION_COLUMN_LAT, lat);
        values.put(SQLiteDBHelper.LOCATION_COLUMN_LONG, lon);
        values.put(SQLiteDBHelper.LOCATION_COLUMN_LONG, imageID);
        long newRowId = database.insert(SQLiteDBHelper.LOCATION_TABLE_NAME, null, values);

        Toast.makeText(getContext(), "The new Row Id is " + newRowId, Toast.LENGTH_LONG).show();
    }

    private String[][] readFromDB(View view) {

        SQLiteDatabase database = new SQLiteDBHelper(context).getReadableDatabase(); //error needs fixing

        String[] projection = {
                SQLiteDBHelper.LOCATION_COLUMN_NAME,
                SQLiteDBHelper.LOCATION_COLUMN_LAT,
                SQLiteDBHelper.LOCATION_COLUMN_LONG,
                SQLiteDBHelper.LOCATION_COLUMN_IMAGE
        };

//        String selection =
//                SampleSQLiteDBHelper.PERSON_COLUMN_NAME + " like ? and " +
//                        SampleSQLiteDBHelper.PERSON_COLUMN_AGE + " > ? and " +
//                        SampleSQLiteDBHelper.PERSON_COLUMN_GENDER + " like ?";
//
//        String[] selectionArgs = {"%" + name + "%", age, "%" + gender + "%"};

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
        //get saved location latitudes and longitudes
        String[] latString = cursorToString(cursor, SQLiteDBHelper.LOCATION_COLUMN_LAT);
        String[] lonString = cursorToString(cursor, SQLiteDBHelper.LOCATION_COLUMN_LONG);
//        float savedLocationLonLat[][] = new float[2][latString.length];
//        for (int i = 0; i < latString.length; i++) {
//            savedLocationLonLat[1][i]= Float.parseFloat(latString[i]);
//            savedLocationLonLat[0][i]= Float.parseFloat(lonString[i]);
//        }

        cursor.close();
        database.close();

        return new String[][]{latString, lonString};
    }

    private void getPopDensity(String[][] savedLocationLonLat){
        Log.d(TAG,"getPopDensity(...) Called");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(TAG_LONGITUDES, savedLocationLonLat[0]);
            jsonObject.put(TAG_LATITUDES, savedLocationLonLat[1]);
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

                        int populationTimeArray[] = new int[15];
                        for (int i = 8; i < 23; i++) {
                            populationTimeArray[i-8] = JSONReturn.getInt("n"+i+"_"+(i+1));
                        }

                        //fill recylerView
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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

    private void populateRecycler(int populationTimeArray[]){
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