package com.rtk.bdtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;

import android.app.Application;
import android.database.Cursor;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.rtk.bdtest.SerialPort;

public class ZigbeeApplication extends Application {
	private SerialPort mBDSerialPort = null;
	private SerialPort mZigbeeSerialPort = null;
	private static String mName;
	private static String Key = null;
	private static final String Tag = "ZigbeeApplication";

	public SerialPort getBDSerialPort() throws SecurityException, IOException,
			InvalidParameterException {
		/* Open the serial port */
		int bdr = 9600;
		try {
			mBDSerialPort = new SerialPort(getApplicationContext(), new File(
					"/dev/ttyMT0"), bdr, 0);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return mBDSerialPort;
	}

	public SerialPort getZigbeeSerialPort() throws SecurityException,
			IOException, InvalidParameterException {
		int bdr = 115200;
		try {
			mZigbeeSerialPort = new SerialPort(getApplicationContext(),
					new File("/dev/ttyMT1"), bdr, 0);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return mZigbeeSerialPort;
	}

	public void closeSerialPort() {
		if (mBDSerialPort != null) {
			mBDSerialPort.close();
			mBDSerialPort = null;
		}
		if(mZigbeeSerialPort != null){
			mZigbeeSerialPort.close();
			mZigbeeSerialPort = null;
		}
	}

	public static void setName(String name) {
		mName = name;
	}

	public static String getName() {
		return mName;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);
		readKey();
	}
    public void readKey() {
		File SDFile = android.os.Environment.getExternalStorageDirectory();  
	    String path= SDFile.getAbsolutePath()+File.separator+ "key.txt";
	    Log.d(Tag,"key file path is : " + path);
	    try{
	        FileInputStream fileIS = new FileInputStream(path);
	        BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS,"GB2312"));
	        String readString = new String();
	        while((readString = buf.readLine())!= null){
	                    Log.d(Tag ,"line: " + readString);
	                    setKey(readString);
	        }
	        fileIS.close();
	        } catch (FileNotFoundException e) {
	               e.printStackTrace();
	        } catch (IOException e){
	                e.printStackTrace();
	        }
    }

	public String getKey() {
		return Key;
	}

	public static void setKey(String key) {
		Key = key;
	}
}
