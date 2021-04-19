package com.example.sdaapp04.util;

import android.os.FileObserver;
import android.util.Log;

/**
 * SD卡中的目录创建监听器。
 *
 * @author mayingcai
 */
public class FileWatcher extends FileObserver {
    private static final String TAG = "FileWatcher";
    public FileWatcher(String path) {
        /*
         * 这种构造方法是默认监听所有事件的,如果使用 super(String,int)这种构造方法，
         * 则int参数是要监听的事件类型.
         */
        super(path, FileObserver.ALL_EVENTS);
        Log.d(TAG, "FileWatcher: 初始化");
    }
    
    @Override
    public void onEvent(int event, String path) {
        switch(event) {
            case FileObserver.ALL_EVENTS:
                Log.d(TAG, "path:"+ path);
                break;
            case FileObserver.CREATE:
                Log.d(TAG, "path:"+ path);
                break;
        }
    }
}