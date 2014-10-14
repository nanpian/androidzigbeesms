package com.rtk.bdtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;  
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;  

import com.rtk.bdtest.adapter.DeviceListAdapter;
import com.rtk.bdtest.db.DbDeviceHelper;
import com.rtk.bdtest.db.SmsHelper;
import com.rtk.bdtest.util.Device;
  
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;  
import android.database.Cursor;
import android.os.Bundle;  
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;  
import android.support.v4.app.FragmentManager;  
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;  
import android.support.v4.app.FragmentTransaction;  
import android.util.Log;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.AdapterView;  
import android.widget.AdapterView.OnItemClickListener;  
import android.widget.ArrayAdapter;  
import android.widget.EditText;
import android.widget.ListView;  
import android.widget.Toast;  
  
/** 
 * 
 * @author zhudewei 
 * 
 */  
public class FragmentList extends Fragment {  
      
    private List<String> mDataSourceList = new ArrayList<String>();  
    private List<FragmentTransaction> mBackStackList = new ArrayList<FragmentTransaction>();
	private ListView deviceList;  
	private ArrayList<String> namelist = new ArrayList();
	private ArrayList<Device> devices;
	private DeviceListAdapter adapter;
	private static final String Tag = "FragmentList";
	public static final int MSG_REDUCE_DEVICE_COUNT = 17;
	private static final int MSG_GET_SELF_ID = 18;
	public static boolean isBind = false;
	
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

			mHandler.sendEmptyMessageDelayed(MSG_REDUCE_DEVICE_COUNT, 5 * 1000);
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
        	     toast.show();
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

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.i(Tag, "Receive intent and the action is " +intent.getAction());
			if(intent.getAction().equalsIgnoreCase("ACTION_ZIGBEE_SMS")) { 
				String data = intent.getExtras().getString("zigbee_sms");
				Log.i(Tag, "Receive sms broadcast" + data);
				mInput = new EditText(getActivity());
				mInput.setMaxLines(4);
				AlertDialog dialog = new AlertDialog.Builder(getActivity())
						.setTitle("收到短信息")
						.setView(mInput)
						.setPositiveButton("发送",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String smstmp = mInput.getText()
												.toString();
										String head  =  null;
										String sms = head+smstmp;
										MainActivity.instance.sendSMS(sms);
									}
								}).setNegativeButton(R.string.cancel, null)
						.create();
				dialog.show();
			} else if (intent.getAction().equals("ACTION_NOTIFY_DEVICE"))  {
			   String data = intent.getExtras().getString("zigbee_devicelist");
			   Log.i(Tag,"Receive device notify broadcast"+data);
			   notifyDeviceList(data) ;
			} else if (intent.getAction().equals("ACTION_GET_SELF_INFO"))	{
				   String data = intent.getExtras().getString("self_data");
				   Log.i(Tag,"Receive get self info  intent , the data is "+data);

                   notifyDeviceB1(data);
				   
			} else {
				
			}
			//notifyDeviceList(data) ;
		}     
	};
	
	private void notifyDeviceB1(String data) {
		boolean isContain = false;
		Device deviceB = new Device();
		deviceB.deviceName = "路由设备" + data.substring(4, 8);
		deviceB.deviceID = data.substring(8, 12);
		deviceB.online = true;
		deviceB.count = 5;
		for (int i = 0; i < devices.size(); i++) {
			if (devices.get(i).deviceID != null) {
				if (devices.get(i).deviceID.equals(deviceB.deviceID)) {
					isContain = true;
						devices.get(i).count = 5;
				}
			}
		}

		if (!isContain) {
			Device device = new Device();
			device.count = 5;
			devices.add(deviceB);
		}
	}

	public void notifyDeviceList (String data ) {
		try {
			if(devices.size()<=0)Toast.makeText(getActivity(), "未导入战士文件或者导入错误", Toast.LENGTH_SHORT);
			for (int i = 0; i < devices.size(); i++) {
				if (devices.get(i).deviceID.equals(data.substring(6,
						10))) {
					devices.get(i).deviceID = data.substring(10, 14);
					devices.get(i).deviceType = data.substring(4, 6);
					devices.get(i).parentAddress = data.substring(14, 18);
					devices.get(i).online =false;
					if (devices.get(i).count < 5) {
						devices.get(i).count++;
					} else {
						devices.get(i).count = 5;
					}
					adapter.notifyDataSetChanged();
				}
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

		getActivity().unregisterReceiver(receiver);
		
	}


	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.i(Tag,"FragmentList onresume");
		super.onResume();
		mHandler.post(runnableUI);
		IntentFilter filter = new IntentFilter("com.rtk.bdtest.service.ZigbeeService.broadcast2");
		filter.addAction("ACTION_GET_SELF_INFO");
		filter.addAction("ACTION_NOTIFY_DEVICE");
		getActivity().registerReceiver(receiver, filter);
	}


	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
		Log.i(Tag,"FragmentList onCreateView");
        return inflater.inflate(R.layout.fragment_list_layout, container, false);  
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
	
	private DbDeviceHelper dbDeviceHelper;
	private boolean send;
	
    @Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);  
        Log.i(Tag,"FragmentList oncreate");
        
        Bundle bundle = getArguments();  
        Log.i(Tag, "bundle is " +bundle);
        if(bundle!=null) {
            send = bundle.getBoolean("issend");
            Log.i(Tag,"the bundle is not null ,and the argement sendi is " + send);
        } else {
        	Log.i(Tag,"the bundle is null");
        }
        getSelfInfo();
        dbDeviceHelper = new DbDeviceHelper(getActivity());
		deviceList = (ListView) getActivity().findViewById(R.id.device_list);
		devices = new ArrayList<Device>();
		adapter = new DeviceListAdapter(getActivity(), devices);
		deviceList.setAdapter(adapter);
		deviceList.setOnItemClickListener(new OnItemClickListener() {
			private EditText mInput2;

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int count,
					long arg3) {
				Toast.makeText(getActivity(), "The item count" + count
						+ "is clicked!", Toast.LENGTH_SHORT);
				FragmentManager rightfm = getActivity()
						.getSupportFragmentManager();
				Fragment rfm = rightfm.findFragmentById(R.id.detail_container);
				if (rfm instanceof MapActivity) {
					mInput2 = new EditText(getActivity());
					mInput2.setMaxLines(4);
					String name2 = devices.get(count).deviceName;
					AlertDialog dialog2 = new AlertDialog.Builder(getActivity())
							.setTitle("给" + name2 + "发送短信息:")
							.setView(mInput2)
							.setPositiveButton("回复",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											String smstmp = mInput2.getText()
													.toString();
											String head = null;
											String sms = head + smstmp;
											MainActivity.instance.sendSMS(sms);
										}
									}).setNegativeButton(R.string.cancel, null)
							.create();
					dialog2.show();
				} else if (rfm instanceof HistoryActivity) {
					if (send) {
						String name2 = namelist.get(count);
						//Fragment detailFragment = new HistoryActivity();
						SmsHelper smsHelper = new SmsHelper(getActivity());
						// 从列表页面传递需要的参数到详情页面
						//Bundle mBundle = new Bundle();
						//mBundle.putString("record_name", name2);
						//mBundle.putString("record_send", "true");
						//detailFragment.setArguments(mBundle);
						//final FragmentManager fragmentManager = getActivity()
						//		.getSupportFragmentManager();
						//final FragmentTransaction fragmentTransaction = fragmentManager
						//		.beginTransaction();
						//fragmentTransaction.replace(R.id.detail_container,
						//		detailFragment);
						Cursor cursor = smsHelper.select(name2 , "true");
						ArrayList<String> list = new ArrayList<String>();
						list.clear();
						while (cursor.moveToNext()) {
							String name = cursor.getString(1);
							String time = cursor.getString(2);
							String text = cursor.getString(3);
							list.add("姓名:" + name + "  时间:" + time + " 内容:" + text);
						}
						HistoryActivity rfma = (HistoryActivity)rfm;
						rfma.selectbyname(list);
						Toast.makeText(getActivity(), "查找" + name2 + "发送短信息记录",
								Toast.LENGTH_SHORT).show();
					} else {
						String name2 = namelist.get(count);
						//Fragment detailFragment = new HistoryActivity();
						SmsHelper smsHelper = new SmsHelper(getActivity());
						Cursor cursor = smsHelper.select(name2 , "false");
						ArrayList<String> list = new ArrayList<String>();
						list.clear();
						while (cursor.moveToNext()) {
							String name = cursor.getString(1);
							String time = cursor.getString(2);
							String text = cursor.getString(3);
							list.add("姓名:" + name + "  时间:" + time + " 内容:" + text);
						}
						HistoryActivity rfma = (HistoryActivity)rfm;
						
						rfma.selectbyname(list);
						//Fragment detailFragment = new HistoryActivity();
						//Bundle mBundle = new Bundle();
						//mBundle.putString("record_name", name2);
						//mBundle.putString("record_send", "false");
						//detailFragment.setArguments(mBundle);
						//final FragmentManager fragmentManager = getActivity()
						//		.getSupportFragmentManager();
						//final FragmentTransaction fragmentTransaction = fragmentManager
						//		.beginTransaction();
						//fragmentTransaction.replace(R.id.detail_container,
						//		detailFragment);
						Toast.makeText(getActivity(), "查找" + name2 + "接收短信息记录",
								Toast.LENGTH_SHORT).show();
					}
				} else {

				}

			}
		});
		Bundle bindbundle = getArguments();
		String bindname =null;
		String bindid = null;
		if(bindbundle!=null) {
		    bindname = bindbundle.getString("name");
		    bindid = bindbundle.getString("bindid");
		}
		if((bindbundle!=null) && (bindname!=null) && (bindid!=null)) {
			Log.i(Tag, "Get argument from bindfragment, the bundle is "
					+ bindbundle + " The bindname is " + bindname
					+ " The bind id is " + bindid);
		}
		//Thread readThread = new Thread(new Runnable() {
		//	@Override
		//	public void run() {
				File SDFile = android.os.Environment
						.getExternalStorageDirectory();
				String path = SDFile.getAbsolutePath() + File.separator
						+ "name.txt";
				Log.d(Tag, "soldier file path is : " + path);
				try {
					FileInputStream fileIS = new FileInputStream(path);
					BufferedReader buf = new BufferedReader(
							new InputStreamReader(fileIS, "GB2312"));
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
					Message msg = new Message();
					msg.what = 3;
					mHandler.sendMessage(msg);
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				reduceDeviceCount();
          
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