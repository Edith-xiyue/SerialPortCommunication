package com.example.sdaapp04.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.sdaapp04.util.FileWatcher;

import java.io.File;
import java.io.IOException;

public class MyService extends Service {
    private static final String TAG = "MyService";
    FileWatcher testFileObserver;
    
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        String fileName = "test.txt";
        String filePath = "/data/local/tmp/test.txt";
        String path = "/data/local/tmp/";
        testFileObserver = new FileWatcher(path);
// 2.开始监听
        testFileObserver.startWatching();
        File folder = new File(path);
        File file = new File(filePath);
        if (file.exists()) {
            Log.d(TAG, "onCreate: 文件存在！");
            if (file.canWrite()) {
                Log.d(TAG, "onCreate: 文件可写！");
//                file.
                if (file.canRead()) {
                    Log.d(TAG, "onCreate: 文件可读！");
                    
                }
            }
        }else {
            Log.d(TAG, "onCreate: 文件不存在！");
            try {
                folder.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onCreate: 文件创建成功！");
        }
        super.onCreate();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    

}
