package com.example.sdaapp04.util;

import android.util.Log;


import com.example.sdaapp04.EnvironmentalDatas;
import com.example.sdaapp04.SDAApplication;

import java.util.ArrayList;

public class ByteUtil {
    private static final String TAG = "ByteUtil";
    private static boolean readEnd = true;
    private static boolean save = false;
    private static byte[] incompleteDataByte = SDAApplication.getIncompleteDataByte();
    private static boolean combinationSucceed = false;
    
    public static int isOdd(int num) {
        return num & 0x1;
    }
    
    public static int HexToInt(String inHex) {
        return Integer.parseInt(inHex, 16);
    }
    
    public static byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }
    
    public static String Byte2Hex(Byte inByte) {
        return String.format("%02x", new Object[]{inByte}).toUpperCase();
    }
    
    public static String ByteArrToDisposeHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        Log.d(TAG, "getCheckSum: inBytArr.length = " + j);
        for (int i = 0; i < j; i++) {
            strBuilder.append(Byte2Hex(Byte.valueOf(inBytArr[i])));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }
    
    public static ArrayList<byte[]> DivisionByteArr(byte[] inBytArr) {
        ArrayList<byte[]> bytes = new ArrayList<>();
        byte[] disposeByte = new byte[1024];
        int k = 0;
        if (!readEnd && incompleteDataByte.length != 0) {
            for (int i = 0; i < incompleteDataByte.length; i++) {
                disposeByte[k] = incompleteDataByte[i];
                k++;
                combinationSucceed = false;
            }
        }
        int j = inBytArr.length;
        Log.d(TAG, "DivisionByteArr: disposeByte = " + ByteArrToDisposeHex(disposeByte));
        Log.d(TAG, "DivisionByteArr: inBytArr = " + ByteArrToDisposeHex(inBytArr));
        EnvironmentalDatas environmentalDatas = new EnvironmentalDatas();
        for (int i = 0; i < j; i++) {
            if (inBytArr[i] == 0x01) {
                save = true;
                readEnd = false;
            }
            if (save) {
//                if (inBytArr[i] == 0x02) {
//                    if (i + 1 < j) {
//                        i++;
//                        disposeByte[k] = (byte) (inBytArr[i] ^ 0x10);
//                        Log.d(TAG, "DivisionByteArr: k = " + k);
//                    } else {
//                        return bytes;
//                    }
//                } else {
                disposeByte[k] = inBytArr[i];
//                }
                k++;
                if (inBytArr[i] == 0x03) {
                    byte[] zeroByte = new byte[k];
                    for (int l = 0; l < k; l++) {
                        zeroByte[l] = disposeByte[l];
                    }
                    bytes.add(zeroByte);
                    disposeByte = new byte[1024];
                    k = 0;
                    readEnd = true;
                    save = false;
                }
            }
        }
        if (!readEnd) {
            incompleteDataByte = new byte[k];
            for (int i = 0; i < k; i++) {
                incompleteDataByte[i] = disposeByte[i];
            }
            SDAApplication.setIncompleteDataByte(incompleteDataByte);
        }
        return bytes;
    }
    
    public static ArrayList<byte[]> AnalysisByteArr(ArrayList<byte[]> inBytArr) {
        ArrayList<byte[]> analysisBytes = new ArrayList<>();
        for (byte[] dataByte : inBytArr) {
            Log.d(TAG, "AnalysisByteArr: dataByte = " + ByteArrToDisposeHex(dataByte));
            int dataByteLength = dataByte.length;
            int k = 0;
            byte[] originalZeroBytes = new byte[dataByteLength];
            for (int i = 0; i < dataByteLength; i++) {
                if (dataByte[i] == (byte) 0x02) {
                    originalZeroBytes[k] = (byte) (dataByte[i + 1] ^ 0x10);
                    Log.d(TAG, "AnalysisByteArr: originalZeroBytes[" + i + "]" + originalZeroBytes[i]);
                    i++;
                } else {
                    originalZeroBytes[k] = (byte) dataByte[i];
                }
                k++;
            }
            byte[] originalBytes = new byte[k];
            for (int i = 0; i < k; i++) {
                originalBytes[i] = originalZeroBytes[i];
            }
            analysisBytes.add(originalBytes);
            Log.d(TAG, "AnalysisByteArr: originalBytes = " + ByteArrToDisposeHex(originalBytes));
        }
        return analysisBytes;
    }
    
    public static EnvironmentalDatas getDatas(byte[] inBytArr, String time, byte[] headByte) {
        ArrayList<byte[]> divisionByteList = DivisionByteArr(inBytArr);
//        byte[] headByte = LongColumnHexToByteArr(headStr);
        ArrayList<byte[]> originalByteList = AnalysisByteArr(divisionByteList);
        EnvironmentalDatas datas = new EnvironmentalDatas();
        datas.setmTime(time);
        if (originalByteList.size() > 0) {
            for (byte[] dataByte : originalByteList) {
                if (dataByte[1] == (byte) 0x00 && dataByte[2] == (byte) 0x4d) {
                    if (dataByte[6] == headByte[0] && dataByte[7] == headByte[1]) {
                        Log.d(TAG, "getDatas: reset");
                        datas.setDataType(EnvironmentalDatas.getDEVICERESET());
                    }
                }
                if (dataByte[7] == headByte[0] && dataByte[8] == headByte[1]) {
                    if (dataByte[1] == (byte) 0x81 && dataByte[2] == (byte) 0x02) {
                        if (dataByte[10] == (byte) 0x04) {
                            if (dataByte[11] == (byte) 0x05) {
                                datas.setDataType(EnvironmentalDatas.getENVIRONMENTALDATA());
                                datas.setsHumidity(((dataByte[17] & 0xFF) * 16 * 16) + (dataByte[18] & 0xFF));
                            } else if (dataByte[11] == (byte) 0x02) {
                                datas.setDataType(EnvironmentalDatas.getENVIRONMENTALDATA());
                                datas.setsTemperature(((dataByte[17] & 0xFF) * 16 * 16) + (dataByte[18] & 0xFF));
                            } else if (dataByte[11] == (byte) 0x00) {
                                datas.setDataType(EnvironmentalDatas.getENVIRONMENTALDATA());
                                datas.setsIlluminance(((dataByte[17] & 0xFF) * 16 * 16) + (dataByte[18] & 0xFF));
                            }
                        }
                    } else if (dataByte[1] == (byte) 0x81 && dataByte[2] == (byte) 0x01) {
                        if (dataByte[13] == (byte) 0x01 || dataByte[13] == (byte) 0x02 || dataByte[13] == (byte) 0x00) {
                            datas.setDataType(EnvironmentalDatas.getLEDSTATEDATA());
                            datas.setsLedState(dataByte[13]);
                        }
                    }
                    Log.d(TAG, "getDatas: " + ByteArrToDisposeHex(dataByte) + "++++++++++++++++++++++++++++++++++++++");
                }
                Log.d(TAG, "getDatas: " + ByteArrToDisposeHex(dataByte) + "---------------------------------");
            }
        }
        return datas;
    }
    
    public static ArrayList<String> ByteArrToOriginalHexList(byte[] inBytArr, String time) {
        ArrayList<String> hexList = new ArrayList<>();
        ArrayList<byte[]> bytes = DivisionByteArr(inBytArr);
        for (int i = 0; i < bytes.size(); i++) {
            byte[] data = bytes.get(i);
            hexList.add(time + " : " + ByteArrToDisposeHex(data));
        }
        return hexList;
    }
    
    public static byte hexBitwiseXOR(byte bValue, byte bKey) {
        byte bResult;
        
        bResult = (byte) (bValue ^ bKey);
        return bResult;
    }
    
    public static byte hexBitwiseAND(byte bValue, byte bKey) {
        byte bResult;
        bResult = (byte) (bValue & bKey);
        return bResult;
    }
    
    
    public static String ByteArrToDisposeHex(byte[] inBytArr, int offset, int byteCount) {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            strBuilder.append(Byte2Hex(Byte.valueOf(inBytArr[i])));
        }
        return strBuilder.toString();
    }
    
    public static byte[] LongColumnHexToByteArr(String inHex) {
        byte[] result;
        StringBuffer buffer = new StringBuffer();
        String[] strs;
        int strLength = inHex.length();
        Log.d(TAG, "LongColumnHexToByteArr: strLength " + strLength);
        if (inHex.contains(" ")) {
            strs = inHex.split(" ");
        } else {
            strs = new String[strLength / 2];
            for (int i = 0; i < strLength; i += 2) {
                strs[i / 2] = inHex.substring(i, i + 2);
            }
        }
        
        for (String str : strs) {
            buffer.append(str);
            Log.d(TAG, "LongColumnHexToByteArr: buffer = " + buffer.toString());
        }
        int hexlen = buffer.length();
        Log.d(TAG, "LongColumnHexToByteArr: buffer = " + buffer.toString());
        result = new byte[hexlen / 2];
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(buffer.substring(i, i + 2));
            j++;
        }
        Log.d(TAG, "LongColumnHexToByteArr: result = " + ByteArrToDisposeHex(result));
        return result;
    }
    
    public static byte getOnOffWithNoEffectsCheckSum(byte[] msgTypeBytes, byte[] lengthBytes, byte addressModeByte,
                                                     byte[] targetAddressBytes, byte sourceEndpointByte,
                                                     byte destinationEndpointByte, byte commandIdByte) {
        byte checkSum = 0x00;
        checkSum = (byte) (checkSum ^ msgTypeBytes[0]);
        checkSum = (byte) (checkSum ^ msgTypeBytes[1]);
        
        checkSum = (byte) (checkSum ^ lengthBytes[0]);
        checkSum = (byte) (checkSum ^ lengthBytes[1]);
        
        checkSum = (byte) (checkSum ^ addressModeByte);
        
        checkSum = (byte) (checkSum ^ targetAddressBytes[0]);
        checkSum = (byte) (checkSum ^ targetAddressBytes[1]);
        
        checkSum = (byte) (checkSum ^ sourceEndpointByte);
        
        checkSum = (byte) (checkSum ^ destinationEndpointByte);
        
        checkSum = (byte) (checkSum ^ commandIdByte);
        Log.d(TAG, "getCheckSum: checkSum = " + checkSum);
        return checkSum;
    }
    
    public static byte[] getSendOnOffWithNoEffectsHex(String addressMode, String targetAddress, String sourceEndpoint, String destinationEndpoint, String commandId) {
        Log.d(TAG, "getSendOnOffWithNoEffectsHex: start");
        byte[] msgTypeBytes = TwoHexToByteArr("00 92");
        byte[] lengthBytes = TwoHexToByteArr("00 06");
        byte[] targetAddressBytes = TwoHexToByteArr(targetAddress);
        byte addressModeByte = HexToByte(addressMode);
        byte sourceEndpointByte = HexToByte(sourceEndpoint);
        byte destinationEndpointByte = HexToByte(destinationEndpoint);
        byte commandIdByte = HexToByte(commandId);
        byte checkSum = getOnOffWithNoEffectsCheckSum(msgTypeBytes, lengthBytes, addressModeByte,
                targetAddressBytes, sourceEndpointByte, destinationEndpointByte, commandIdByte);
        StringBuffer hexBuffer = new StringBuffer();
        hexBuffer.append("00920006" + Byte2Hex(checkSum) + Byte2Hex(addressModeByte) + targetAddress + Byte2Hex(sourceEndpointByte) + Byte2Hex(destinationEndpointByte) + Byte2Hex(commandIdByte));
        byte[] hex = LongColumnHexToByteArr(hexBuffer.toString());
        Log.d(TAG, "getSendOnOffWithNoEffectsHex: end\nhex = " + ByteArrToDisposeHex(hex));
        
        return hex;
    }
    
    public static byte[] ZigbeeEncapsulation(byte[] originalBytes) {
        Log.d(TAG, "ZigbeeEncapsulation: start \n originalBytes = " + ByteArrToDisposeHex(originalBytes));
        byte[] bytes = new byte[1024];
        bytes[0] = 0x01;
        int j = 1;
        for (int i = 0; i < originalBytes.length; i++) {
            if ((originalBytes[i] & 0xFF) < 0x10) {
                bytes[j] = (byte) 0x02;
                j++;
                bytes[j] = (byte) (originalBytes[i] ^ 0x10);
                j++;
            } else {
                bytes[j] = originalBytes[i];
                j++;
            }
        }
        byte[] encapsulationBytes = new byte[j + 1];
        if (j >= originalBytes.length) {
            for (int i = 0; i < j; i++) {
                encapsulationBytes[i] = bytes[i];
            }
            encapsulationBytes[j] = 0x03;
            Log.d(TAG, "ZigbeeEncapsulation: end \n" + ByteArrToDisposeHex(encapsulationBytes));
        } else {
            Log.d(TAG, "ZigbeeEncapsulation: defeated");
            return null;
        }
        return encapsulationBytes;
    }
    
    public static byte[] TwoHexToByteArr(String byteString) {
        if (byteString.length() == 4) {
            String regex = "(.{2})";
            byteString = byteString.replaceAll(regex, "$1 ");
        }
        byte[] msgTypeBytes = LongColumnHexToByteArr(byteString);
        return msgTypeBytes;
    }
    
    public static String AddSpacesForEveryTwoStrings(String str) {
        String regex = "(.{2})";
        str = str.replaceAll(regex, "$1 ");
        return str;
    }
}