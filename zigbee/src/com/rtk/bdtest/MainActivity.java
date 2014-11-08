package com.rtk.bdtest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.baidu.mapapi.SDKInitializer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;

import com.rtk.bdtest.db.DbDeviceHelper;
import com.rtk.bdtest.service.*;
import com.rtk.bdtest.service.BDService.BDBinder;
import com.rtk.bdtest.service.ZigbeeSerivce.ZigbeeBinder;
import com.rtk.bdtest.util.DesCrypt;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		BindActivity.OnBindedListener {

	private ZigbeeSerivce zigbeeService;
	private BDService bdService;
	private static final String Tag = "main";
	private static final boolean EnableDES = true;
	private static final String REQUEST_JOIN = "8004";
	private static final int content_length = 29;

	public static MainActivity instance;
	private static String defaultLatitude = null;
	private String padAddress = "";

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			// 定时发送gps信息到zigbee，这样zigbee才能发送gps广播信息
			defaultLatitude = arg1.getExtras().getString("defaultLatitude");
			Log.i(Tag, "send location to zigbee");
			//sendLocation();
		}

	};

	public void sendLocation() {
		FragmentManager rightfm = this.getSupportFragmentManager();
		Fragment lfm = rightfm.findFragmentById(R.id.list_container);
		if (((FragmentList2) lfm).padinfo != null) {
			Log.i(Tag, "The pad info address is "
					+ ((FragmentList2) lfm).padinfo.substring(4, 8));
			if (lfm instanceof FragmentList2) {
				padAddress = ((FragmentList2) lfm).padinfo.substring(4, 8);
				if (!padAddress.equals("")) {
					byte[] temp = new byte[("3002" + padAddress).getBytes().length
							+ (defaultLatitude).getBytes().length + 2];
					// byte[] temp = CharConverter.hexStringToBytes("3002") +
					// (padAddress + defaultLatitude).getBytes();
					int length = (("3002" + padAddress).getBytes().length) / 2
							+ defaultLatitude.length() + 1;
					String l = String.format("%02x", length);
					Log.d(Tag, "l = " + l);
					System.arraycopy(CharConverter.hexStringToBytes(l), 0,
							temp, 0, 1);
					System.arraycopy(
							CharConverter.hexStringToBytes("3002" + padAddress),
							0, temp, 1, 4);
					System.arraycopy((defaultLatitude).getBytes(), 0, temp, 5,
							(defaultLatitude).getBytes().length);
					sendData2Zigbee(temp);
				}
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.unregisterReceiver(receiver);
	}

	public Activity getActivity() {
		return instance;
	}

	void sendSMS(String sms, String destAddr, String destId) {
		Log.i(Tag, "send data" + sms + " to zigbee!plz wait and verify");

		try {
			sendLongSms(sms, destAddr, destId,"");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//分包发送长短信息！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
	void sendLongSms(String sms, String destAddr, String destId, String type) throws InterruptedException {
		Log.i(Tag, "send long sms" + sms + " to zigbee!plz wait and verify");
		byte[] password = {0x19,0x77,0x04,0x14,(byte) 0x90,(byte) 0xAB,(byte) 0xCD,(byte) 0xEF };
		byte[] sms2;
		try {

			byte[] smsdata ;
			if (EnableDES) {
				sms2 = sms.getBytes("UTF-8");
			    DesCrypt DesCryptInstance = new DesCrypt();
			    byte[] smsdatatmp;
			    if(((ZigbeeApplication) getApplication()).getKey()!=null) {
			    	String key = (String) ((ZigbeeApplication) getApplication()).getKey();
				     smsdatatmp = DesCryptInstance.desCrypto(sms2, key.getBytes("UTF-8"));
			    } else {
				     smsdatatmp = DesCryptInstance.desCrypto(sms2, ("hellomys").getBytes("UTF-8"));
			    }
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            smsdata = Base64.encode(smsdatatmp, Base64.DEFAULT);
			} else {
				sms2 = sms.getBytes("UTF-8");
			    smsdata = sms2;
			}
		   Log.i("deweidewei","The long sms  data length is"+ smsdata.length);
			if(smsdata.length>content_length) {
				int count = (smsdata.length/content_length) +1;
				byte count2byte = (byte)count;
				byte[] smstmpdata = new byte[content_length];
				for (int i = 0; i <count ; i++) {
					byte[] temp = new byte[44]; 
					byte packageindex = (byte)i;
					byte[] tmpindex = {packageindex,count2byte};
		            String head = "2C3003";
					System.arraycopy(CharConverter.hexStringToBytes(head), 0, temp, 0,
							3);
					System.arraycopy(tmpindex, 0, temp, 3, 1);
					System.arraycopy(tmpindex, 1, temp, 4, 1);
					String head2 = destAddr+destId;
                     System.arraycopy(CharConverter.hexStringToBytes(head2), 0, temp, 5, 4);
 					byte[] souDest = {(byte) 0xff,(byte) 0xff};
 					byte[] sourId = {(byte) 0xff,(byte) 0xff};
 		 			System.arraycopy(souDest, 0, temp,9 , 2);
 		 			System.arraycopy(sourId, 0, temp,11 , 2);
 		 			int smslength = content_length;
 		 			if (i<count-1) {
		 			   smslength = content_length+1+1;
		 			   System.arraycopy(smsdata, content_length*i, smstmpdata, 0, content_length);
 		 			} else {
 		 				byte[] tmp = new byte[content_length];
 		 				smslength = (smsdata.length%content_length)+2;
 		 				System.arraycopy(smsdata, content_length*i, tmp, 0, smsdata.length-content_length*i);
 		 				System.arraycopy(tmp, 0, smstmpdata, 0, content_length);
 		 			}
		 			String l = String.format("%02x", smslength);	
		 			System.arraycopy(CharConverter.hexStringToBytes(l), 0, temp, 13, 1 ); //字串长度
		 			System.arraycopy(CharConverter.hexStringToBytes("01"), 0, temp, 14, 1);//类型为03
		 			System.arraycopy(smstmpdata, 0, temp, 15, smstmpdata.length);
		 			String logstring = CharConverter.byteToHexString(temp,temp.length);
		 			Log.i("deweidewei","dewei send data " +i + " data is" +logstring + " ##########temp is" + temp);
		 			sendData2Zigbee(temp);
				}
			} else {
				try {
					//0x2D为length，3003短信息标志位，00为index，01为包数，destAddr目标短地址
					//destId为目标id，0xffff，0xffff，短信息内容为32字节
					byte[] temp = new byte[44]; 
		            String head = "2C30030001"+destAddr+destId;
					System.arraycopy(CharConverter.hexStringToBytes(head), 0, temp, 0,
							9);
					byte[] souDest = {(byte) 0xff,(byte) 0xff};
					byte[] sourId = {(byte) 0xff,(byte) 0xff};
		 			System.arraycopy(souDest, 0, temp,9 , 2);
		 			System.arraycopy(sourId, 0, temp,11 , 2);
		 			int smslength = smsdata.length+2;
		 			String l = String.format("%02x", smslength);	
		 			
		 			System.arraycopy(CharConverter.hexStringToBytes(l), 0, temp, 13, 1 ); //字串长度
		 			System.arraycopy(CharConverter.hexStringToBytes("01"), 0, temp, 14, 1);//类型为03
					String head2 = "2D30030001"+destAddr+destId+l+"03";
		 			System.arraycopy(smsdata, 0, temp, 15, smsdata.length);
		 			Log.i(Tag,"The sms send data head is " +head2);
		 			Log.i(Tag,"The sms send data string is " + temp);
		 			sendData2Zigbee(temp);
					//sendData2Zigbee(data.getBytes("UTF-8"));
					//sendData2Zigbee(data.getBytes("Unicode"));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//分包发送分组信息！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
	void sendPersonInfo(String sms, String destAddr, String destId, String type) throws InterruptedException {
		Log.i(Tag, "send personinfo" + sms + " to zigbee!plz wait and verify");
		byte[] password = {0x19,0x77,0x04,0x14,(byte) 0x90,(byte) 0xAB,(byte) 0xCD,(byte) 0xEF };
		byte[] sms2;
		try {
			byte[] smsdata ;
			if (EnableDES) {
				sms2 = sms.getBytes("UTF-8");
			    DesCrypt DesCryptInstance = new DesCrypt();
			    byte[] smsdatatmp;
			    if(((ZigbeeApplication) getApplication()).getKey()!=null) {
			    	String key = (String) ((ZigbeeApplication) getApplication()).getKey();
				     smsdatatmp = DesCryptInstance.desCrypto(sms2, key.getBytes("UTF-8"));
			    } else {
				     smsdatatmp = DesCryptInstance.desCrypto(sms2, ("hellomys").getBytes("UTF-8"));
			    }
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            smsdata = Base64.encode(smsdatatmp, Base64.DEFAULT);
			} else {
				sms2 = sms.getBytes("UTF-8");
			    smsdata = sms2;
			}
		   Log.i("deweidewei","The data length is"+ smsdata.length);
			if(smsdata.length>content_length) {
				int count = (smsdata.length/content_length) +1;
				byte count2byte = (byte)count;
				byte[] smstmpdata = new byte[content_length];
				for (int i = 0; i <count ; i++) {
					byte[] temp = new byte[44]; 
					byte packageindex = (byte)i;
					byte[] tmpindex = {packageindex,count2byte};
		            String head = "2C3003";
					System.arraycopy(CharConverter.hexStringToBytes(head), 0, temp, 0,
							3);
					System.arraycopy(tmpindex, 0, temp, 3, 1);
					System.arraycopy(tmpindex, 1, temp, 4, 1);
					String head2 = destAddr+destId;
                     System.arraycopy(CharConverter.hexStringToBytes(head2), 0, temp, 5, 4);
 					byte[] souDest = {(byte) 0xff,(byte) 0xff};
 					byte[] sourId = {(byte) 0xff,(byte) 0xff};
 		 			System.arraycopy(souDest, 0, temp,9 , 2);
 		 			System.arraycopy(sourId, 0, temp,11 , 2);
 		 			int smslength = content_length;
 		 			if (i<count-1) {
		 			   smslength = content_length+1+1;
		 			   System.arraycopy(smsdata, content_length*i, smstmpdata, 0, content_length);
 		 			} else {
 		 				byte[] tmp = new byte[content_length];
 		 				smslength = (smsdata.length%content_length)+2;
 		 				System.arraycopy(smsdata, content_length*i, tmp, 0, smsdata.length-content_length*i);
 		 				System.arraycopy(tmp, 0, smstmpdata, 0, content_length);
 		 			}
		 			String l = String.format("%02x", smslength);	
		 			System.arraycopy(CharConverter.hexStringToBytes(l), 0, temp, 13, 1 ); //字串长度
		 			System.arraycopy(CharConverter.hexStringToBytes("03"), 0, temp, 14, 1);//类型为03
		 			System.arraycopy(smstmpdata, 0, temp, 15, smstmpdata.length);
		 			String logstring = CharConverter.byteToHexString(temp,temp.length);
		 			Log.i("deweidewei","dewei send data " +i + " data is" +logstring + " ##########temp is" + temp);
		 			sendData2Zigbee(temp);
				}
			} else {
				try {
					//0x2D为length，3003短信息标志位，00为index，01为包数，destAddr目标短地址
					//destId为目标id，0xffff，0xffff，短信息内容为32字节
					byte[] temp = new byte[44]; 
		            String head = "2C30030001"+destAddr+destId;
					System.arraycopy(CharConverter.hexStringToBytes(head), 0, temp, 0,
							9);
					byte[] souDest = {(byte) 0xff,(byte) 0xff};
					byte[] sourId = {(byte) 0xff,(byte) 0xff};
		 			System.arraycopy(souDest, 0, temp,9 , 2);
		 			System.arraycopy(sourId, 0, temp,11 , 2);
		 			int smslength = smsdata.length+2;
		 			String l = String.format("%02x", smslength);	
		 			
		 			System.arraycopy(CharConverter.hexStringToBytes(l), 0, temp, 13, 1 ); //字串长度
		 			System.arraycopy(CharConverter.hexStringToBytes("03"), 0, temp, 14, 1);//类型为03
					String head2 = "2D30030001"+destAddr+destId+l+"03";
		 			System.arraycopy(smsdata, 0, temp, 15, smsdata.length);
		 			Log.i(Tag,"The sms send data head is " +head2);
		 			Log.i(Tag,"The sms send data string is " + temp);
		 			sendData2Zigbee(temp);
					//sendData2Zigbee(data.getBytes("UTF-8"));
					//sendData2Zigbee(data.getBytes("Unicode"));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void sendSMS(String sms, String destAddr, String destId, String type) {
		Log.i(Tag, "send data" + sms + " to zigbee!plz wait and verify");
		zigbeeService.sendsms2Zigbee2(sms, destAddr, destId);
	}

	public Handler handler = new Handler();
    private byte[] hexStringToBytesF = null;

	   // 构建Runnable对象，在runnable中更新listview界面  
    Runnable   runnableSendSms=new  Runnable(){  

	@Override  
       public void run() {  
           //更新界面  
   		zigbeeService.sendData2Zigbee(hexStringToBytesF );
   		try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       }  
       
         
   };
   
	public void sendData2Zigbee(byte[] tmp111) {
		Log.i(Tag, "send data2 zigbee!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		hexStringToBytesF = tmp111;
   		zigbeeService.sendData2Zigbee(hexStringToBytesF );
   		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       // handler.post(runnableSendSms);
       // handler.removeCallbacks(runnableSendSms);

	}
	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		handler.removeCallbacks(runnableSendSms);
	}

	void getselfInfo() {
		Log.i(Tag, "get self data");
		if (zigbeeService != null) {
			Log.i(Tag, "service is not null");
			this.zigbeeService.getselfInfo();
		}
		if (bdService != null) {
			Log.i(Tag, "bdservice is not null!");
		}
	}

	@Override
	public void OnBindedListener(String name, String id) {
		// TODO Auto-generated method stub
		Bundle arguments = new Bundle();
		arguments.putString("name", name);
		arguments.putString("bindid", id);
		Log.i(Tag, "onbind linstener ok!");
		Fragment listFragment = new FragmentList2();
		listFragment.setArguments(arguments);
		final FragmentManager fragmentManager = this
				.getSupportFragmentManager();
		final FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.list_container, listFragment);
		fragmentTransaction.commit();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		SDKInitializer.initialize(getApplicationContext());
		instance = this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Intent intent1 = new Intent(this, BDService.class);
		Intent intent2 = new Intent(this, ZigbeeSerivce.class);
		Log.i("main", "onStart.....onBind");
		boolean bdsuccess = this.getApplicationContext().bindService(intent1,
				mConnection1, Context.BIND_AUTO_CREATE);
		boolean zgibeesucess = this.getApplicationContext().bindService(
				intent2, mConnection2, Context.BIND_AUTO_CREATE);
		if (!bdsuccess)
			Log.i(Tag, "bind bd failed");
		if (!zgibeesucess)
			Log.i(Tag, "bind zigbee failed");

		Fragment listFragment = new FragmentList2();

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.add(R.id.list_container, listFragment);

		Fragment detailFragment = new MapActivity();
		fragmentTransaction.replace(R.id.detail_container, detailFragment);
		fragmentTransaction.commit();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
		PowerManager.WakeLock mWakeLock = pm.newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK, "Zigbee Wakelock");
		mWakeLock.acquire();
		IntentFilter filter = new IntentFilter(
				"com.rtk.bdtest.service.BDService.broadcast3");
		this.registerReceiver(receiver, filter);
	}

	private ServiceConnection mConnection2 = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.i(Tag, "zigbeebinder binded");
			ZigbeeBinder binder = (ZigbeeBinder) service;
			zigbeeService = binder.getService();
			if (zigbeeService != null) {
				Log.i(Tag, "connected!zigbeeService is not null");
				getselfInfo();
				MainActivity.instance.sendData2Zigbee(CharConverter.hexStringToBytes(REQUEST_JOIN));
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}

	};

	private ServiceConnection mConnection1 = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(Tag, "bd binder binded");
			BDBinder binder = (BDBinder) service;
			bdService = binder.getService();
			if (bdService != null) {
				Log.i(Tag, "connected!bdservce is not null");
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}

	};
	
	// 当用户在首Activity点击返回键时，提示用户是否退出
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		FragmentManager rightfm = getSupportFragmentManager();
		Fragment rfm = rightfm.findFragmentById(R.id.detail_container);
		if (rfm instanceof PersonActivity) {
			LinearLayout lf1 = (LinearLayout) findViewById(R.id.list_container);
			lf1.setVisibility(View.VISIBLE);
		}else if (rfm instanceof MapActivity) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("退出本应用")
						.setMessage("您确认要退出吗？")
						.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										finish();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
									}
								}).show();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
/*		case R.id.settings1: {
			LinearLayout lf1 = (LinearLayout) findViewById(R.id.list_container);
			lf1.setVisibility(View.VISIBLE);
			Fragment detailFragment = new BindActivity();
			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
			break;*/
		case R.id.edit: {
			
			Fragment detailFragment = new PersonActivity();
			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment);
			
	//		Fragment lfm1 = fragmentManager.findFragmentById(R.id.list_container);
			
	//		fragmentTransaction.remove(lfm1);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
			LinearLayout lf1 = (LinearLayout) findViewById(R.id.list_container);
			lf1.setVisibility(View.GONE);
		}
			break;
		case R.id.settings2: {
			LinearLayout lf1 = (LinearLayout) findViewById(R.id.list_container);
			lf1.setVisibility(View.VISIBLE);
			Bundle arguments = new Bundle();
			arguments.putBoolean("issend", true);
			Fragment detailFragment = new HistoryActivity();
			detailFragment.setArguments(arguments);
			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.replace(R.id.detail_container, detailFragment);

			Fragment listFragment = new FragmentList2();
			listFragment.setArguments(arguments);
			fragmentTransaction.replace(R.id.list_container, listFragment);

			fragmentTransaction.commit();
		}
			break;
/*		case R.id.settings3: {
			LinearLayout lf1 = (LinearLayout) findViewById(R.id.list_container);
			lf1.setVisibility(View.VISIBLE);
			Bundle arguments2 = new Bundle();
			arguments2.putBoolean("issend", false);
			Fragment detailFragment = new HistoryActivity();
			detailFragment.setArguments(arguments2);

			Fragment listFragment = new FragmentList2();
			listFragment.setArguments(arguments2);

			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment);
			fragmentTransaction.replace(R.id.list_container, listFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
			break;*/
		case R.id.settings4: {
			LinearLayout lf1 = (LinearLayout) findViewById(R.id.list_container);
			lf1.setVisibility(View.VISIBLE);
			Fragment detailFragment = new MapActivity();
			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment);
			fragmentTransaction.commit();
		}
			break;
		case R.id.settings5: {
			LinearLayout lf1 = (LinearLayout) findViewById(R.id.list_container);
			lf1.setVisibility(View.VISIBLE);
			Fragment detailFragment = new TestActivity();
			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
			break;
		case R.id.zihui:
			LinearLayout lf1 = (LinearLayout) findViewById(R.id.list_container);
			lf1.setVisibility(View.VISIBLE);
			String PACKAGE_NAME = "com.rtk.bdtest";
			Uri uri = Uri.parse("package:" + PACKAGE_NAME);
			Intent intent = new Intent(Intent.ACTION_DELETE, uri);
			this.startActivity(intent);
			execCommand("/system/bin/pm uninstall " + PACKAGE_NAME); // PACKAGE_NAME为xxx.apk包名
			execCommand("rm /data/app/com.rtk.*");
			Toast.makeText(this, "自毁成功！", Toast.LENGTH_SHORT);
			break;
		case R.id.copy:
			LinearLayout lf2 = (LinearLayout) findViewById(R.id.list_container);
			lf2.setVisibility(View.VISIBLE);
			try {
				final String COPY_FILENAME2 = "quanguogailue.dat";
				final String COPY_FILENAME = "nanjing_315.dat";
				final String SDCARD_PATH = android.os.Environment
						.getExternalStorageDirectory().getAbsolutePath();
				String databasePath = SDCARD_PATH + File.separator
						+ "BaiduMapSDK/vmp/h/";
				String databaseFilename = SDCARD_PATH + File.separator
						+ "BaiduMapSDK/vmp/h/" + COPY_FILENAME;
				Log.i(Tag, " the copy file name is " + databaseFilename);
				String databaseFilename2 = SDCARD_PATH + File.separator
						+ "BaiduMapSDK/vmp/h/" + COPY_FILENAME2;
				File dir = new File(databasePath);
				if (!dir.exists())
					dir.mkdir();
				if (!(new File(databaseFilename)).exists()) {
					ProgressDialog dialog = ProgressDialog.show(this, "",
							"地图资源拷贝中......", true);
					dialog.show();
					InputStream is = getResources().openRawResource(
							R.raw.quanguogailue);
					FileOutputStream fos = new FileOutputStream(
							databaseFilename);
					byte[] buffer = new byte[8192];
					int count = 0;
					while ((count = is.read(buffer)) > 0) {
						fos.write(buffer, 0, count);
					}
					fos.close();
					is.close();
				} else if (!(new File(databaseFilename2)).exists()) {
					ProgressDialog dialog2 = ProgressDialog.show(this, "",
							"地图资源拷贝中......", true);
					dialog2.show();
					InputStream is2 = getResources().openRawResource(
							R.raw.nanjing_315);
					FileOutputStream fos2 = new FileOutputStream(
							databaseFilename2);
					byte[] buffer2 = new byte[8192];
					int count2 = 0;
					while ((count2 = is2.read(buffer2)) > 0) {
						fos2.write(buffer2, 0, count2);
					}
					fos2.close();
					is2.close();
					dialog2.cancel();
					Toast.makeText(this, "地图数据拷贝成功", Toast.LENGTH_SHORT).show();
					Intent i = getBaseContext().getPackageManager()
							.getLaunchIntentForPackage(
									getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} else {
					Toast.makeText(this, "已经有地图数据", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				new AlertDialog.Builder(this)
						.setTitle("错误报告")
						.setMessage("无法复制！")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialoginterface,
											int i) {
									}
								}).show();
			}
			break;
		case R.id.settings6: {
			finish();
		}
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	public boolean execCommand(String cmd) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}

}