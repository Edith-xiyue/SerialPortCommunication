package com.example.sdaapp04;

import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.example.sdaapp04.ui.MainActivity;
import com.example.sdaapp04.util.ByteUtil;
import com.example.sdaapp04.util.ComBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

public class SerialPortUtil {
    private static final String TAG = "SerialPortUtil";
    
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private SendThread mSendThread;
    private static String sPort = "/dev/ttyUSB0";
    private int iBaudRate = 1000000;
    private int stopBits = 1;
    private int dataBits = 8;
    private int parity = 0;
    private int flowCon = 0;
    private int flags = 0;
    private boolean _isOpen = false;
    private byte[] _bLoopData = {48};
    private int iDelay = 500;
    private static boolean test = false;
    
    public SerialPort getmSerialPort() {
        return mSerialPort;
    }
    
    public void setmSerialPort(SerialPort mSerialPort) {
        this.mSerialPort = mSerialPort;
    }
    
    public SerialPortUtil() {
    }
    
    public void open()
            throws SecurityException, IOException, InvalidParameterException {
        Log.d(TAG, "open: 文件存在。");
        this.mSerialPort = new SerialPort( new File(sPort), this.iBaudRate, this.stopBits, this.dataBits, this.parity, this.flowCon, this.flags);
        
        this.mOutputStream = this.mSerialPort.getOutputStream();
        this.mInputStream = this.mSerialPort.getInputStream();
        this.mReadThread = new ReadThread();
        this.mReadThread.start();
        this.mSendThread = new SendThread();
        this.mSendThread.setSuspendFlag();
        this.mSendThread.start();
        this._isOpen = true;
        MainActivity.setsSerialPortIsStart(true);
    }
    
    public void close() {
        if (this.mReadThread != null) {
            this.mReadThread.interrupt();
        }
        if (this.mSerialPort != null) {
            this.mSerialPort.close();
            this.mSerialPort = null;
        }
        this._isOpen = false;
    }
    
    public void send(byte[] bOutArray) {
        try {
            if (this.mOutputStream != null) {
                this.mOutputStream.write(bOutArray);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendHaveSpacesHex(String sHex) {
        byte[] bOutArray = ByteUtil.LongColumnHexToByteArr(sHex);
        send(bOutArray);
    }
    
    public void sendNoSpacesHex(String sHex) {
        sHex = ByteUtil.AddSpacesForEveryTwoStrings(sHex);
        byte[] bOutArray = ByteUtil.LongColumnHexToByteArr(sHex);
        send(bOutArray);
    }
    
    public void sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }
    
    private class ReadThread
            extends Thread {
        private ReadThread() {
        }
        
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    int available = mInputStream.available();
                    Log.d(TAG, "run: available = " + available);
                    if (available > 0) {
                        byte[] buffer = new byte[1024];
                        
                        int size = 0;
                        try {
                            Log.d(TAG, "run: start read data");
                            size = mInputStream.read(buffer);
                            Log.d(TAG, "run: stop read data");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("error", e.getMessage());
                        }
//                        int size;
//                        String str1 = "01 81 02 12 02 10 02 1D C7 B8 A9 6F 02 11 02 14 02 12 02 10 02 10 29 02 10 02 12 02 10 1B 03 01 81 02 12 02 10 02 1D 74 B9 A9 6F 02 11 02 14 02 10 02 10 02 10 21 02 10 02 12 02 10 A3 03 01 81 02";
//                        buffer = ByteUtil.LongColumnHexToByteArr(str1);
//                        size = buffer.length;
                        Log.d(TAG, "run: read data size: " + size);
                        if (size > 0) {
                            ComBean comRecData1 = new ComBean(sPort, buffer, size);
                            Message message1 = new Message();
                            message1.what = 1;
                            message1.obj = comRecData1;
                            Log.d(TAG, "getDatas: originalBuffer = " + ByteUtil.ByteArrToDisposeHex(buffer));
                            MainActivity.getHandler().sendMessage(message1);
//                        String str2 = "12 02 10 02 1D CF B7 A9 6F 02 11 02 14 02 15 02 10 02 10 29 02 10 02 12 02 10 1B";
//                        buffer = ByteUtil.LongColumnHexToByteArr(str2);
//                        size = buffer.length;
//                        ComBean comRecData2 = new ComBean(sPort, buffer, size);
//                        Message message2 = new Message();
//                        message2.what = 1;
//                        message2.obj = comRecData2;
//                        MainActivity.getHandler().sendMessage(message2);
//                        String str3 = "03";
//                        buffer = ByteUtil.LongColumnHexToByteArr(str3);
//                        size = buffer.length;
//                        ComBean comRecData3 = new ComBean(sPort, buffer, size);
//                        Message message3 = new Message();
//                        message3.what = 1;
//                        message3.obj = comRecData3;
//                        MainActivity.getHandler().sendMessage(message3);
//                        onDataReceived(ComRecData);
                        }
                    } else {
                        Log.d(TAG, "run: not data");
                        SystemClock.sleep(50);
                    }
                } catch (Throwable e) {
                    Log.e("error", e.getMessage());
                    return;
                }
            }
        }
    }
    
    private class SendThread
            extends Thread {
        public boolean suspendFlag = true;
        
        private SendThread() {
        }
        
        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (this) {
                    while (this.suspendFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                send(getbLoopData());
                try {
                    Thread.sleep(iDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }
        
        public synchronized void setResume() {
            this.suspendFlag = false;
            notify();
        }
    }
    
    public int getBaudRate() {
        return this.iBaudRate;
    }
    
    public boolean setBaudRate(int iBaud) {
        if (this._isOpen) {
            return false;
        }
        this.iBaudRate = iBaud;
        return true;
    }
    
    public boolean setBaudRate(String sBaud) {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }
    
    public int getStopBits() {
        return this.stopBits;
    }
    
    public boolean setStopBits(int stopBits) {
        if (this._isOpen) {
            return false;
        }
        this.stopBits = stopBits;
        return true;
    }
    
    public int getDataBits() {
        return this.dataBits;
    }
    
    public boolean setDataBits(int dataBits) {
        if (this._isOpen) {
            return false;
        }
        this.dataBits = dataBits;
        return true;
    }
    
    public int getParity() {
        return this.parity;
    }
    
    public boolean setParity(int parity) {
        if (this._isOpen) {
            return false;
        }
        this.parity = parity;
        return true;
    }
    
    public int getFlowCon() {
        return this.flowCon;
    }
    
    public boolean setFlowCon(int flowCon) {
        if (this._isOpen) {
            return false;
        }
        this.flowCon = flowCon;
        return true;
    }
    
    public String getPort() {
        return this.sPort;
    }
    
    public boolean setPort(String sPort) {
        if (this._isOpen) {
            return false;
        }
        this.sPort = sPort;
        return true;
    }
    
    public boolean isOpen() {
        return this._isOpen;
    }
    
    public byte[] getbLoopData() {
        return this._bLoopData;
    }
    
    public void setbLoopData(byte[] bLoopData) {
        this._bLoopData = bLoopData;
    }
    
    public void setTxtLoopData(String sTxt) {
        this._bLoopData = sTxt.getBytes();
    }
    
    public void setHexLoopData(String sHex) {
        this._bLoopData = ByteUtil.LongColumnHexToByteArr(sHex);
    }
    
    public int getiDelay() {
        return this.iDelay;
    }
    
    public void setiDelay(int iDelay) {
        this.iDelay = iDelay;
    }
    
    public void startSend() {
        if (this.mSendThread != null) {
            this.mSendThread.setResume();
        }
    }
    
    public void stopSend() {
        if (this.mSendThread != null) {
            this.mSendThread.setSuspendFlag();
        }
    }
    
    
//
//    protected abstract void onDataReceived(ComBean paramComBean);
}
