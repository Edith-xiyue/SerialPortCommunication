package com.example.sdaapp04.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sdaapp04.EnvironmentalDatas;
import com.example.sdaapp04.R;

import java.util.ArrayList;

/**
 * Author
 * Created Time 2017/12/14.
 */
public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.MyViewHolder> {
    private static final String TAG = "LogListAdapter";
    
    private Context mContext;
    private int mPosition;
    private ArrayList<EnvironmentalDatas> dataList;
    
    public LogListAdapter(Context context, ArrayList<EnvironmentalDatas> dataList) {
        this.mContext = context;
        this.dataList = dataList;
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.history_item_layout, parent, false);
        MyViewHolder incomeRecycleHolder = new MyViewHolder(inflate);
        return incomeRecycleHolder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        this.mPosition = position;
        holder.setsPosition(position);
    }
    
    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size();
    }
    
//    public void addData(EnvironmentalDatas data) {
//        Log.d(TAG, "addData: 1");
//        if (data.getsTemperature().isEmpty()) {
//            Log.d(TAG, "addData: 2");
//            data.setsTemperature(mContext.getResources().getString(R.string.no_data_was_retrieved));
//        }
//        if (data.getsHumidity().isEmpty()) {
//            Log.d(TAG, "addData: 3");
//            data.setsHumidity(mContext.getResources().getString(R.string.no_data_was_retrieved));
//        }
//        if (data.getsIlluminance().isEmpty()) {
//            Log.d(TAG, "addData: 4");
//            data.setsIlluminance(mContext.getResources().getString(R.string.no_data_was_retrieved));
//        }
//        Log.d(TAG, "addData: 5");
//        dataList.add(data);
//        Log.d(TAG, "addData: str = " + data.toString());
//        this.notifyItemInserted(dataList.size() - 1);
//    }
    
    public void addDatas(ArrayList<EnvironmentalDatas> datas) {
        int size = dataList.size();
        Log.d(TAG, "addDatas: datas.toString: " + datas);
        dataList.addAll(datas);
        this.notifyItemRangeInserted(size, datas.size());
    }
    
    public ArrayList<EnvironmentalDatas> getData() {
        return dataList;
    }
    
    public void clean() {
        dataList.clear();
        notifyDataSetChanged();
    }
    
    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView temperature;
        private TextView illuminance;
        private TextView humidity;
        private TextView time;
        private int sPosition;
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        
        public void setsPosition(int position) {
            this.sPosition = position;
            initView();
            initData();
        }
        
        public void initView() {
            temperature = itemView.findViewById(R.id.temperature);
            illuminance = itemView.findViewById(R.id.illuminance);
            humidity = itemView.findViewById(R.id.humidity);
            time = itemView.findViewById(R.id.time);
        }
        
        public void initData() {
            temperature.setText(dataList.get(sPosition).getsTemperature());
            illuminance.setText(dataList.get(sPosition).getsIlluminance());
            humidity.setText(dataList.get(sPosition).getsHumidity());
            time.setText(dataList.get(sPosition).getmTime());
        }
//        private TextView textView;
//        private Context sContext;
//        private int sPosition;
//
//        public MyViewHolder(@NonNull View itemView,Context context) {
//            super(itemView);
//            this.sContext = context;
//        }
//
//        public void setsPosition(int position) {
//            this.sPosition = position;
//            initView();
//        }
//
//        public void initView(){
//            textView = itemView.findViewById(R.id.textView);
//            textView.setText(dataList.get(sPosition));
//        }
    }
}
