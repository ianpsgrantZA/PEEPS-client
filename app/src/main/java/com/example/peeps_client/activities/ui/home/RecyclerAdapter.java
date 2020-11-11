package com.example.peeps_client.activities.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peeps_client.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private static final String TAG = "RecyclerAdapter";

    // Card data
    String titles[], states[];
    int images[];
    int populationTimeArray[];
    Context context;
    RecyclerView recyclerView;

    // Card expansion details
    int mExpandedPosition = -1;


    public RecyclerAdapter(Context ct, String s1[], String s2[], int img[],int pta[],RecyclerView rv){
        context = ct;
        titles = s1;
        states = s2;
        images = img;
        recyclerView = rv;
        populationTimeArray = pta;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_row, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Set card details
        holder.txtTitle.setText(titles[position]);
//        holder.txtState.setText("State: "+states[position]);
        holder.imgIcon.setImageResource(images[position]);

        //set Card colour
        setCardColour(holder,position);

        // Expand card on selection
        final boolean isExpanded = position==mExpandedPosition;
        holder.graphTime.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:position;
                TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();
            }
        });



        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
//                new DataPoint(6, 0), //6:00
//                new DataPoint(7, 1), //
                new DataPoint(8, populationTimeArray[0]),
                new DataPoint(9, populationTimeArray[1]),
                new DataPoint(10, populationTimeArray[2]),
                new DataPoint(11, populationTimeArray[3]),
                new DataPoint(12, populationTimeArray[4]),
                new DataPoint(13, populationTimeArray[5]),
                new DataPoint(14, populationTimeArray[6]),
                new DataPoint(15, populationTimeArray[7]),
                new DataPoint(16, populationTimeArray[8]),
                new DataPoint(17, populationTimeArray[9]),
                new DataPoint(18, populationTimeArray[10]),
                new DataPoint(19, populationTimeArray[11]),
                new DataPoint(20, populationTimeArray[12]),
                new DataPoint(21, populationTimeArray[13]),
                new DataPoint(22, populationTimeArray[14])//,

//                new DataPoint(23, 11),
//                new DataPoint(24, 10)

        });
        series.setSpacing(4);
        holder.graphTime.addSeries(series);

        GridLabelRenderer gridLabel = holder.graphTime.getGridLabelRenderer();
//        gridLabel.setHorizontalAxisTitle("Time");
//        gridLabel.setVerticalAxisTitle("Population");
//        gridLabel.setPadding(50);
        gridLabel.setVerticalLabelsVisible(false);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(holder.graphTime);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"08 ","09", "10","11", "12", "13","14","15","16","17","18","19","20","21","22","23"});
        holder.graphTime.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        holder.graphTime.getViewport().setMinX(7);
        holder.graphTime.getViewport().setMaxX(23);
        holder.graphTime.getViewport().setXAxisBoundsManual(true);

    }

    //set colour of location
    void setCardColour(MyViewHolder holder, int position){
        Log.d(TAG, "colour");
        int max = 0;
        int min = 200000000;
        double upper = 0.7;
        double lower = 0.3;

        for (int i = 0; i < populationTimeArray.length; i++) {
            if (populationTimeArray[i] > max) {
                max = populationTimeArray[i];
            }
            if (populationTimeArray[i] < min) {
                min = populationTimeArray[i];
            }
        }

        Calendar calendar = GregorianCalendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        System.out.println(hour);

        if (max == min || hour<8 || hour>22) {
            holder.txtState.setText("Normal");
            holder.itemView.setBackgroundColor(Color.parseColor("#a0ed8e")); //normal
        }else if(populationTimeArray[hour-8]>=upper*(max-min)){
            holder.txtState.setText("Busy");
            holder.itemView.setBackgroundColor(Color.parseColor("#fa8484")); //busy
        }else if (populationTimeArray[hour-8] <= lower*(max-min)) {
            holder.txtState.setText("Quiet");
            holder.itemView.setBackgroundColor(Color.parseColor("#8eaee6")); //quiet #8eaee6
        }else{
            holder.txtState.setText("Normal");
            holder.itemView.setBackgroundColor(Color.parseColor("#a0ed8e")); //normal
        }
        
        //testing
//        if (position == 3) {
////            holder.txtState.setText("Quiet.");
////            holder.itemView.setBackgroundColor(Color.parseColor("#8eaee6")); //quiet #8eaee6
//            holder.txtState.setText("Normal");
//            holder.itemView.setBackgroundColor(Color.parseColor("#a0ed8e")); //normal
//        }
//        if (position == 1) {
//            holder.txtState.setText("Busy");
//            holder.itemView.setBackgroundColor(Color.parseColor("#fa8484")); //busy
//        }
//        if (position == 2) {
//            holder.txtState.setText("Normal");
//            holder.itemView.setBackgroundColor(Color.parseColor("#a0ed8e")); //normal
//        }
        
        
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView txtTitle, txtState;
        ImageView imgIcon;//,imgGraph;
        GraphView graphTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtState = itemView.findViewById(R.id.txtState);
            imgIcon = itemView.findViewById(R.id.imgIcon);
//            imgGraph = itemView.findViewById(R.id.imgGraph);
            graphTime = (GraphView) itemView.findViewById(R.id.graphTime);
        }
    }
}
