package com.example.sdaapp04.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sdaapp04.EnvironmentalDatas;
import com.example.sdaapp04.R;
import com.example.sdaapp04.SDAApplication;
import com.example.sdaapp04.adapter.HistoryAdapter;

import java.util.ArrayList;

public class HistoryActivity  extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";
    private RecyclerView recyclerView;
    private static HistoryAdapter adapter;
    private Button back;
    
    public static HistoryAdapter getAdapter() {
        if (adapter == null) {
            adapter = new HistoryAdapter(SDAApplication.getContext(),new ArrayList<>());
        }
        return adapter;
    }
    
    public static void setAdapter(HistoryAdapter adapter) {
        HistoryActivity.adapter = adapter;
    }
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Log.d(TAG, "onCreate: 界面初始化");
        initView();
        initClick();
    }
    
    private void initView() {
        recyclerView = findViewById(R.id.history_recyclerView);
        back = findViewById(R.id.back_main);
        adapter = new HistoryAdapter(this,new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        EnvironmentalDatas datas = new EnvironmentalDatas();
    }
    
    private void initClick() {
        back.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
//                MainActivity.comeActivity(HistoryActivity.this);
            }
        });
    }
    
    public static void comeActivity(Activity activity) {
        Intent intent = new Intent(activity,HistoryActivity.class);
        activity.startActivity(intent);
    }
}
