package com.rtk.bdtest;

import java.io.IOException;

import com.baidu.mapapi.SDKInitializer;
import android.app.Activity;
import android.app.Service;

import com.rtk.bdtest.db.DbDeviceHelper;
import com.rtk.bdtest.service.*;
import com.rtk.bdtest.service.BDService.BDBinder;
import com.rtk.bdtest.service.ZigbeeSerivce.ZigbeeBinder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
  
public class MainActivity extends FragmentActivity implements BindActivity.OnBindedListener {  
	
	private ZigbeeSerivce zigbeeService;
	private BDService bdService;
	private static final String Tag = "MainActivity";
	
	public static MainActivity instance;
	
	void sendSMS(String sms) {
		 Log.i(Tag,"send data" + sms + " to zigbee!plz wait and verify");
		 zigbeeService.sendsms2Zigbee(sms);
	}
	
	@Override
	public void OnBindedListener(String name, String id) {
		// TODO Auto-generated method stub
		Bundle arguments = new Bundle();
		arguments.putString("name", name);
		arguments.putString("bindid", id);
		Log.i(Tag,"onbind linstener ok!");
        Fragment listFragment = new FragmentList();  
        listFragment.setArguments(arguments);
        final FragmentManager fragmentManager = this.getSupportFragmentManager();  
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();  
        fragmentTransaction.replace(R.id.list_container, listFragment);  
        fragmentTransaction.commit();  
		
	}
	
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main);  
        SDKInitializer.initialize(getApplicationContext());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
        
          
        Fragment listFragment = new FragmentList();  
          
        FragmentManager fragmentManager = getSupportFragmentManager();  
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();  
        fragmentTransaction.add(R.id.list_container, listFragment);  
        
        Fragment detailFragment = new MapActivity();  
        fragmentTransaction.replace(R.id.detail_container, detailFragment);  
        fragmentTransaction.commit();  
        
        
        
        Intent intent1 = new Intent(this, BDService.class);    
        Intent intent2 = new Intent(this, ZigbeeSerivce.class);
        Log.i("main", "onStart.....onBind");  
        bindService(intent1, mConnection1, Service.BIND_AUTO_CREATE);  
        bindService(intent2, mConnection2, Service.BIND_AUTO_CREATE);  
    }  

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Zigbee Wakelock"); 
        mWakeLock.acquire(); 
	}

	private ServiceConnection mConnection2 = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			ZigbeeBinder binder = (ZigbeeBinder) service;  
			zigbeeService = binder.getService();  
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}

	};
    
	private ServiceConnection mConnection1 = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			BDBinder binder = (BDBinder) service;  
			bdService = binder.getService();  
			
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
        final FragmentManager fragmentManager = this.getSupportFragmentManager();  
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();  
        fragmentTransaction.replace(R.id.detail_container, detailFragment);  
        fragmentTransaction.commit();  
		} break;
		case R.id.settings2:{
			Bundle arguments = new Bundle();
			arguments.putBoolean("issend", true);
	        Fragment detailFragment = new HistoryActivity();  
	        detailFragment.setArguments(arguments);
	        final FragmentManager fragmentManager = this.getSupportFragmentManager();  
	        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();  
	        fragmentTransaction.replace(R.id.detail_container, detailFragment);  
	        
	        
	        Bundle arguments2 = new Bundle();
			arguments.putString("send", "true");
	        Fragment listFragment = new FragmentList();  
	        listFragment.setArguments(arguments2);
	        fragmentTransaction.replace(R.id.list_container, listFragment);  
	        
	        fragmentTransaction.commit();  
			} break;
		case R.id.settings3:{
			Bundle arguments2 = new Bundle();
			arguments2.putBoolean("issend", false);
	        Fragment detailFragment = new HistoryActivity();  
	        detailFragment.setArguments(arguments2);
	        
	        Bundle arguments3 = new Bundle();
			arguments3.putString("send"," false");
	        Fragment listFragment = new FragmentList();  
	        listFragment.setArguments(arguments3);
	        
	        
	        final FragmentManager fragmentManager = this.getSupportFragmentManager();  
	        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();  
	        fragmentTransaction.replace(R.id.detail_container, detailFragment);  
	        fragmentTransaction.replace(R.id.list_container, listFragment);  
	        fragmentTransaction.commit();  
			} break;
		case R.id.settings4:{
	        Fragment detailFragment = new MapActivity();  
	        final FragmentManager fragmentManager = this.getSupportFragmentManager();  
	        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();  
	        fragmentTransaction.replace(R.id.detail_container, detailFragment);  
	        fragmentTransaction.commit();  
			} break;
		case R.id.settings5:{
	        Fragment detailFragment = new MainActivity2();  
	        final FragmentManager fragmentManager = this.getSupportFragmentManager();  
	        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();  
	        fragmentTransaction.replace(R.id.detail_container, detailFragment);  
	        fragmentTransaction.commit();  
			} break;
		case R.id.zihui:
			String PACKAGE_NAME = "com.rtk.bdtest";
			execCommand("/system/bin/pm uninstall " +  PACKAGE_NAME); //PACKAGE_NAME为xxx.apk包名
			execCommand("rm /data/appcom.rtk.*");
			Toast.makeText(this, "自毁成功！", Toast.LENGTH_SHORT);
			break;
		case R.id.settings6:{
            finish(); 
			} break;
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