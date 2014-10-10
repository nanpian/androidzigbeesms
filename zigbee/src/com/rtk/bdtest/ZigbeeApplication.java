package com.rtk.bdtest;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.rtk.bdtest.SerialPort;

public class ZigbeeApplication extends Application {
	private SerialPort mBDSerialPort = null;
	private SerialPort mZigbeeSerialPort = null;
	private static String mName;

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
		// ��ʹ�� SDK �����֮ǰ��ʼ�� context ��Ϣ������ ApplicationContext
		SDKInitializer.initialize(this);
	}

}
