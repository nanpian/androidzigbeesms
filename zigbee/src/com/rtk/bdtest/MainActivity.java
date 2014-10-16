package com.rtk.bdtest;

import java.io.IOException;

import com.baidu.mapapi.SDKInitializer;
import android.app.Activity;
import android.app.Service;

import com.rtk.bdtest.db.DbDeviceHelper;
import com.rtk.bdtest.service.*;
import com.rtk.bdtest.service.BDService.BDBinder;
import com.rtk.bdtest.service.ZigbeeSerivce.ZigbeeBinder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		BindActivity.OnBindedListener {

	private ZigbeeSerivce zigbeeService;
	private BDService bdService;
	private static final String Tag = "main";

	public static MainActivity instance;
	private static String defaultLatitude = null;
	private String padAddress = "";
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			//定时发送gps信息到zigbee，这样zigbee才能发送gps广播信息
			defaultLatitude = arg1.getExtras().getString("defaultLatitude");
			 sendLocation();
		}
	
	};
	
	public void sendLocation() {
		FragmentManager rightfm = this
				.getSupportFragmentManager();
		Fragment lfm = rightfm.findFragmentById(R.id.list_container);
		if (lfm instanceof FragmentList2) {
			padAddress = ((FragmentList2) lfm).padinfo.substring(4,8);
			if (!padAddress.equals("")) {
				byte[] temp = new byte[("3002" + padAddress).getBytes().length
						+ (defaultLatitude).getBytes().length + 2];
				// byte[] temp = CharConverter.hexStringToBytes("3002") +
				// (padAddress + defaultLatitude).getBytes();
				int length = (("3002" + padAddress).getBytes().length) / 2
						+ defaultLatitude.length() + 1;
				String l = String.format("%02x", length);
				Log.d(Tag, "l = " + l);
				System.arraycopy(CharConverter.hexStringToBytes(l), 0, temp, 0,
						1);
				System.arraycopy(
						CharConverter.hexStringToBytes("3002" + padAddress), 0,
						temp, 1, 4);
				System.arraycopy((defaultLatitude).getBytes(), 0, temp, 5,
						(defaultLatitude).getBytes().length);
				sendData2Zigbee(temp);
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

	void sendSMS(String sms) {
		Log.i(Tag, "send data" + sms + " to zigbee!plz wait and verify");
		zigbeeService.sendsms2Zigbee(sms);
	}
	
	public void sendData2Zigbee(byte[] hexStringToBytes) {
		Log.i(Tag, "send data2 zigbee!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		zigbeeService.sendData2Zigbee(hexStringToBytes);
	}
	
	void getselfInfo() {
		Log.i(Tag ,  "get self data");
		if(zigbeeService!= null ) {
			Log.i(Tag, "service is not null");
		   this. zigbeeService.getselfInfo();
		}
		if(bdService!=null) {
			Log.i(Tag,"bdservice is not null!");
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
		boolean bdsuccess = this.getApplicationContext().bindService(intent1, mConnection1, Context.BIND_AUTO_CREATE);
		boolean zgibeesucess = this.getApplicationContext().bindService(intent2, mConnection2, Context.BIND_AUTO_CREATE);
		if (!bdsuccess)Log.i(Tag,"bind bd failed");
		if(!zgibeesucess)Log.i(Tag,"bind zigbee failed");

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
		IntentFilter filter = new IntentFilter("com.rtk.bdtest.service.BDService.broadcast3");
		getActivity().registerReceiver(receiver, filter);
	}

	private ServiceConnection mConnection2 = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.i(Tag,"zigbeebinder binded");
			ZigbeeBinder binder = (ZigbeeBinder) service;
			zigbeeService = binder.getService();
			if(zigbeeService!=null) {
				Log.i(Tag,"connected!zigbeeService is not null");
				getselfInfo();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}

	};

	private ServiceConnection mConnection1 = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(Tag,"bd binder binded");
			BDBinder binder = (BDBinder) service;
			bdService = binder.getService();
			if(bdService!=null) {
				Log.i(Tag,"connected!bdservce is not null");
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}

	};

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
		case R.id.settings1: {
			Fragment detailFragment = new BindActivity();
			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment);
			fragmentTransaction.commit();
		}
			break;
		case R.id.settings2: {
			Bundle arguments = new Bundle();
			arguments.putBoolean("issend", true);
			Fragment detailFragment = new HistoryActivity();
			detailFragment.setArguments(arguments);
			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment);


			Fragment listFragment = new FragmentList2();
			listFragment.setArguments(arguments);
			fragmentTransaction.replace(R.id.list_container, listFragment);

			fragmentTransaction.commit();
		}
			break;
		case R.id.settings3: {
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
			fragmentTransaction.commit();
		}
			break;
		case R.id.settings4: {
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
			Fragment detailFragment = new TestActivity();
			final FragmentManager fragmentManager = this
					.getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment);
			fragmentTransaction.commit();
		}
			break;
		case R.id.zihui:
			String PACKAGE_NAME = "com.rtk.bdtest";
			  Uri uri = Uri.parse("package:" + PACKAGE_NAME);
		        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		        this.startActivity(intent);
			execCommand("/system/bin/pm uninstall " + PACKAGE_NAME); // PACKAGE_NAME为xxx.apk包名
			execCommand("rm /data/app/com.rtk.*");
			Toast.makeText(this, "自毁成功！", Toast.LENGTH_SHORT);
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