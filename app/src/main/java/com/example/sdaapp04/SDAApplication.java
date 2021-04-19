package com.example.sdaapp04;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

public class SDAApplication extends Application {
    
    private static byte[] incompleteDataByte = new byte[1024];
    private static Context context;
    private static ArrayList<EnvironmentalDatas> dataList;
    
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        dataList = new ArrayList<>();
    }
    
    public synchronized static void setIncompleteDataByte(byte[] incompleteDataByte) {
        SDAApplication.incompleteDataByte = incompleteDataByte;
    }
    
    public static byte[] getIncompleteDataByte() {
        return incompleteDataByte;
    }
   
    public static ArrayList<EnvironmentalDatas> getEnvironmentalDatas() {
        
        return dataList;
    }
    
    public static Context getContext() {
        return context;
    }
    
    public static void setContext(Context context) {
        SDAApplication.context = context;
    }
}
