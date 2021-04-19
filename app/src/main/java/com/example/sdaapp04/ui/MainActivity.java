package com.example.sdaapp04.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sdaapp04.SerialPortUtil;
import com.example.sdaapp04.service.MyService;
import com.example.sdaapp04.util.ByteUtil;
import com.example.sdaapp04.util.ComBean;
import com.example.sdaapp04.EnvironmentalDatas;
import com.example.sdaapp04.R;
import com.example.sdaapp04.adapter.LogListAdapter;
import com.example.sdaapp04.adapter.SpAdapter;
import com.example.sdaapp04.util.FileWatcher;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //    private static RecyclerView recy;
    private static SerialPortUtil serialPortUtil;
    private static LogListAdapter logListAdapter;
    
    private static EnvironmentalDatas datas;
    private static boolean sSerialPortIsStart = false;
    private static boolean fileIsReady = false;
    private static boolean sLEDIsOpen = false;
    private static boolean sSDAIsOpen = false;
    private int sKeyCodeDelayJudgeTime = 3 * 1000;
    private static int mTemperatureNumber = -999;
    private static int mHumidityNumber = -999;
    private static int mIlluminanceNumber = -999;
    private static long sF1KeyDownTime = 0;
    private static long sF1KeyUpTime = 0;
    private static StringBuffer mTemperature = new StringBuffer();
    private static StringBuffer mHumidity = new StringBuffer();
    private static StringBuffer mIlluminance = new StringBuffer();
    private static byte[] headByte;
    private static String sTargetShortAddressValue = "";
    private static String sKeyCodeDelayJudgeTimeStr;
    private static String sAddressCommandIdValue = "01";
    private static String sAddressModeValue = "02";
    private static String sSourceEndpointValue = "01";
    private static String sDestinationEndpointValue = "01";
    private static String mTemperatureUnit = "℃";
    private static String mHumidityUnit = "%";
    private static String mIlluminanceUnit = "lx";
    private static FileWatcher testFileObserver;
    
    private TextView txPeopleNumber;
    private Button btToggleLED;
    private Button btSDASwitch;
    private Button btLEDSwitch;
    private EditText spAddressName;
    private Spinner spAddressCommand;
    private TextView temperature;
    private TextView illuminance;
    private TextView humidity;
    private TextView txLEDState;
    private static File SDAFile;
    private fileIsExistsThread mThread;
    //    private Spinner spSerial;
//    private Button history;
//    private Spinner spBote;
//    private Spinner spDatab;
//    private Spinner spParity;
//    private Spinner spStopb;
//    private Spinner spFlowcon;
    
    private SharedPreferences mPreference;
    private SharedPreferences.Editor mEditor;
    private static MyHandler handler;
    private static CountDownLatch mLatch;
    
    private class fileIsExistsThread extends Thread{
        private fileIsExistsThread() {
        }
        
        public void run() {
            super.run();
//            File file = new File(serialPortUtil.getPort());
            Log.d(TAG, "open: " + SDAFile.exists());
            while (!isInterrupted()) {
                if (SDAFile.exists() && !fileIsReady) {
                    Message message = new Message();
                    message.what = 3;
                    MainActivity.getHandler().sendMessage(message);
                    fileIsReady = true;
                    Log.d(TAG, "run: fileIsReady1 = " + fileIsReady);
                } else if (!SDAFile.exists()) {
                    Message message = new Message();
                    message.what = 2;
                    MainActivity.getHandler().sendMessage(message);
                    fileIsReady = false;
                    Log.d(TAG, "!!!!!!!!!!!!!!!!");
                    SystemClock.sleep(2000);
                }
            }
        }
    };
    
    public static Handler getHandler() {
        return handler;
    }
    
    public static void setHandler(Handler handler) {
        handler = handler;
    }
    
    public static boolean issSerialPortIsStart() {
        return sSerialPortIsStart;
    }
    
    public static void setsSerialPortIsStart(boolean sSerialPortIsStart) {
        MainActivity.sSerialPortIsStart = sSerialPortIsStart;
    }
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        EventBus.getDefault().register(this);
        initView();
        initData();
        initSpinner();
        initClick();
        initConfig(this);
    }
    
    
    public void initValue() {
        sLEDIsOpen = false;
        fileIsReady = false;
        sSDAIsOpen = false;
        mTemperatureNumber = -999;
        mHumidityNumber = -999;
        mIlluminanceNumber = -999;
        sF1KeyDownTime = 0;
        sF1KeyUpTime = 0;
        mTemperature.setLength(0);
        mHumidity.setLength(0);
        mIlluminance.setLength(0);
        sTargetShortAddressValue = "";
        sAddressCommandIdValue = "01";
        sAddressModeValue = "02";
        sSourceEndpointValue = "01";
        sDestinationEndpointValue = "01";
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 开始");
        if (mThread != null) {
            mThread.interrupt();
        }
        if (sSerialPortIsStart) {
            serialPortUtil.close();
            sSerialPortIsStart = false;
        }
        initValue();
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "onDestroy: 结束");
//        EventBus.getDefault().unregister(this);
    }
    
    public void initView() {
//        recy = (RecyclerView) findViewById(R.id.main_recyclerView);
        txLEDState = (TextView) findViewById(R.id.LED_state);
        btToggleLED = (Button) findViewById(R.id.btn_send_hex);
        btSDASwitch = (Button) findViewById(R.id.btn_sda_switch);
        temperature = (TextView) findViewById(R.id.temperature);
        illuminance = (TextView) findViewById(R.id.illuminance);
        humidity = (TextView) findViewById(R.id.humidity);
        btSDASwitch.setVisibility(View.VISIBLE);
        btLEDSwitch = (Button) findViewById(R.id.btn_led_switch);
        btLEDSwitch.setVisibility(View.VISIBLE);
        txPeopleNumber = (TextView) findViewById(R.id.people_number);
        spAddressCommand = (Spinner) findViewById(R.id.address_command);
        spAddressName = (EditText) findViewById(R.id.address_name);
        InputFilter[] filters = {new InputFilter.LengthFilter(4)};
        spAddressName.setFilters(filters);
//        spSerial = (Spinner) findViewById(R.id.sp_serial);
//        spBote = (Spinner) findViewById(R.id.sp_baudrate);
//        history = (Button) findViewById(R.id.history);
//        spDatab = (Spinner) findViewById(R.id.sp_databits);
//        spParity = (Spinner) findViewById(R.id.sp_parity);
//        spStopb = (Spinner) findViewById(R.id.sp_stopbits);
//        spFlowcon = (Spinner) findViewById(R.id.sp_flowcon);

//        logListAdapter = new LogListAdapter(this, new ArrayList<>());

//        recy.setLayoutManager(new LinearLayoutManager(this));
//        recy.setAdapter(logListAdapter);
//
//        recy.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
    
    public void initSpinner() {
        final String[] command = new String[]{"Off", "On", "Toggle"};
        
        SpAdapter commanAdapter = new SpAdapter(this);
        commanAdapter.setDatas(command);
        spAddressCommand.setAdapter(commanAdapter);
        
        spAddressCommand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sAddressCommandIdValue = Integer.toHexString(position);
                Log.d(TAG, "onItemSelected: sAddressCommandIdValue = " + sAddressCommandIdValue);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            
            }
        });
//        final String[] paths = serialPortFinder.getAllDevicesPath();
//        final String[] botes = new String[]{"0", "50", "75", "110", "134", "150", "200", "300", "600", "1200", "1800", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400", "460800", "500000", "576000", "921600", "1000000", "1152000", "1500000", "2000000", "2500000", "3000000", "3500000", "4000000"};
//        final String[] databits = new String[]{"8", "7", "6", "5"};
//        final String[] paritys = new String[]{"NONE", "ODD", "EVEN"};
//        final String[] stopbits = new String[]{"1", "2"};
//        final String[] flowcons = new String[]{"NONE", "RTS/CTS", "XON/XOFF"};
//
//        SpAdapter pathsAdapter = new SpAdapter(this);
//        pathsAdapter.setDatas(paths);
//        spSerial.setAdapter(pathsAdapter);
//
//        spSerial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                serialPortUtil.stop();
//                serialPortUtil.setsPath(paths[position]);
//                Log.d(TAG, "run: 停止接收");
//                btOpen.setText("开始接收");
//                serialPortUtil.setmStopRead(false);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        SpAdapter boteAdapter = new SpAdapter(this);
//        boteAdapter.setDatas(botes);
//        spBote.setAdapter(boteAdapter);
//
//        spBote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                serialPortUtil.stop();
//                serialPortUtil.setsBaudRate(Integer.parseInt(botes[position]));
//                Log.d(TAG, "run: 停止接收");
//                btOpen.setText("开始接收");
//                serialPortUtil.setmStopRead(false);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        SpAdapter databitAdapter = new SpAdapter(this);
//        databitAdapter.setDatas(databits);
//        spDatab.setAdapter(databitAdapter);
//
//        spDatab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                serialPortUtil.stop();
//                serialPortUtil.setsDataBits(Integer.parseInt(databits[position]));
//                Log.d(TAG, "run: 停止接收");
//                btOpen.setText("开始接收");
//                serialPortUtil.setmStopRead(false);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        SpAdapter parityAdapter = new SpAdapter(this);
//        parityAdapter.setDatas(paritys);
//        spParity.setAdapter(parityAdapter);
//
//        spParity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                serialPortUtil.stop();
//                serialPortUtil.setsParity(position);
//                Log.d(TAG, "run: 停止接收");
//                btOpen.setText("开始接收");
//                serialPortUtil.setmStopRead(false);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        SpAdapter stopbitAdapter = new SpAdapter(this);
//        stopbitAdapter.setDatas(stopbits);
//        spStopb.setAdapter(stopbitAdapter);
//
//        spStopb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                serialPortUtil.stop();
//                serialPortUtil.setsStopBits(Integer.parseInt(stopbits[position]));
//                Log.d(TAG, "run: 停止接收");
//                btOpen.setText("开始接收");
//                serialPortUtil.setmStopRead(false);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        SpAdapter flowconAdapter = new SpAdapter(this);
//        flowconAdapter.setDatas(flowcons);
//        spFlowcon.setAdapter(flowconAdapter);
//
//        spFlowcon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                serialPortUtil.stop();
//                serialPortUtil.setsFlowCon(position);
//                Log.d(TAG, "run: 停止接收");
//                btOpen.setText("开始接收");
//                serialPortUtil.setmStopRead(false);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
    }
    
    public void initData() {
        handler = new MyHandler(this);
        serialPortUtil = new SerialPortUtil();
        SDAFile = new File(serialPortUtil.getPort());
//        if (SDAFile.exists()) {
//            try {
//                serialPortUtil.open();
//            }catch (IOException e) {
//                Log.e(TAG, "initData: ", e);
//            }
//        }else {
        btLEDSwitch.setEnabled(false);
        btSDASwitch.setEnabled(false);
        mThread = new fileIsExistsThread();
        mThread.start();
//        }
//        sThreadStart = true;
//        new Thread(mRunnable).start();
        Intent startIntent = new Intent(this, MyService.class);
        this.startService(startIntent);
//        serialPortUtil.open();
//        testFileObserver.stopWatching();
    
        
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: 开始");
        this.finish();
    }
    
    public void initClick() {
//        history.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                HistoryActivity.comeActivity(MainActivity.this);
//            }
//        });

//        btToggleLED.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                sTargetShortAddressValue = String.valueOf(spAddressName.getText());
//                if (sTargetShortAddressValue.isEmpty() || sTargetShortAddressValue.length() != 4) {
//                    Toast.makeText(MainActivity.this, getString(R.string.toast_address_name_is_worning), Toast.LENGTH_SHORT).show();
//                } else {
//                    byte[] sendOnOffWithNoEffectsHex = byteUtil.getSendOnOffWithNoEffectsHex(sAddressModeValue,
//                            sTargetShortAddressValue,
//                            sSourceEndpointValue,
//                            sDestinationEndpointValue,
//                            sAddressCommandIdValue);
//                    byte[] bytes = byteUtil.ZigbeeEncapsulation(sendOnOffWithNoEffectsHex);
//                    serialPortUtil.send(bytes);
//                }
//            }
//        });
        
        btSDASwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sSerialPortIsStart) {
                    Log.d(TAG, "onClick: 开启串口");
                    btSDASwitch.setText(R.string.bt_sda_switch_close_str);
                    sSerialPortIsStart = true;
                    try {
                        serialPortUtil.open();
                        btLEDSwitch.setEnabled(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onClick: 关闭串口");
                    btSDASwitch.setText(R.string.bt_sda_switch_open_str);
                    serialPortUtil.close();
                    btLEDSwitch.setEnabled(false);
                    sSerialPortIsStart = false;
                }
            }
        });
        
        btLEDSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 发送文件。");
                Log.d(TAG, "updateUI: Integer.valueOf(sAddressCommandIdValue) = " + Integer.valueOf(sAddressCommandIdValue));
                if (Integer.valueOf(sAddressCommandIdValue) == 0) {
                    byte[] sendOnOffWithNoEffectsHex = ByteUtil.getSendOnOffWithNoEffectsHex(sAddressModeValue,
                            sTargetShortAddressValue,
                            sSourceEndpointValue,
                            sDestinationEndpointValue,
                            sAddressCommandIdValue);
                    byte[] bytes = ByteUtil.ZigbeeEncapsulation(sendOnOffWithNoEffectsHex);
                    serialPortUtil.send(bytes);
                    btLEDSwitch.setEnabled(false);
                    sAddressCommandIdValue = "01";
                } else if (Integer.valueOf(sAddressCommandIdValue) == 1) {
                    byte[] sendOnOffWithNoEffectsHex = ByteUtil.getSendOnOffWithNoEffectsHex(sAddressModeValue,
                            sTargetShortAddressValue,
                            sSourceEndpointValue,
                            sDestinationEndpointValue,
                            sAddressCommandIdValue);
                    byte[] bytes = ByteUtil.ZigbeeEncapsulation(sendOnOffWithNoEffectsHex);
                    serialPortUtil.send(bytes);
                    btLEDSwitch.setEnabled(false);
                    sAddressCommandIdValue = "00";
                }
            }
        });
    }
    
    public void updateUI(ComBean paramComBean) {
        datas = ByteUtil.getDatas(paramComBean.bRec, paramComBean.sRecTime, headByte);
//        bytes = byteUtil.ByteArrToOriginalHexList(paramComBean.bRec, paramComBean.sRecTime);
//        StringBuffer buffer = new StringBuffer();
//        for (String str : bytes) {
//            buffer.append("\n" + str);
//        }
//
//        Log.d(TAG, "run: byteUtil.isLEDStateData() = " + byteUtil.isLEDStateData());
//        Log.d(TAG, "run: " + getString(R.string.log_original_data_str) + buffer);
//        Log.d(TAG, "run: "/* + (logListAdapter == null) */ + "  environmentalDatas = null ? " + (environmentalDatas == null));
        Log.d(TAG, "updateUI: datas.getDataType() = " + datas.getDataType());
        if (datas.getDataType() == EnvironmentalDatas.getENVIRONMENTALDATA()) {
            Log.d(TAG, "run: environmentalDatas = " + datas.toString());
//                            logListAdapter.addData(environmentalDatas);
            if (datas.getsIlluminance() != -999) {
                mIlluminance.append(datas.getsIlluminance());
                mIlluminance.append(mIlluminanceUnit);
                illuminance.setText(mIlluminance);
                mIlluminance.setLength(0);
            }
            if (datas.getsHumidity() != -999) {
                mHumidity.append(datas.getsHumidity());
                mHumidity.append(mHumidityUnit);
                humidity.setText(mHumidity);
                mHumidity.setLength(0);
            }
            if (datas.getsTemperature() != -999) {
                mTemperature.append(datas.getsTemperature());
                mTemperature.append(mTemperatureUnit);
                temperature.setText(mTemperature);
                mTemperature.setLength(0);
            }
        } else if (datas.getDataType() == EnvironmentalDatas.getLEDSTATEDATA()) {
            if (datas.getsLedState() != -999) {
                if (datas.getsLedState() == 0x01) {
                    txLEDState.setText(R.string.led_open);
                    txLEDState.setTextColor(getColor(R.color.holo_green_light));
                    btLEDSwitch.setText(R.string.bt_led_switch_close_str);
                    sLEDIsOpen = true;
                    btLEDSwitch.setEnabled(true);
                }else if (datas.getsLedState() == 0x00) {
                    txLEDState.setText(R.string.led_close);
                    txLEDState.setTextColor(getColor(R.color.holo_red_dark));
                    btLEDSwitch.setText(R.string.bt_led_switch_open_str);
                    sLEDIsOpen = false;
                    btLEDSwitch.setEnabled(true);
                }else {
                    if (!sLEDIsOpen) {
                        txLEDState.setText(R.string.led_open);
                        txLEDState.setTextColor(getColor(R.color.holo_green_light));
                        btLEDSwitch.setText(R.string.bt_led_switch_close_str);
                        sLEDIsOpen = true;
                        btLEDSwitch.setEnabled(true);
                    } else {
                        txLEDState.setText(R.string.led_close);
                        txLEDState.setTextColor(getColor(R.color.holo_red_dark));
                        btLEDSwitch.setText(R.string.bt_led_switch_open_str);
                        sLEDIsOpen = false;
                        btLEDSwitch.setEnabled(true);
                    }
                }
            }
        } else if (datas.getDataType() == EnvironmentalDatas.getDEVICERESET()) {
            Log.d(TAG, "updateUI: Integer.valueOf(sAddressCommandIdValue) = " + Integer.valueOf(sAddressCommandIdValue));
            Log.d(TAG, "updateUI: sLEDIsOpen = " + sLEDIsOpen);
            if (sLEDIsOpen) {
                txLEDState.setText(R.string.led_close);
                txLEDState.setTextColor(getColor(R.color.holo_red_dark));
                btLEDSwitch.setText(R.string.bt_led_switch_open_str);
                sLEDIsOpen = false;
                sAddressCommandIdValue = "01";
                Log.d(TAG, "updateUI: Integer.valueOf(sAddressCommandIdValue) = " + Integer.valueOf(sAddressCommandIdValue));
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        setConfig(MainActivity.this);
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F1:
                sF1KeyDownTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "onKeyDown: sKeyCodeDelayJudgeTime = " + sKeyCodeDelayJudgeTime);
                if ((sF1KeyDownTime - sF1KeyUpTime) > sKeyCodeDelayJudgeTime) {
                    Log.d(TAG, "onKeyDown: " + getString(R.string.pir_detected_str));
                    txPeopleNumber.setText(R.string.pir_detected_str);
                    txPeopleNumber.setTextColor(getColor(R.color.holo_green_light));
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F1:
                sF1KeyUpTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "onKeyUp: ");
                Log.d(TAG, "onKeyDown: " + getString(R.string.pir_not_detected_str));
                txPeopleNumber.setText(R.string.pir_not_detected_str);
                txPeopleNumber.setTextColor(getColor(R.color.holo_red_dark));
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
    
    public static void comeActivity(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    
    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if ((v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                                                                                       + v.getWidth();
            if (event.getX() > left && event.getX() < right
                        && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }
    
    /**
     * 多种隐藏软件盘方法的其中一种
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    private static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivityReference;
        
        MyHandler(Activity activity) {
            this.mActivityReference = new WeakReference<Activity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = (MainActivity) mActivityReference.get();  //获取弱引用队列中的activity
            switch (msg.what) {    //获取消息，更新UI
                case 1:
                    activity.updateUI((ComBean) msg.obj);
                    break;
                case 2:
                    activity.btSDASwitch.setEnabled(false);
                    activity.btLEDSwitch.setEnabled(false);
                    if (sSerialPortIsStart) {
                        serialPortUtil.close();
                        activity.btSDASwitch.setText(activity.getString(R.string.bt_sda_switch_open_str));
                        sSerialPortIsStart = false;
                    }
                    Toast.makeText(activity, activity.getString(R.string.usb_not_found), Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Log.d(TAG, "handleMessage: btSDASwitch.setEnabled = true");
                    activity.btSDASwitch.setEnabled(true);
                    break;
            }
        }
    }
    
    private final String CONFIG_NAME = "SDA_CONFIG";
    private final String CONFIG_KEY_A = "CONFIG_A";
    private final String CONFIG_KEY_B = "CONFIG_B";
    
    private void initConfig(Context context) {
        mPreference = context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
        mEditor = mPreference.edit();
        sTargetShortAddressValue = mPreference.getString(CONFIG_KEY_A, "");
        sKeyCodeDelayJudgeTimeStr = mPreference.getString(CONFIG_KEY_B, "");
        if (TextUtils.isEmpty(sTargetShortAddressValue)) {
            sTargetShortAddressValue = "A96F";
            mEditor.putString(CONFIG_KEY_A, sTargetShortAddressValue);
            mEditor.commit();
        }
        
        if (TextUtils.isEmpty(sKeyCodeDelayJudgeTimeStr)) {
            sKeyCodeDelayJudgeTimeStr = "3";
            mEditor.putString(CONFIG_KEY_B, sKeyCodeDelayJudgeTimeStr);
            mEditor.commit();
        }
        sKeyCodeDelayJudgeTime = Integer.valueOf(sKeyCodeDelayJudgeTimeStr) * 1000;
        headByte = ByteUtil.LongColumnHexToByteArr(sTargetShortAddressValue);
    }
    
    private void setConfig(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_config_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                              .setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        
        final EditText view1 = view.findViewById(R.id.input_1);
        final EditText view2 = view.findViewById(R.id.input_2);
        final Button setBtn = view.findViewById(R.id.set);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sTargetShortAddressValue = view1.getText().toString().trim().toLowerCase();
                sKeyCodeDelayJudgeTimeStr = view2.getText().toString().trim().toLowerCase();
                Log.d(TAG, "Config: sTargetShortAddressValue = " + sTargetShortAddressValue
                                   + "\nsKeyCodeDelayJudgeTimeStr = " + sKeyCodeDelayJudgeTimeStr);
                if (sKeyCodeDelayJudgeTimeStr.length() == 0) {
                    sKeyCodeDelayJudgeTimeStr = "3";
                }
                headByte = ByteUtil.LongColumnHexToByteArr(sTargetShortAddressValue);
                sKeyCodeDelayJudgeTime = Integer.valueOf(sKeyCodeDelayJudgeTimeStr) * 1000;
                if (sSerialPortIsStart){
                    btLEDSwitch.setEnabled(true);
                    txLEDState.setText(R.string.led_close);
                    txLEDState.setTextColor(getColor(R.color.holo_red_dark));
                    btLEDSwitch.setText(R.string.bt_led_switch_open_str);
                    sLEDIsOpen = false;
                }
                sAddressCommandIdValue = "01";
                mEditor.putString(CONFIG_KEY_A, sTargetShortAddressValue);
                mEditor.putString(CONFIG_KEY_B, sKeyCodeDelayJudgeTimeStr);
                mEditor.commit();
                dialog.dismiss();
                dialog.cancel();
            }
        };
        
        if (!TextUtils.isEmpty(sTargetShortAddressValue)) {
            view1.setText(sTargetShortAddressValue);
            if (sTargetShortAddressValue.length() == 4) {
                setBtn.setOnClickListener(clickListener);
            } else {
//                Toast.makeText()
            }
        }
        if (!TextUtils.isEmpty(sKeyCodeDelayJudgeTimeStr)) {
            view2.setText(sKeyCodeDelayJudgeTimeStr);
        }
        view1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "s: " + s);
                if (s.length() == 4) {
                    setBtn.setOnClickListener(clickListener);
                } else {
                    setBtn.setOnClickListener(null);
                }
            }
        });
        
        dialog.show();
        Log.d(TAG, "Get config: sTargetShortAddressValue = " + sTargetShortAddressValue
                           + "\nsKeyCodeDelayJudgeTimeStr = " + sKeyCodeDelayJudgeTimeStr);
    }
    
    static class CountDownLatchTest {
        /**
         * 启动服务器
         */
        public static void startServer() throws Exception {
            System.out.println("Server is starting.");
            final CountDownLatch latch = new CountDownLatch(1);
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    System.out.println(" Start thread 1");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(" End thread 1");
                    latch.countDown();
                }
            }).start();
            latch.await();
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    System.out.println(" Start thread 2");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(" End thread 2");
                }
            }).start();
            System.out.println("Server is end!");
        }
        
        public static void main(String[] args) throws Exception {
            CountDownLatchTest.startServer();
        }
    }
    
    class TestFileObserver extends FileObserver {
        
        // path 为 需要监听的文件或文件夹
        public TestFileObserver(String path) {
            super(path, FileObserver.ALL_EVENTS);
        }
        
        @Override
        public void onEvent(int event, String path) {
            // 如果文件修改了 打印出文件相对监听文件夹的位置
            Log.d("edong", "event = " + event);
            if (event == FileObserver.CREATE) {
                Log.d("edong", path);
                try {
                    serialPortUtil.open();
                    stopWatching();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        @Override
        public void stopWatching() {
            super.stopWatching();
        }
    }
}

