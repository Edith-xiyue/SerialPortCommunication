package com.example.sdaapp04;

public class EnvironmentalDatas {
    private static final byte NOTREADDATA = 0X00;
    private static final byte ENVIRONMENTALDATA = 0X01;
    private static final byte LEDSTATEDATA = 0X02;
    private static final byte DEVICERESET = 0X03;
    private int mTemperature = -999;
    private int mHumidity = -999;
    private int mIlluminance = -999;
    private byte dataType = NOTREADDATA;
    private int sLedState = -999;
    private String mTime = "";
    
    public static byte getNOTREADDATA() {
        return NOTREADDATA;
    }
    
    public static byte getENVIRONMENTALDATA() {
        return ENVIRONMENTALDATA;
    }
    
    public static byte getLEDSTATEDATA() {
        return LEDSTATEDATA;
    }
    
    public static byte getDEVICERESET() {
        return DEVICERESET;
    }
    
    public int getsLedState() {
        return sLedState;
    }
    
    public void setsLedState(int sLedState) {
        this.sLedState = sLedState;
    }
    
    public byte getDataType() {
        return dataType;
    }
    
    public void setDataType(byte dataType) {
        this.dataType = dataType;
    }
    
    public int getsTemperature() {
        return mTemperature;
    }
    
    public void setsTemperature(int sTemperature) {
        this.mTemperature = sTemperature;
    }
    
    public int getsHumidity() {
        return mHumidity;
    }
    
    public void setsHumidity(int sHumidity) {
        this.mHumidity = sHumidity;
    }
    
    public int getsIlluminance() {
        return mIlluminance;
    }
    
    public void setsIlluminance(int sIlluminance) {
        this.mIlluminance = sIlluminance;
    }
    
    public String getmTime() {
        return mTime;
    }
    
    public void setmTime(String mTime) {
        this.mTime = mTime;
    }
    
    @Override
    public String toString() {
        return "\nEnvironmentalDatas{" +
                       "\nmTemperature = " + mTemperature +
                       ", \nmHumidity = " + mHumidity +
                       ", \nmIlluminance = " + mIlluminance +
                       ", \nmTime = " + mTime +
//                       ", \nmLEDState = " + mLEDState +
                       "\n}";
    }
}
