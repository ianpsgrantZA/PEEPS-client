package com.example.peeps_client.activities.ui.home;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.peeps_client.R;
import com.example.peeps_client.supplementary.SQLiteDBHelper;


public class AddLocationFragment extends DialogFragment {
    private static final String TAG = "AddLocationFragment";

    Integer images[] = {R.drawable.ic_24px_home,R.drawable.ic_24px_business,R.drawable.ic_24px_shopping,R.drawable.ic_24px_location};
    int intImages[] = {R.drawable.ic_24px_home,R.drawable.ic_24px_business,R.drawable.ic_24px_shopping,R.drawable.ic_24px_location};

    EditText etxtName, etxtAddress, etxtLatitude, etxtLongitude;
    TextView txtAddress, txtLatitude, txtLongitude;
    Button btnSaveLocation, btnCancel;
    Switch swCoords;
    Spinner sp;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_location, container, false);

        sp = rootView.findViewById(R.id.spIcons);
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), images);
        sp.setAdapter(adapter);

        etxtName = rootView.findViewById(R.id.etxtLocName);
        etxtAddress = rootView.findViewById(R.id.etxtEnterAddress);
        etxtLatitude = rootView.findViewById(R.id.etxtEnterLat);
        etxtLongitude = rootView.findViewById(R.id.etxtEnterLong);
        btnSaveLocation = rootView.findViewById(R.id.btnSaveLocation);
        btnCancel = rootView.findViewById(R.id.btnCancel);
        swCoords = rootView.findViewById(R.id.swCoords);
        txtAddress = rootView.findViewById(R.id.txtEnterAddress);
        txtLatitude = rootView.findViewById(R.id.txtEnterLat);
        txtLongitude = rootView.findViewById(R.id.txtEnterLong);

        etxtLatitude.setEnabled(false);
        etxtLongitude.setEnabled(false);
        etxtLongitude.setVisibility(View.GONE);
        etxtLatitude.setVisibility(View.GONE);
        txtLongitude.setVisibility(View.GONE);
        txtLatitude.setVisibility(View.GONE);
        txtAddress.setVisibility(View.VISIBLE);

        // Switch listener
        swCoords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (swCoords.isChecked()){
                    etxtLatitude.setEnabled(true);
                    etxtLongitude.setEnabled(true);
                    etxtAddress.setEnabled(false);
                    etxtLongitude.setVisibility(View.VISIBLE);
                    etxtLatitude.setVisibility(View.VISIBLE);
                    etxtAddress.setVisibility(View.GONE);
                    txtLongitude.setVisibility(View.VISIBLE);
                    txtLatitude.setVisibility(View.VISIBLE);
                    txtAddress.setVisibility(View.GONE);
                }else{
                    etxtLatitude.setEnabled(false);
                    etxtLongitude.setEnabled(false);
                    etxtAddress.setEnabled(true);
                    etxtLongitude.setVisibility(View.GONE);
                    etxtLatitude.setVisibility(View.GONE);
                    etxtAddress.setVisibility(View.VISIBLE);
                    txtLongitude.setVisibility(View.GONE);
                    txtLatitude.setVisibility(View.GONE);
                    txtAddress.setVisibility(View.VISIBLE);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (swCoords.isChecked()){
                    //save coords
                    String name = etxtName.getText().toString();
                    float lat = Float.parseFloat(etxtLatitude.getText().toString());
                    float lon = Float.parseFloat(etxtLongitude.getText().toString());
                    int imageID = intImages[sp.getSelectedItemPosition()];
                    Log.d(TAG, "position: "+sp.getSelectedItemPosition()+", image: "+imageID);
                    for (int i = 0; i < intImages.length; i++) {
                        Log.d(TAG, "image "+i+" "+intImages[i]);
                    }

                    saveToDB(name,lat,lon,imageID);

                    dismiss();

                }else{
                    //look up address
                    //add saved location
                    dismiss();
                }
            }
        });

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        return dialog;
    }


    private void saveToDB(String name,float lat,float lon, int imageID) {
        SQLiteDatabase database = new SQLiteDBHelper(getContext()).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteDBHelper.LOCATION_COLUMN_NAME, name);
        values.put(SQLiteDBHelper.LOCATION_COLUMN_LAT, lat);
        values.put(SQLiteDBHelper.LOCATION_COLUMN_LONG, lon);
        values.put(SQLiteDBHelper.LOCATION_COLUMN_IMAGE, imageID);
        long newRowId = database.insert(SQLiteDBHelper.LOCATION_TABLE_NAME, null, values);

        Log.d(TAG,"valueList:"+values.toString());

        Toast.makeText(getContext(), "The new Row Id is " + newRowId, Toast.LENGTH_LONG).show();
    }

}