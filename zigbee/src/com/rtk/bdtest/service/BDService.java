package com.rtk.bdtest.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import com.rtk.bdtest.SerialPort;
import com.rtk.bdtest.ZigbeeApplication;
import com.rtk.bdtest.adapter.DeviceListAdapter;
import com.rtk.bdtest.util.Device;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class BDService extends Service{
	
	private static final String Tag = "BDService";
	public static final String WRITE_SUCCESS = "00";
	public static final String CMD_MODE_UPDATE = "80FF";
	public static final String CMD_HAND_SHAKE = "FE004D0449";
	public static final String CMD_ENABLE_RUNNING_MODE = "FE004D034E";
	public static final String CMD_HAND_SHAKE_SUCCESS = "FE014D8400C8";
	public static final String CMD_ENABLE_RUNNING_MODE_SUCCESS = "FE014D8300CF";

	private static ZigbeeApplication mApplication;
	private SerialPort mBDSerialPort;
	private InputStream mBDInputStream;
	private byte[] bdBuffer = new byte[1024];

	private BDThread bdThread;
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
	public static final int MSG_UPDATE_SELF_GPS = 19;

	public static final int crcStartIndex = 90;
	public static final int crcLength = 4;
	int[] res;
	ProgressDialog checkBinPD;
	private int currentProgress = 0;
	private String defaultLongitude = "";
	private String defaultLatitude = "";
	
	private ArrayList<Device> devices;
	private DeviceListAdapter adapter;
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_UPDATE_SELF_GPS:
				Intent gpsintent = new Intent("com.rtk.bdtest.service.BDService.broadcast");
				gpsintent.setAction("ACTION_UPDATE_SELF_GPS");
				String longitude = defaultLatitude.split(",")[2];
				String latitude = defaultLatitude.split(",")[0];
				Double longitudef = Double.parseDouble(longitude)*0.01f;
				Double latitudef = Double.parseDouble(latitude)*0.01f;
				gpsintent.putExtra("longitude", longitudef);
				gpsintent.putExtra("latitude", latitudef);
				//发送gps信息到zigbee设备，来发送gps广播
				Intent gpssend = new Intent("com.rtk.bdtest.service.BDService.broadcast3");
				gpssend.putExtra("defaultLatitude", defaultLatitude) ;
				Log.i(Tag,"The longitude is " +longitudef + " The latitude is " +latitudef );
			    sendBroadcast(gpsintent);
			    sendBroadcast(gpssend);
				break;
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
			case MSG_NOTIFY_DEVICE_LIST:
				break;
			case MSG_SHOW_TOAST:
				break;
			case MSG_REDUCE_DEVICE_COUNT:
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};
	
	class BDThread extends Thread {
		private boolean bIsRunning = false;

		BDThread() {
		}

		void setRunFlag(boolean flag) {
			bIsRunning = flag;
		}

		@Override
		public void run() {
			while (bIsRunning) {
				try {
				  //Thread.sleep(0);
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int len = 0;
				try {
					if(mBDInputStream!=null) {
					    len = mBDInputStream.read(bdBuffer);
					} else {
						Log.i(Tag,"mBDInputStream isn null ,len is null");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (len != 0) {
					String s = new String(bdBuffer);
					Log.d(Tag, "s = " + s);
					if (len > 0) {
						Message message = handler
								.obtainMessage(MSG_SHOW_BD_DATA);
						message.obj = s;
						handler.sendMessage(message);
						defaultLatitude = s.substring(20, 48);
						defaultLongitude = s.substring(34, 46);
						if((defaultLatitude!=null) && (defaultLongitude!=null) ) {
							//每隔5s更新一次gps经纬度信息
						    handler.sendEmptyMessageDelayed(MSG_UPDATE_SELF_GPS, 5000);
						}
						String[] a = s.split("\n");
						for (int i = 0; i < a.length; i++) {
							// Log.d(TAG, "bd data = " + a[i]);
							if ((a[i].length() >= 9)
									&& (a[i].substring(0, 8).equals("$PDTINFO"))) {
								Message infoMessage = handler
										.obtainMessage(MSG_SHOW_BD_INFO);
								infoMessage.obj = a[i];
								handler.sendMessage(infoMessage);
							}
						}
					}
				}
			}
		}
	}
	
	private void initBDSerialPort() {
		try {
				mBDSerialPort = new SerialPort(getApplicationContext(), new File(
						"/dev/ttyMT0"), 9600, 0);
			mBDSerialPort.getOutputStream();
			mBDInputStream = mBDSerialPort.getInputStream();
			Log.d("dewei","mApplication is " +mApplication);
			Log.d("dewei","mBDInputStream is " + mBDInputStream);
			Log.d("dewei","mBDSerialPort is " + mBDSerialPort);
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
		Log.i(Tag,"bdservice create");
		mApplication = (ZigbeeApplication) getApplicationContext();
		initBDSerialPort();
		
		bdThread = new BDThread();
		bdThread.setRunFlag(true);
		bdThread.start();
	}
	
	

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		bdThread.setRunFlag(false);
		try {
			// thread.join();
			bdThread.interrupt();
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

		super.onDestroy();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		Log.i(Tag,"bdservice binded!");
		return new BDBinder();
	}
	
	public class BDBinder extends Binder{  
        /** 
         * 获取当前Service的实例 
         * @return 
         */  
        public BDService getService(){  
            return BDService.this;  
        }  
    }  

}
