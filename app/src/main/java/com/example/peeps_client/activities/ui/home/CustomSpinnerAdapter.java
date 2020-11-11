package com.example.peeps_client.activities.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.peeps_client.R;

import java.util.List;
import java.util.Map;

public class CustomSpinnerAdapter extends ArrayAdapter<Integer> {
    private Integer[] images;

    public CustomSpinnerAdapter(Context context, Integer[] images) {
        super(context, android.R.layout.simple_spinner_item, images);
        this.images = images;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }
    private View getImageForPosition(int position) {
        ImageView imageView = new ImageView(getContext());
        Bitmap bmp= BitmapFactory.decodeResource(getContext().getResources(),images[position]);//image is your image
        bmp= Bitmap.createScaledBitmap(bmp, 300,300, true);
//        img.setImageBitmap(bmp);

        imageView.setImageBitmap(bmp);
        imageView.setMaxWidth(5);
        imageView.setMaxWidth(5);
        imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return imageView;
    }
}
