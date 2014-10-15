package com.rtk.bdtest.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import com.rtk.bdtest.CharConverter;
import com.rtk.bdtest.FragmentList;
import com.rtk.bdtest.R;
import com.rtk.bdtest.SerialPort;
import com.rtk.bdtest.ZigbeeApplication;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

/*
 * 问题：1.平板路由设备如何得到其他平板设备以及协调器的心跳
 *           2.平板路由设备如何得到其他平板路由设备的gps广播或者信息
 *           3.平板路由设备给其他平板路由或者协调器发送短信息的时候，如何判断发送成功？
 */

public class ZigbeeSerivce extends Service {

	private static final String TAG = "ZigbeeService";
	private static final String WRITE_ID_SUCCESS = "AA";
	private static final String WRITE_ID_FAIL = "55";
	public static final String WRITE_SUCCESS = "00";
	public static final String CMD_MODE_UPDATE = "80FF";
	public static final String CMD_HAND_SHAKE = "FE004D0449";
	public static final String CMD_ENABLE_RUNNING_MODE = "FE004D034E";
	public static final String CMD_HAND_SHAKE_SUCCESS = "FE014D8400C8";
	public static final String CMD_ENABLE_RUNNING_MODE_SUCCESS = "FE014D8300CF";
	private static final String GET_FIRMWARE_INFO = "8003";

	private static ZigbeeApplication mApplication;
	private SerialPort mZigbeeSerialPort;
	private OutputStream mZigbeeOutputStream;
	private InputStream mZigbeeInputStream;
	private byte[] zigbeeBuffer = new byte[64];

	private ZigbeeThread mZigbeeThread;
	private TextView mBDInfo;
	private TextView bdVersionTv;
	private static final int MSG_SHOW_BD_DATA = 0;
	private static final int MSG_SHOW_BD_INFO = 1;
	public static final int MSG_TURN_ON_VOLTAGE = 2;
	public static final int MSG_SHOW_CHECK_BIN = 3;
	public static final int MSG_DISMISS_CHECK_BIN = 4;
	public static final int MSG_SEND_HANDSHAKE = 5;
	public static final int MSG_UPDATE_ERROR = 6;
	public static final int MSG_CONTINUE_WRITE = 7;
	public static final int MSG_CONTINUE_VERIFY = 8;
	public static final int MSG_TURN_TO_RUNNING_MODE = 9;
	public static final int MSG_SHOW_UPDATE_PROGRESS_DIALOG = 10;
	public static final int MSG_SHOW_CHANGE_MODE_PROGRESS_DIALOG = 11;
	public static final int MSG_REFRESH_DIALOG = 12;
	public static final int MSG_SHOW_VERIFY_DIALOG = 13;
	public static final int MSG_SEND_LOCATION_TO_ZIGBEE = 14;
	public static final int MSG_NOTIFY_DEVICE_LIST = 15;
	public static final int MSG_SHOW_TOAST = 16;
	public static final int MSG_REDUCE_DEVICE_COUNT = 17;
	private static final int MSG_SEND_SMS = 18;
	private static final int MSG_SEND_GPS_TOALL = 19;
	private static final int MSG_GET_SELF_INFO = 20;

	public static final int crcStartIndex = 90;
	public static final int crcLength = 4;
	int[] res;
	ProgressDialog checkBinPD;
	private int resIndex = 0;
	private int verifyIndex = 0;
	private static final String MODE_WRITE = "write";
	private static final String MODE_VERIFY = "verify";
	private static final String MODE_NORMAL = "nomal";
	private final  static  String Tag = "ZigbeeService";
	private String currentMode = "";
	private int currentProgress = 0;
	private String firmwareUpdateString = "";
	private boolean isVerifyData = false;
	
	//得到自己这个路由平板的设备id、设备短地址
	public void getselfInfo( ) {
		sendData2Zigbee(CharConverter.hexStringToBytes(GET_FIRMWARE_INFO));
	}
	public  void sendData2Zigbee(byte[] data) {
		try {
			if (null != mZigbeeOutputStream) {
				Log.d(TAG, "write data = " + new String(data));
				mZigbeeOutputStream.write(data);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	//发送短信，发送短信成功的标志是什么？？？
	public void sendsms2Zigbee(String data) {
		 /*Message msg = new Message();
		 msg.what = MSG_SEND_SMS;
		 msg.obj = data;
		handler.sendMessage(msg);*/
		Log.i(Tag, "send sms!");
		try {
			sendData2Zigbee(data.getBytes("UTF-8"));
			//sendData2Zigbee(data.getBytes("Unicode"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	Runnable gpsRunnable = new Runnable () {
		@Override
		public void run() {
			// 每隔5s发送一个gps请求
			Message msg = new Message();
			msg.what = MSG_SEND_GPS_TOALL;
			Log.i(Tag,"send gps to all,the message is MSG_SEND_GPS_TOALL");
			handler.postDelayed(this, 5000);
		}	
	};
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_SHOW_BD_DATA:
				if (null != mBDInfo)
					mBDInfo.setText(msg.obj.toString());
				break;
			case MSG_SHOW_BD_INFO:
				if (null != bdVersionTv)
					bdVersionTv.setText(msg.obj.toString());
				break;
			case MSG_TURN_ON_VOLTAGE:
				break;
			case MSG_SHOW_CHECK_BIN:
				break;
			case MSG_DISMISS_CHECK_BIN:
				if ((null != checkBinPD) && (checkBinPD.isShowing())) {
					checkBinPD.dismiss();
				}
				break;
			case MSG_SEND_HANDSHAKE:
				break;
			case MSG_UPDATE_ERROR:
				currentMode = MODE_NORMAL;
				break;
			case MSG_CONTINUE_WRITE:
				break;
			case MSG_CONTINUE_VERIFY:
				break;
			case MSG_TURN_TO_RUNNING_MODE:
				break;
			case MSG_SHOW_UPDATE_PROGRESS_DIALOG:
				break;
			case MSG_SHOW_CHANGE_MODE_PROGRESS_DIALOG:
				break;
			case MSG_REFRESH_DIALOG:
				if ((null != checkBinPD) && (checkBinPD.isShowing())) {
					checkBinPD.setProgress(currentProgress);
				}
				break;
			case MSG_SHOW_VERIFY_DIALOG:
				break;
			case MSG_SEND_LOCATION_TO_ZIGBEE:
				break;
			case MSG_GET_SELF_INFO:
				Intent intent1 = new Intent("com.rtk.bdtest.service.ZigbeeService.broadcast2");
				intent1.setAction(("ACTION_GET_SELF_INFO").toString());
				intent1.putExtra("self_data", msg.obj.toString());
				sendBroadcast(intent1);
				Log.i(Tag,"send!!!!data!!!!");
				break;
			case MSG_NOTIFY_DEVICE_LIST:
			{  
				//收到刷新用户设备列表信息，发送广播给activity
				Intent intent = new Intent("com.rtk.bdtest.service.ZigbeeService.broadcast");
				intent.setAction(("ACTION_NOTIFY_DEVICE").toString());
				intent.putExtra("zigbee_devicelist", msg.obj.toString());
				sendBroadcast(intent);
			}
				break;
			case MSG_SHOW_TOAST:
				break;
			case MSG_REDUCE_DEVICE_COUNT:
				break;
			case MSG_SEND_SMS:

                break;
			case MSG_SEND_GPS_TOALL:
				Log.i(Tag,"send gps to all");
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	public void handleData(String data, byte[] b) {
		if (data.equals(WRITE_ID_SUCCESS)) {
			Message message = handler.obtainMessage(MSG_SHOW_TOAST);
			message.obj = getString(R.string.write_id_success);
			handler.sendMessage(message);
		} else if (data.equals(WRITE_ID_FAIL)) {
			Message message = handler.obtainMessage(MSG_SHOW_TOAST);
			message.obj = getString(R.string.write_id_fail);
			handler.sendMessage(message);
		} else if (data.length() == 20) {
			// device heart beat
			Message message = handler.obtainMessage(MSG_NOTIFY_DEVICE_LIST);
			message.obj = data;
			handler.sendMessage(message);
		} else if (data.length() == 18) {
			// make route id and address
			String padAddress = data.substring(6, 10); 
			Log.i(Tag,"send data !!!!");
			Message message = handler.obtainMessage(MSG_GET_SELF_INFO);
			message.obj = data;
			handler.sendMessage(message);
		} else if (data.substring(1,3).equals(3002)) {
			Log.i(Tag, "Receive beidou broadcast information! and data is " + data);
			String beidou = data.substring(3);
			Intent intent3 = new Intent("com.rtk.bdtest.service.BDService.broadcast");
			intent3.setAction(("ACTION_UPDATE_GPS_INFO").toString());
			intent3.putExtra("gps", beidou);
			sendBroadcast(intent3);
			Log.i(Tag,"send!!!!data!!!!");
	    } else if (data.substring(1, 3).equals(3003))  {
			//收到短信息处理！
			String smsReceive = data.substring(24, data.length());
			byte[] bytes = smsReceive.getBytes();
			try {
				String smsutf8 = new String(bytes,"utf-8");
				//收到短信息，发送广播给activity，分两种情况处理，第一种是gps信息，第二种是普通短信息！！！！！！！
				Intent smsintent = new Intent("com.rtk.bdtest.service.ZigbeeService.broadcast");
				smsintent.setAction(("ACTION_ZIGBEE_SMS").toString());
				smsintent.putExtra("zigbee_sms", smsutf8);
				sendBroadcast(smsintent);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			Log.d(TAG, "debug info = " + new String(b));
		}
	}

	class ZigbeeThread extends Thread {
		private boolean bIsRunning = false;

		ZigbeeThread() {
		}

		void setRunFlag(boolean flag) {
			bIsRunning = flag;
		}

		@Override
		public void run() {
			while (bIsRunning) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int len = 0;
				try {
					if (mZigbeeInputStream != null) {
						len = mZigbeeInputStream.read(zigbeeBuffer);
					}
					Log.d("dewei", "mZigbeeInputStream is "
							+ mZigbeeInputStream + "len is " + len);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (len > 0) {
					String receivedData = CharConverter.byteToHexString(zigbeeBuffer, len);
					Log.d(TAG, "zigbee data = " + receivedData + "    The length is " + receivedData.length());
					handleData(receivedData, zigbeeBuffer);
					}
				}
			}
		}



	private void initZigbeeSerialPort() {
		try {
			mZigbeeSerialPort = mApplication.getZigbeeSerialPort();
			mZigbeeOutputStream = mZigbeeSerialPort.getOutputStream();
			mZigbeeInputStream = mZigbeeSerialPort.getInputStream();
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(Tag,"zigbeeservice created!");
		mApplication = (ZigbeeApplication) getApplicationContext();
		initZigbeeSerialPort();
		
		mZigbeeThread = new ZigbeeThread();
		mZigbeeThread.setRunFlag(true);
		mZigbeeThread.start();

	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		currentMode = MODE_NORMAL;
		mZigbeeThread.setRunFlag(false);
		try {
			mZigbeeThread.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		handler.removeMessages(MSG_SHOW_BD_DATA);
		handler.removeMessages(MSG_SHOW_BD_INFO);
		handler.removeMessages(MSG_TURN_ON_VOLTAGE);
		handler.removeMessages(MSG_SHOW_CHECK_BIN);
		handler.removeMessages(MSG_DISMISS_CHECK_BIN);
		handler.removeMessages(MSG_SEND_HANDSHAKE);
		handler.removeMessages(MSG_UPDATE_ERROR);
		handler.removeMessages(MSG_CONTINUE_WRITE);
		handler.removeMessages(MSG_CONTINUE_VERIFY);
		handler.removeMessages(MSG_TURN_TO_RUNNING_MODE);
		handler.removeMessages(MSG_SHOW_UPDATE_PROGRESS_DIALOG);
		handler.removeMessages(MSG_SHOW_CHANGE_MODE_PROGRESS_DIALOG);
		handler.removeMessages(MSG_REFRESH_DIALOG);
		handler.removeMessages(MSG_SHOW_VERIFY_DIALOG);
		handler.removeMessages(MSG_SEND_LOCATION_TO_ZIGBEE);
		handler.removeMessages(MSG_NOTIFY_DEVICE_LIST);
		handler.removeMessages(MSG_SHOW_TOAST);
		handler.removeMessages(MSG_REDUCE_DEVICE_COUNT);
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub

		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		Log.i(Tag, "zigbee servcie onbind!");
		return new ZigbeeBinder();
	}

	public class ZigbeeBinder extends Binder {
		public ZigbeeSerivce getService() {
			return ZigbeeSerivce.this;
		}
	}
}
