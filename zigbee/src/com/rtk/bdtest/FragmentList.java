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
	public static boolean isBind = false;
	
	private  Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 3: 
        	     Toast toast = Toast.makeText(getActivity(), "没有导入相关战士姓名数据，请导入", 1000);
        	     toast.show();
        	     break;
			}
		}
		
	};
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		private EditText mInput;

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equalsIgnoreCase("ACTION_ZIGBEE_SMS")) { 
				String data = intent.getExtras().getString("zigbee_sms");
				Log.i(Tag, "Receive sms broadcast" + data);
				mInput = new EditText(getActivity());
				mInput.setMaxLines(4);
				AlertDialog dialog = new AlertDialog.Builder(getActivity())
						.setTitle("收到短信息")
						.setView(mInput)
						.setPositiveButton("回复",
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
			} else if (intent.getAction().equalsIgnoreCase("ACTION_NOTIFY_DEVICE"))  {
			   String data = intent.getExtras().getString("zigbee_devicelist");
			   Log.i(Tag,"Receive device notify broadcast"+data);
			}			
			//notifyDeviceList(data) ;
		}     
	};

	
	public void notifyDeviceList(String data) {
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
		IntentFilter filter = new IntentFilter("com.rtk.bdtest.service.ZigbeeService.broadcast");
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
					devicetmp.online = false;
					devices.add(devicetmp);
				}
			}
			adapter.notifyDataSetChanged();
		}

	};
	
	private DbDeviceHelper dbDeviceHelper;
	private String send;
	
    @Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);  
        Log.i(Tag,"FragmentList oncreate");
        
        Bundle bundle = getArguments();  
        Log.i(Tag, "bundle is " +bundle);
        if(bundle!=null) {
            send = bundle.getString("send");
        }
        MainActivity.instance.getselfInfo();
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
                Toast.makeText(getActivity(), "The item count" +count+ "is clicked!", Toast.LENGTH_SHORT);
				if (send == null) {
					mInput2 = new EditText(getActivity());
					mInput2.setMaxLines(4);
					String name2 = namelist.get(count);
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
				} else {
					if (send.equals("true")) {
						String name2 = namelist.get(count);
						Fragment detailFragment = new HistoryActivity();
						// 从列表页面传递需要的参数到详情页面
						Bundle mBundle = new Bundle();
						mBundle.putString("record_name", name2);
						mBundle.putString("record_send", "true");
						detailFragment.setArguments(mBundle);
						final FragmentManager fragmentManager = getActivity()
								.getSupportFragmentManager();
						final FragmentTransaction fragmentTransaction = fragmentManager
								.beginTransaction();
						fragmentTransaction.replace(R.id.detail_container,
								detailFragment);

						Toast.makeText(getActivity(), "查找"+name2 +"发送短信息记录", Toast.LENGTH_SHORT);
					} else {
						String name2 = namelist.get(count);
						Fragment detailFragment = new HistoryActivity();
						Bundle mBundle = new Bundle();
						mBundle.putString("record_name", name2);
						mBundle.putString("record_send", "false");
						detailFragment.setArguments(mBundle);
						final FragmentManager fragmentManager = getActivity()
								.getSupportFragmentManager();
						final FragmentTransaction fragmentTransaction = fragmentManager
								.beginTransaction();
						fragmentTransaction.replace(R.id.detail_container,
								detailFragment);
						Toast.makeText(getActivity(), "查找"+name2 +"接收短信息记录", Toast.LENGTH_SHORT);
					}
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
		Thread readThread = new Thread(new Runnable() {
			@Override
			public void run() {
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

			}
		});
		readThread.start();
		
        /*  
        for(int i=0, count=20; i<count; i++){  
        	if(i%2==0) {
            mDataSourceList.add("战士设备" + i + "        在线");  
        	} else 
        	{
            mDataSourceList.add("战士设备" + i + "        离线");  
        	}
        }  
        
        ListView listView = (ListView) getActivity().findViewById(R.id.fragment_list);  
        listView.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mDataSourceList));  
          
        listView.setOnItemClickListener(new OnItemClickListener() {  
  
            @Override  
            public void onItemClick(AdapterView<?> parent, View view,  
                    int position, long id) {  
                Fragment detailFragment = new FragmentDetail();  
               
                Bundle mBundle = new Bundle();  
                mBundle.putString("arg", mDataSourceList.get(position));  
                detailFragment.setArguments(mBundle);  
                  
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();  
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();  
                  
                Configuration configuration = getActivity().getResources().getConfiguration();  
                int ori = configuration.orientation;  
                  
                fragmentTransaction.replace(R.id.detail_container, detailFragment);  
                  
                if(ori == configuration.ORIENTATION_PORTRAIT){  
                    fragmentTransaction.addToBackStack(null);  
                }  
                  
                fragmentTransaction.commit();  
                  
                  
            }  
        });  */
          
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