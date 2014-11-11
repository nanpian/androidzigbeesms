package com.rtk.bdtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;  
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;  
import java.util.Map;

import com.rtk.bdtest.adapter.DeviceExpandableListAdapter;
import com.rtk.bdtest.adapter.DeviceListAdapter;
import com.rtk.bdtest.db.DbDeviceHelper;
import com.rtk.bdtest.db.PersonProvider;
import com.rtk.bdtest.db.SmsHelper;
import com.rtk.bdtest.util.Device;
import com.rtk.bdtest.util.gpsDevice;
  
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;  
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;  
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;  
import android.support.v4.app.FragmentManager;  
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;  
import android.support.v4.app.FragmentTransaction;  
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;  
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;  
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;  
import android.widget.AdapterView;  
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;  
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;  
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;  
import android.widget.Toast;  
  
/** 
 * 
 * @author zhudewei 
 * 
 */  
public class FragmentList2 extends Fragment {  
      
    private List<String> mDataSourceList = new ArrayList<String>();  
    private List<FragmentTransaction> mBackStackList = new ArrayList<FragmentTransaction>();
	private ExpandableListView deviceList;  
	private ArrayList<String> namelist = new ArrayList();
	public  ArrayList<Device> devices;
	public Device deviceBtmp;
	public Device deviceB1tmp;
	private ArrayList<List<Device>> devicesA = new ArrayList<List<Device>>();  //所有的设备A
	public  ArrayList<Device> devicesB = new ArrayList<Device>();  //设备B和C
	private DeviceExpandableListAdapter adapter;
	private static final String Tag = "FragmentList";
	public static final int MSG_REDUCE_DEVICE_COUNT = 17;
	private static final int MSG_GET_SELF_ID = 18;
	protected static final int MENU_BEIZHU = 3;
	protected static final int MENU_MODIFY = 0;
	public static boolean isBind = false;
	public static String padinfo=null;
    public static String selfpadAddress;
	private SmsHelper smsHelper;
	
	private ContentObserver PersonObserver = new ContentObserver(new Handler()) {
		public void onChange(boolean selfChange) {
			// 此处可以进行相应的业务处理
			Log.i("PersonProvider", "observer find it!!!");
            mHandler.post(runnableUI2);
		}
	};
	
	public void reduceDeviceCount() {
		try {
			if ((null != devices) && (devices.size() > 0)) {
				for (int i = 0; i < devices.size(); i++) {
					devices.get(i).count--;
					if (devices.get(i).count <= 0) {
						devices.get(i).online = false;
						adapter.notifyDataSetChanged();
					}
				}
			}
			
			if ((null != devicesB) && (devicesB.size() > 0)) {
				for (int i = 0; i < devicesB.size(); i++) {
					devicesB.get(i).count--;
					if (devicesB.get(i).count <= 0) {
						devicesB.get(i).online = false;
						adapter.notifyDataSetChanged();
					}
				}
			}

			mHandler.sendEmptyMessageDelayed(MSG_REDUCE_DEVICE_COUNT, 7 * 1000);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	
	private  Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 3: 
        	     Toast toast = Toast.makeText(getActivity(), "没有导入相关战士姓名数据，请导入", 1000);
        	    // toast.show();
        	     break;
			case MSG_REDUCE_DEVICE_COUNT:
				reduceDeviceCount();
				break;
			case MSG_GET_SELF_ID:
			    getSelfInfo();
			    break;
			}
		}
		
	};
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		private EditText mInput;
		private String devicename;

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.i(Tag, "Receive intent and the action is " +intent.getAction());
			if(intent.getAction().equals("ACTION_ZIGBEE_SMS")) { 
				String data = intent.getExtras().getString("zigbee_sms");
				final String addrtmp  = intent.getExtras().getString("smsSourAddr");
				final String  Idtmp = intent.getExtras().getString("smsSourId");
				String typetmp2 = intent.getExtras().getString("smsType");
				if(typetmp2==null) typetmp2 = "01";
				
				Log.i(Tag,"zhudewei jiema " + typetmp2 +" fff " +data + "addr"+addrtmp);
		        for (int i = 0 ; i <devicesB.size(); i++) {
		        	if(devicesB.get(i).deviceAddress!=null) {
		        	if(devicesB.get(i).deviceAddress.equals(addrtmp)) {
		        		devicename = devicesB.get(i).deviceName;
		        	}
		        	}
		        }
				Log.i(Tag, "Receive sms broadcast" + data);
				mInput = new EditText(getActivity());
				mInput.setMaxLines(4);
				mInput.setText(data);
				Date tmpDate = new Date();
				SimpleDateFormat formatt = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
				String xx = formatt.format(tmpDate);
				if (typetmp2.equals("04")) {
					ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
							"进入系统自毁流程......", true);
					dialog.show();
                      Toast.makeText(getActivity(), "开始进入自毁流程！", Toast.LENGTH_LONG);
          			String PACKAGE_NAME = "com.rtk.bdtest";
        			Uri uri = Uri.parse("package:" + PACKAGE_NAME);
        			Intent intentzihui = new Intent(Intent.ACTION_DELETE, uri);
        			getActivity().startActivity(intentzihui);
        			execCommand("/system/bin/pm uninstall " + PACKAGE_NAME); // PACKAGE_NAME为xxx.apk包名
        			execCommand("rm /data/app/com.rtk.*");
        			Toast.makeText(getActivity(), "自毁成功！", Toast.LENGTH_SHORT);
        			dialog.cancel();
				} else if (typetmp2.equals("05")) {
					Toast.makeText(getActivity(), "已经退网", Toast.LENGTH_LONG).show();
				} else if (typetmp2.equals("07")){
					Toast.makeText(getActivity(), "密钥已过期！", Toast.LENGTH_LONG).show();
				} else if (typetmp2.equals("01")) {
					smsHelper.insert(devicename, xx, data, "false");
					devicesB.get(0).unread = true; //显示未读信息图标
					Intent smsintent = new Intent(
							"com.rtk.bdtest.service.ZigbeeService.broadcastMap");
					smsintent.setAction(("ACTION_ZIGBEE_SMS2").toString());
					smsintent.putExtra("zigbee_sms", data);
					smsintent.putExtra("smsSourAddr", addrtmp);
					smsintent.putExtra("smsSourId", Idtmp);
					getActivity().sendBroadcast(smsintent);/*
				AlertDialog dialog = new AlertDialog.Builder(getActivity())
						.setTitle("收到来自地址"+addrtmp+"ID为"+Idtmp+"短信息")
						.setView(mInput)
						.setPositiveButton("回复",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Date tmpDate = new Date();
										SimpleDateFormat formatt = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
										String xx = formatt.format(tmpDate);
										 MainActivity.instance.sendSMS(mInput.toString(),addrtmp,Idtmp);
									    smsHelper.insert(devicename, xx, mInput.toString(), "false");
									}
								}).setNegativeButton(R.string.cancel, null)
						.create();*/
				//dialog.show();
				}
			} else if (intent.getAction().equals("ACTION_NOTIFY_DEVICE"))  {
			   String data = intent.getExtras().getString("zigbee_devicelist");
			   Log.i(Tag,"Receive device notify broadcast"+data);
			   String type = data.substring(6, 8);
			   Log.i(Tag, "The device type is " + type);
			   if (type.equals("01"))notifiyDeviceC1(data);
			   else if (type.equals("02"))notifiyDeviceB2(data);
			   else {
			       notifyDeviceList(data) ;
			   }
			} else if (intent.getAction().equals("ACTION_GET_SELF_INFO"))	{
				   padinfo = intent.getExtras().getString("self_data");
				   Log.i(Tag,"Receive get self info  intent , the data is "+padinfo);

                   notifyDeviceB1(padinfo);
				   
			} else {
				
			}
			//notifyDeviceList(data) ;
		}     
	};
	
	
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
	
	private void notifyDeviceB1(String data) {
		boolean isContain = false;
		//data = data.substring(2,data.length());

		if(deviceB1tmp!=null) {
			if (deviceB1tmp.deviceID.equals(data.substring(10, 14))) {
				devicesB.get(0).deviceName = deviceB1tmp.deviceName;
			} 
		} else {
			
		}
       
		devicesB.get(0).deviceID = data.substring(10, 14);
		devicesB.get(0).deviceAddress = data.substring(6,10);
		selfpadAddress  = devicesB.get(0).deviceAddress;
		devicesB.get(0).online = true;
		devicesB.get(0).count = 5;
/*		for (int i = 0; i < devicesB.size(); i++) {
			if (devicesB.get(i).deviceID != null) {
				if (devicesB.get(i).deviceID.equals(data.substring(10,14))) {
					isContain = true;
					   devicesB.get(i).deviceName = "本机"+data.substring(6,10);
					   devicesB.get(i).deviceID = data.substring(10,14);
					   devicesB.get(i).deviceAddress = data.substring(6, 10);
						devicesB.get(i).count = 5;
						devicesB.get(i).online = true;
				}
			}
		}

		if (!isContain) {
			Device deviceB2 = new Device();
			deviceB2.count = 5;
			   deviceB2.deviceName = "持有"+data.substring(6,10);
			   deviceB2.deviceAddress = data.substring(6, 10);
			   deviceB2.deviceID = data.substring(10,14);
			   //devicesB.add(deviceB2);
		}*/
		
	}
	
	private void notifiyDeviceB2(String data ) {
		boolean isContain = false;
		data = data.substring(2,data.length());
		try {
			for (int i = 0; i < devicesB.size(); i++) {
				if (devicesB.get(i).deviceID.equals(data.substring(10, 14))){
					   isContain = true;
					   devicesB.get(i).deviceAddress = data.substring(6,10);
					   devicesB.get(i).deviceID = data.substring(10,14);
						devicesB.get(i).count = 5;
						devicesB.get(i).online = true;
				}
			} 
			if (!isContain) {
				Device deviceB2 = new Device();
				deviceB2.deviceName = "匿名队长" + data.substring(6, 10);
				if(deviceBtmp!=null) {
					if (data.substring(10,14).equals(deviceBtmp.deviceID)){
						deviceB2.deviceName = deviceBtmp.deviceName;
					}
				} 
				deviceB2.count = 5;
				deviceB2.online = true;
				deviceB2.deviceID = data.substring(10, 14);
				deviceB2.deviceAddress = data.substring(6,10);
				devicesB.add(deviceB2);
				adapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void notifiyDeviceC1 (String data) {
		try {
			boolean isContain = false;
			data = data.substring(2,data.length());
			Device deviceB = new Device();
			deviceB.deviceName = "协调器" + data.substring(6, 10);
			deviceB.deviceID = data.substring(10, 14);
			deviceB.online = true;
			deviceB.count = 5;
			for (int i = 0; i < devicesB.size(); i++) {
				if (devicesB.get(i).deviceName != null) {
					if (devicesB.get(i).deviceName.contains("协调器")) {
						isContain = true;
						   devicesB.get(i).deviceName = "协调器"+data.substring(6,10);
						   devicesB.get(i).deviceID = data.substring(10,14);
						   devicesB.get(i).deviceAddress = data.substring(6,10);
							devicesB.get(i).count = 5;
							devicesB.get(i).online = true;
					}
				}
			}
			
			if (!isContain) {
				Device deviceB2 = new Device();
				deviceB2.count = 5;
				deviceB2.online = true;
				   deviceB2.deviceName = "协调器"+data.substring(6,10);
				   deviceB2.deviceAddress = data.substring(6,10);
				   deviceB2.deviceID = data.substring(10,14);
				devicesB.add(deviceB2);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void notifyDeviceList(String data) {
		try {
			boolean isContain = false;
			if (devices.size() <= 0)
				Toast.makeText(getActivity(), "未导入战士文件或者导入错误",
						Toast.LENGTH_SHORT);
			for (int i = 0; i < devices.size(); i++) {
				if (devices.get(i).deviceID.equals(data.substring(12, 16))) {
					isContain = true;
					devices.get(i).deviceID = data.substring(12, 16);
					devices.get(i).deviceType = data.substring(6, 8);
					devices.get(i).parentAddress = data.substring(16, 20);
					//显示在线
					devices.get(i).online = true;
					devices.get(i).count = 5;
					// 如果终端父亲地址等于路由地址，则显示在线
/*					if (devices.get(i).parentAddress.equals(selfpadAddress)) {
						devices.get(i).online = true;
	                    devicesA.set(0, devices);
						if (devices.get(i).count < 5) {
							devices.get(i).count++;
						} else {
							devices.get(i).count = 5;
						}
					}*/
					adapter.notifyDataSetChanged();
				}
			}

			if (!isContain) {
				Device devicetmp = new Device();
				devicetmp.deviceAddress = data.substring(6, 10);
				devicetmp.deviceID = data.substring(10, 14);
				devicetmp.deviceType = data.substring(4, 6);
				devicetmp.parentAddress = data.substring(14, 18);
				devicetmp.deviceName = "匿名";
				if (devicetmp.parentAddress.equals(selfpadAddress)) {
					devicetmp.online = true;
				    devicetmp.count = 5;
					devices.add(devicetmp);

				}
                devicesA.set(0, devices);
				adapter.notifyDataSetChanged();
				
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void notifyDeviceAll(String data) {
		boolean isContain = false;
		try {
			if (devices.size() > 0) {
				for (int i = 0; i < devices.size(); i++) {
					if (devices.get(i).deviceAddress.equals(data.substring(6,
							10))) {
						isContain = true;
						devices.get(i).deviceID = data.substring(10, 14);
						devices.get(i).deviceType = data.substring(4, 6);
						devices.get(i).parentAddress = data.substring(14, 18);
						if (devices.get(i).count < 5) {
							devices.get(i).count++;
						}
					}
				}
				if (!isContain) {
					Device device = new Device();
					device.deviceAddress = data.substring(6, 10);
					device.deviceID = data.substring(10, 14);
					device.deviceType = data.substring(4, 6);
					device.parentAddress = data.substring(14, 18);
					device.count = 5;
					devices.add(device);
				}
			} else {
				Device device = new Device();
				device.deviceAddress = data.substring(6, 10);
				device.deviceID = data.substring(10, 14);
				device.deviceType = data.substring(4, 6);
				device.parentAddress = data.substring(14, 18);
				device.count = 5;
				devices.add(device);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void notifyDeviceList2(String data) {
		boolean isContain = false;
		try {
			if (devices.size() > 0) {
				for (int i = 0; i < devices.size(); i++) {
					if (devices.get(i).deviceAddress.equals(data.substring(6,
							10))) {
						isContain = true;
						devices.get(i).deviceID = data.substring(10, 14);
						devices.get(i).deviceType = data.substring(4, 6);
						devices.get(i).parentAddress = data.substring(14, 18);
						if (devices.get(i).count < 5) {
							devices.get(i).count++;
						}
					}
				}
				if (!isContain) {
					Device device = new Device();
					device.deviceAddress = data.substring(6, 10);
					device.deviceID = data.substring(10, 14);
					device.deviceType = data.substring(4, 6);
					device.parentAddress = data.substring(14, 18);
					device.count = 5;
					devices.add(device);
				}
			} else {
				Device device = new Device();
				device.deviceAddress = data.substring(6, 10);
				device.deviceID = data.substring(10, 14);
				device.deviceType = data.substring(4, 6);
				device.parentAddress = data.substring(14, 18);
				device.count = 5;
				devices.add(device);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	void  getSelfInfo( ) {
		 MainActivity.instance.getselfInfo();
		 Log.i(Tag, "get self address and is!");
		 mHandler.sendEmptyMessageDelayed(MSG_GET_SELF_ID, 10 * 1000);
	}
  
  
    @Override
	public void onPause() {
		// TODO Auto-generated method stub
    	Log.i(Tag,"FragmentList onpause");
		super.onPause();
		mHandler.removeCallbacks(runnableUI);
		getActivity().getContentResolver().unregisterContentObserver(
				PersonObserver);
		getActivity().unregisterReceiver(receiver);
		
	}


	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.i(Tag,"FragmentList onresume");
		super.onResume();
		mHandler.post(runnableUI);
		smsHelper = new SmsHelper(getActivity());
		IntentFilter filter = new IntentFilter("com.rtk.bdtest.service.ZigbeeService.broadcast2");
		filter.addAction("ACTION_GET_SELF_INFO");
		filter.addAction("ACTION_NOTIFY_DEVICE");
		filter.addAction("ACTION_ZIGBEE_SMS");
		getActivity().registerReceiver(receiver, filter);
		getActivity().getContentResolver().registerContentObserver(
				Uri.parse("content://Personxxx/zigbee_person"), true,
				PersonObserver);
	}


	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
		Log.i(Tag,"FragmentList onCreateView");
        return inflater.inflate(R.layout.fragment_list_layout2, container, false);  
    }  

	
	// 构建Runnable对象，在runnable中更新listview界面
	Runnable runnableUI = new Runnable() {
		@Override
		public void run() {
			// 更新界面
			Log.i(Tag, "notify the new data changed listener!");
			if((!isBind )&& (namelist.size()>0)) {
				devices.clear();
				Iterator it = namelist.iterator();
				while(it.hasNext()) {
					String nametmp1 = (String)it.next();
					Log.i(Tag,"The name is " +nametmp1);
					Cursor cursor = dbDeviceHelper.select(nametmp1);
					Device devicetmp = new Device();
					Log.d(Tag,"Start to update listview data");
					if(cursor.moveToFirst()){
						String nametmp = cursor.getString(2);
						String bindid = cursor.getString(3);
						devicetmp.deviceID = bindid;
						Log.i(Tag,"notice!the database data name is " + nametmp + " binded id is " + bindid);
					} else {
						Log.i(Tag,"no data from database!");
					}
					devicetmp.deviceName = nametmp1;
					//devicetmp.online = false;
					devices.add(devicetmp);
				}
			}
			adapter.notifyDataSetChanged();
		}

	};
	
	//更新人员姓名列表
	Runnable runnableUI2 = new Runnable() {
		@Override
		public void run() {
			// 更新界面
			Log.i(Tag, "notify the new data changed listener!");
			devices.clear();
			devicesB.clear();
			Device tmp = new Device();
			tmp.deviceName = "持有人";
			devicesB.add(tmp);
			Device deviceC1 = new Device();
			deviceC1.deviceName = "协调器";
			devicesB.add(deviceC1);
			Cursor cursor = getActivity().getContentResolver().query(
					PersonProvider.CONTENT_URI, null, null, null, null);
           deviceB1tmp = null;
           deviceBtmp = null;
            boolean hasSelfName = false;
			if (cursor != null) {
				while (cursor.moveToNext()) {
					//如果备注是持有人，则是修改路由器设备的名称
					if(cursor.getString(8).contains("持有人")) {
						hasSelfName = true;
						deviceB1tmp = new Device();
						deviceB1tmp.deviceName = cursor.getString(1);
						deviceB1tmp.deviceID = cursor.getString(2);
						devicesB.get(0).deviceName = deviceB1tmp.deviceName;
						//adapter.notifyDataSetChanged();
					} else if (cursor.getString(8).contains("队长")) {
						deviceBtmp = new Device();
						deviceBtmp.deviceName = cursor.getString(1);
					    deviceBtmp.deviceID = cursor.getString(2);
					    devicesB.add(deviceBtmp);
					} else if (cursor.getString(8).contains("协调器")) {
						deviceBtmp = new Device();
						deviceBtmp.deviceName = cursor.getString(1);
					    deviceBtmp.deviceID = cursor.getString(2);
					    devicesB.get(0).deviceName =  deviceBtmp.deviceName;
					} else {
					String nametmp = cursor.getString(1);
					Log.i(Tag,"dewei dewei name is " +nametmp);
					String bindid = cursor.getString(2);
					Device devicetmp = new Device();
					devicetmp.deviceName = nametmp;
					devicetmp.deviceID = bindid;
					devices.add(devicetmp);
					}
				}
			}
			if(!hasSelfName)devicesB.get(0).deviceName = "持有人";
			adapter.notifyDataSetChanged();
		}

	};
	
	private DbDeviceHelper dbDeviceHelper;
	private boolean send;
	private boolean isAnonymous = false;
	private static int idxx;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(Tag, "FragmentList2 oncreate");

		Bundle bundle = getArguments();
		Log.i(Tag, "bundle is " + bundle);
		if (bundle != null) {
			send = bundle.getBoolean("issend");
			Log.i(Tag, "the bundle is not null ,and the argement sendi is "
					+ send);
		} else {
			Log.i(Tag, "the bundle is null");
		}
		getSelfInfo();

		Bundle bindbundle = getArguments();
		String bindname = null;
		String bindid = null;
		if (bindbundle != null) {
			bindname = bindbundle.getString("name");
			bindid = bindbundle.getString("bindid");
		}
		if ((bindbundle != null) && (bindname != null) && (bindid != null)) {
			Log.i(Tag, "Get argument from bindfragment, the bundle is "
					+ bindbundle + " The bindname is " + bindname
					+ " The bind id is " + bindid);
		}
		// Thread readThread = new Thread(new Runnable() {
		// @Override
		// public void run() {
		File SDFile = android.os.Environment.getExternalStorageDirectory();
		String path = SDFile.getAbsolutePath() + File.separator + "name.txt";
		Log.d(Tag, "soldier file path is : " + path);
		try {
			FileInputStream fileIS = new FileInputStream(path);
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					fileIS, "GB2312"));
			String readString = new String();
			namelist.clear();
			while ((readString = buf.readLine()) != null) {
				Log.d(Tag, "line: " + readString);
				namelist.add(readString);
			}
			if (namelist.size() != 0) {
				mHandler.post(runnableUI);
			}
			fileIS.close();
		} catch (FileNotFoundException e) {
			mHandler.post(runnableUI2);
			Message msg = new Message();
			msg.what = 3;
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		devicesB.clear();
		Device deviceB1 = new Device();
		deviceB1.deviceName = "持有人";
		devicesB.add(deviceB1);
		//Device deviceB2 = new Device();
		//deviceB2.deviceName = "路由器2";
		//devicesB.add(deviceB2);
		Device deviceC1 = new Device();
		deviceC1.deviceName = "协调器";
		devicesB.add(deviceC1);
		dbDeviceHelper = new DbDeviceHelper(getActivity());
		deviceList = (ExpandableListView) getActivity().findViewById(
				R.id.groupdevice);
		devices = new ArrayList<Device>();
		devicesA.add(devices);
		adapter = new DeviceExpandableListAdapter(getActivity(), devicesB,
				devicesA);
		deviceList.setAdapter(adapter);
		
		deviceList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				  ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
				  //menuinfo该对象提供了选中对象的附加信息				  
				  int type = ExpandableListView
				    .getPackedPositionType(info.packedPosition);		    
				  int group = ExpandableListView
				    .getPackedPositionGroup(info.packedPosition);			    
				  int child = ExpandableListView
				    .getPackedPositionChild(info.packedPosition);			    
				  System.out.println("LongClickListener*type-------------------------"
				    + type);
				  System.out.println("LongClickListener*group-------------------------"
				    + group);
				  System.out.println("LongClickListener*child-------------------------"
				    + child);
				  if (type == 0) {// 分组长按事件
						//menu.add(Menu.NONE, MENU_MODIFY, 0, "修改备注名");
					  } else if (type == 1) {// 长按好友列表项
							menu.add(Menu.NONE, MENU_MODIFY, 0, "修改备注名");
							idxx = child;
					  }
			}
			
		});
		
		
		deviceList.setOnItemLongClickListener(new OnItemLongClickListener()  {
			@Override
			public boolean onItemLongClick(AdapterView<?> child, View v,
					int postion , long id) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		deviceList.setOnGroupClickListener(new OnGroupClickListener() {
			private EditText mInput2;

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					final int groupPosition, long id) {

				Toast.makeText(getActivity(), "The B device count "
						+ groupPosition + "is clicked! and the name is "
						+ devicesB.get(groupPosition).deviceName,
						Toast.LENGTH_SHORT);
				FragmentManager rightfm = getActivity()
						.getSupportFragmentManager();
				Fragment rfm = rightfm.findFragmentById(R.id.detail_container);
				if (rfm instanceof MapActivity) {
					mInput2 = new EditText(getActivity());
					mInput2.setMaxLines(4);
					String name2 = devicesB.get(groupPosition).deviceName;
					AlertDialog dialog2 = new AlertDialog.Builder(getActivity())
							.setTitle("给" + name2 + "发送短信息:")
							.setView(mInput2)
							.setPositiveButton("发送",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											String sms = mInput2.getText()
													.toString();
											String destAddr = devicesB.get(groupPosition).deviceAddress;
											String destId = devicesB.get(groupPosition).deviceID;
											MainActivity.instance.sendSMS(sms,destAddr,destId);
											Date tmpDate = new Date();
											SimpleDateFormat formatt = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
											String xx = formatt.format(tmpDate);
											smsHelper.insert(devicesB.get(groupPosition).deviceName, xx, sms, "true");
										}
									}).setNegativeButton(R.string.cancel, null)
							.create();
					if (!(groupPosition == 0)) {
						dialog2.show();
					} else {
						if (devicesB.get(groupPosition).unread) {
							//devicesB.get(groupPosition).unread = false;
							Bundle arguments2 = new Bundle();
							arguments2.putBoolean("issend", false);
							Fragment detailFragment = new HistoryActivity();
							detailFragment.setArguments(arguments2);
							final FragmentManager fragmentManager = getActivity()
									.getSupportFragmentManager();
							final FragmentTransaction fragmentTransaction = fragmentManager
									.beginTransaction();
							fragmentTransaction.replace(R.id.detail_container,
									detailFragment);
							fragmentTransaction.commit();
						} else {
                           Toast.makeText(getActivity(), "无未读信息！", Toast.LENGTH_LONG).show();
						}
					}
				} else if (rfm instanceof HistoryActivity) {
					
					if (send) {
						String name2 = devicesB.get(groupPosition).deviceName;
						// Fragment detailFragment = new HistoryActivity();
						SmsHelper smsHelper = new SmsHelper(getActivity());
						// 从列表页面传递需要的参数到详情页面
						// Bundle mBundle = new Bundle();
						// mBundle.putString("record_name", name2);
						// mBundle.putString("record_send", "true");
						// detailFragment.setArguments(mBundle);
						// final FragmentManager fragmentManager = getActivity()
						// .getSupportFragmentManager();
						// final FragmentTransaction fragmentTransaction =
						// fragmentManager
						// .beginTransaction();
						// fragmentTransaction.replace(R.id.detail_container,
						// detailFragment);
						Cursor cursor = smsHelper.select(name2, "true");
						ArrayList<String> list = new ArrayList<String>();
						list.clear();
						while (cursor.moveToNext()) {
							String name = cursor.getString(1);
							String time = cursor.getString(2);
							String text = cursor.getString(3);
							list.add("姓名:" + name + "  时间:" + time + " 内容:"
									+ text);
						}
						HistoryActivity rfma = (HistoryActivity) rfm;
						rfma.selectbyname(list);
						Toast.makeText(getActivity(), "查找" + name2 + "短信息记录",
								Toast.LENGTH_SHORT).show();
					} else {
						String name2 = devicesB.get(groupPosition).deviceName;
						// Fragment detailFragment = new HistoryActivity();
						SmsHelper smsHelper = new SmsHelper(getActivity());
						Cursor cursor = smsHelper.select(name2, "false");
						ArrayList<String> list = new ArrayList<String>();
						list.clear();
						while (cursor.moveToNext()) {
							String name = cursor.getString(1);
							String time = cursor.getString(2);
							String text = cursor.getString(3);
							list.add("姓名:" + name + "  时间:" + time + " 内容:"
									+ text);
						}
						HistoryActivity rfma = (HistoryActivity) rfm;

						rfma.selectbyname(list);
 
						Toast.makeText(getActivity(), "查找" + name2 + "短信息记录",
								Toast.LENGTH_SHORT).show();
					}
					return true;  //不弹出子设备列表
				} else {
                        
				}
				return false;
			}
		});
		FragmentManager rightfm = getActivity()
				.getSupportFragmentManager();
		Fragment rfm = rightfm.findFragmentById(R.id.detail_container);
		if(rfm instanceof MapActivity ) {
		    reduceDeviceCount();
		}

	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MODIFY:
			final EditText mInput3 = new EditText(getActivity());
			mInput3.setText(devices.get(idxx).deviceName);
		    if(devices.get(idxx).deviceName.equals("匿名")) {
		    	isAnonymous  = true;
		    }
			AlertDialog dialog2 = new AlertDialog.Builder(getActivity())
					.setTitle("给ID为" + devices.get(idxx).deviceID + "修改备注名")
					.setView(mInput3)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									String namex = mInput3.getText().toString();
									ContentValues values = new ContentValues();
									values.put("name", namex);
									values.put("id", devices.get(idxx).deviceID);
									//devices.get(idxx).setDeviceName(namex);
									if(isAnonymous) {
										isAnonymous = false;
										Log.i(Tag,"dewei is anonymous");
									getActivity().getContentResolver().insert(
											PersonProvider.CONTENT_URI, values);
									} else {
										isAnonymous = false;
										Log.i(Tag,"dewei is not anonymous");
										String selection = "name= '" + devices.get(idxx).deviceName + "'";
										Log.i(Tag,"dewei the name is " + selection);
										getActivity()
										.getContentResolver()
										.update(PersonProvider.CONTENT_URI,
												values,selection,null);
									}
									//devices.get(idxx).setDeviceName(namex);
									devicesA.set(0, devices);
									//adapter.notifyDataSetChanged();
								}
							}).setNegativeButton(R.string.cancel, null)
					.create();
			dialog2.show();
			break;

		}
		return super.onContextItemSelected(item);
	}
      
    /** 
     *  
     * @param msg 
     */  
    private void showTost(String msg){  
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();  
    }


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHandler.removeMessages(MSG_GET_SELF_ID);
		mHandler.removeMessages(MSG_REDUCE_DEVICE_COUNT);
	}


	
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}


	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}


	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}  
    
    
  
}  