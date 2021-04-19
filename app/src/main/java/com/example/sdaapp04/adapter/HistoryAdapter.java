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
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder>{
    private static final String TAG = "HistoryAdapter";
    
    private Context mContext;
    private int mPosition;
    private ArrayList<EnvironmentalDatas> dataList;
    
    public HistoryAdapter(Context context, ArrayList<EnvironmentalDatas> dataList) {
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
    
    public void addData(EnvironmentalDatas datas) {
        dataList.add(datas);
        Log.d(TAG, "addData: datas.toString = " + datas.toString());
        this.notifyItemInserted(dataList.size() - 1);
    }
    
    public void addDatas(ArrayList<EnvironmentalDatas> dataList) {
        int size = dataList.size() - 1;
        if (size < 0) {
            size = 0;
        }
        dataList.addAll(dataList);
        this.notifyItemRangeInserted(size, dataList.size());
    }
    
    public void clean() {
        dataList.clear();
        notifyDataSetChanged();
    }
    
    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView temperature;
        private TextView illuminance;
        private TextView humidity;
        private int sPosition;
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        
        public void setsPosition(int position) {
            this.sPosition = position;
            initView();
            initData();
        }
        
        public void initView(){
            temperature = itemView.findViewById(R.id.temperature);
            illuminance = itemView.findViewById(R.id.illuminance);
            humidity = itemView.findViewById(R.id.humidity);
        }
    
        public void initData(){
            if (dataList.size() > 0) {
                temperature.setText(dataList.get(sPosition).getsTemperature() + "");
                illuminance.setText(dataList.get(sPosition).getsIlluminance() + "");
                humidity.setText(dataList.get(sPosition).getsHumidity() + "");
            }
        }
    }
}
