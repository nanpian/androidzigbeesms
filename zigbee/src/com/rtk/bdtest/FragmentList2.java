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
import com.rtk.bdtest.db.TeamProvider;
import com.rtk.bdtest.sharedpreference.ZigbeeSharedPreference;
import com.rtk.bdtest.util.Device;
import com.rtk.bdtest.util.gpsDevice;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
	public ArrayList<Device> devices;
	public ArrayList<Device> devicesNull = new ArrayList<Device>();
	public Device deviceBtmp;
	public Device deviceB1tmp;
	public static boolean HasInitSelf = false;
	private ArrayList<List<Device>> devicesA = new ArrayList<List<Device>>(); // 所有的设备A
	public ArrayList<Device> devicesB = new ArrayList<Device>(); // 设备B和C
	private DeviceExpandableListAdapter adapter;
	private static final String Tag = "FragmentList";
	public static final int MSG_REDUCE_DEVICE_COUNT = 17;
	private static final int MSG_GET_SELF_ID = 18;
	protected static final int MENU_BEIZHU = 3;
	protected static final int MENU_MODIFY = 0;
	protected static final int MENU_QUERY = 12;
	public static boolean isBind = false;
	public static String padinfo = null;
	public static String selfpadAddress = null;
	private String selfpadId = null;
	private SmsHelper smsHelper;
	private boolean isFirstTime = true;
	private boolean isFirstTime2 = true;
	private boolean isFirstbind = true;
	private static boolean hasInitSystem = false;
	private static final String REQUEST_JOIN = "8004";

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

			// 未知设备栏
			if ((null != devicesNull) && (devicesNull.size() > 0)) {
				for (int i = 0; i < devicesNull.size(); i++) {
					devicesNull.get(i).count--;
					if (devicesNull.get(i).count <= 0) {
						devicesNull.get(i).online = false;
						adapter.notifyDataSetChanged();
					}
				}
			}

			// 心跳为2s*5
			mHandler.sendEmptyMessageDelayed(MSG_REDUCE_DEVICE_COUNT, 3 * 1000);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	private Handler mHandler = new Handler() {
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
			Log.i(Tag, "Receive intent and the action is " + intent.getAction());
			if (intent.getAction().equals("ACTION_ZIGBEE_SMS")) {
				String data = intent.getExtras().getString("zigbee_sms");
				final String addrtmp = intent.getExtras().getString("smsSourAddr");
				final String Idtmp = intent.getExtras().getString("smsSourId");
				String typetmp2 = intent.getExtras().getString("smsType");
				if (typetmp2 == null)
					typetmp2 = "01";

				Log.i(Tag, "zhudewei jiema " + typetmp2 + " fff " + data + "addr" + addrtmp + "Idtmp is" + Idtmp
						
						+ " typetmp is " + typetmp2);
				for (int i = 0; i < devicesB.size(); i++) {
					if (devicesB.get(i).deviceAddress != null) {
						if (devicesB.get(i).deviceAddress.equals(addrtmp)) {
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
					ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "进入系统自毁流程......", true);
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
					System.exit(0);
				} else if (typetmp2.equals("05")) {
					
					Toast.makeText(getActivity(), "已经退网", Toast.LENGTH_LONG).show();
				} else if (typetmp2.equals("07")) {
					Toast.makeText(getActivity(), "密钥已过期！", Toast.LENGTH_LONG).show();
				} else if (typetmp2.equals("01")) {
					smsHelper.insert(devicename, xx, data, "false");
					devicesB.get(0).unread = true; // 显示未读信息图标
					Intent smsintent = new Intent("com.rtk.bdtest.service.ZigbeeService.broadcastMap");
					smsintent.setAction(("ACTION_ZIGBEE_SMS2").toString());
					smsintent.putExtra("zigbee_sms", data);
					smsintent.putExtra("smsSourAddr", addrtmp);
					smsintent.putExtra("smsSourId", Idtmp);
					getActivity().sendBroadcast(smsintent);
				} else if (typetmp2.equals("09")) {
					Toast.makeText(getActivity(), "收到队长绑定信息！", Toast.LENGTH_SHORT);

					// 收到队长绑定信息时，将所有其他队长的信息清掉
					if (isFirstbind) {
					String[] removeSection = { "others" };
					getActivity().getContentResolver().delete(PersonProvider.CONTENT_URI, "beizhu=?", removeSection);
					isFirstbind = false;
					} 
					String bindName = data.substring(4);
					String bindId = data.substring(0, 4);
					Log.i(Tag, "receive bind info from B    " + data + "  bind name is " + bindName + "bindId is " + bindId);
					if ((bindName != null) && (bindId != null)) {
						if (isContainInSQL(bindId)) {
							Log.i(Tag, "receive bind info from B the name update" + data.substring(4));
							Log.i(Tag, "recevie bind info from B the id update" + data.substring(0, 4));
							ContentValues values = new ContentValues();
							values.put("name", data.substring(4));
							values.put("id", data.substring(0, 4));
							ZigbeeSharedPreference zSp = new ZigbeeSharedPreference(getActivity());
							String selfId = zSp.getSeflId();
							if (bindId.equals(selfId)) {
								Log.i(Tag, "self id is " + selfId);
								String selection = "id= '" + data.substring(0, 4) + "'";
								getActivity().getContentResolver().update(PersonProvider.CONTENT_URI, values, selection, null);
							} else {
								values.put("beizhu", "others");
								String selection = "id= '" + data.substring(0, 4) + "'";
								getActivity().getContentResolver().update(PersonProvider.CONTENT_URI, values, selection, null);
							}
						} else {
							Log.i(Tag, "receive insert bind info from B the name " + data.substring(4));
							Log.i(Tag, "recevie insert bind info from B the id" + data.substring(0, 4));
							ContentValues values = new ContentValues();
							values.put("name", data.substring(4));
							values.put("id", data.substring(0, 4));
							ZigbeeSharedPreference zSp = new ZigbeeSharedPreference(getActivity());
							Log.i(Tag, "recevie insert bind info2222 from B the name" + data.substring(4));
							String selfId = zSp.getSeflId();
							if (bindId.equals(selfId)) {
								Log.i(Tag, "self id is " + selfId);
								getActivity().getContentResolver().insert(PersonProvider.CONTENT_URI, values);
							} else {
								Log.i(Tag, "recevie insert bind info3333 from B the name" + data.substring(4));
								values.put("beizhu", "others");
								getActivity().getContentResolver().insert(PersonProvider.CONTENT_URI, values);
							}
							// getActivity().getContentResolver().insert(PersonProvider.CONTENT_URI,
							// values);
						}
					}

				} else if (typetmp2.equals("0B")) {
					Log.i(Tag, "receive A query bind info from B" + data);

					String nameNull = data.substring(4);
					String idNull = data.substring(0, 4);
					Toast.makeText(getActivity(), "查询成功,ID为" +idNull + 
							",名称为"+nameNull, Toast.LENGTH_SHORT).show();
					if (devicesNull!=null && devicesNull.size()>0) {
						for (int k=0; k < devicesNull.size(); k++) {
							if (idNull==null)break;
							if(idNull.equals(devicesNull.get(k).deviceID)) {
								devicesNull.get(k).deviceName = nameNull;
								adapter.notifyDataSetChanged();
							}
						}
					}
/*					if (isContainInSQL(bindId)) {
						ContentValues values = new ContentValues();
						values.put("name", data.substring(4));
						values.put("id", data.substring(0, 4));
						String selection = "id= '" + data.substring(0, 4) + "'";
						getActivity().getContentResolver().update(PersonProvider.CONTENT_URI, values, selection, null);
					} else {
						ContentValues values = new ContentValues();
						values.put("name", data.substring(4));
						values.put("id", data.substring(0, 4));
						getActivity().getContentResolver().insert(PersonProvider.CONTENT_URI, values);
					}*/
				}
			} else if (intent.getAction().equals("ACTION_NOTIFY_DEVICE")) {
				String data = intent.getExtras().getString("zigbee_devicelist");
				Log.i(Tag, "Receive device notify broadcast" + data);
				String type = data.substring(6, 8);
				Log.i(Tag, "The device type is " + type);
				if (type.equals("01"))
					notifiyDeviceC1(data);
				else if (type.equals("02"))
					notifiyDeviceB2(data);
				else {
					notifyDeviceList(data);
				}
			} else if (intent.getAction().equals("ACTION_GET_SELF_INFO")) {
				padinfo = intent.getExtras().getString("self_data");
				ZigbeeSharedPreference zSp = new ZigbeeSharedPreference(getActivity());
				zSp.setSelfData(padinfo);
				zSp.setSelfId(padinfo.substring(10, 14));
				Log.i(Tag, "Receive get self info  intent , the data is " + padinfo);

				notifyDeviceB1(padinfo);

			} else {
			}
			// notifyDeviceList(data) ;
		}
	};

	public boolean isContainInSQL(String deviceId) {
		String selection = "id= '" + deviceId + "'";
		Cursor cursor = null;
		try {
			cursor = getActivity().getContentResolver().query(PersonProvider.CONTENT_URI, null, selection, null, null);
			while (cursor.moveToNext()) {
				String id = cursor.getString(2);
				if (id.equals(deviceId))
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}

		return false;
	}

	public String selectNamewithId(String deviceId) {
		String selection = "id= '" + deviceId + "'";
		Cursor cursor = null;
		try {
			cursor = getActivity().getContentResolver().query(PersonProvider.CONTENT_URI, null, selection, null, null);
			while (cursor.moveToNext()) {
				String id = cursor.getString(2);
				if (id.equals(deviceId))
					return cursor.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return null;
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

	private void notifyDeviceB1(String data) {
		Log.i(Tag, "dewei firsttime");
		if (isFirstTime) {
			MainActivity.instance.sendData2Zigbee(CharConverter.hexStringToBytes(REQUEST_JOIN));
			Log.i(Tag, "first time dewei");
			String devicename = selectNamewithId(data.substring(10, 14));
			Log.i(Tag, "first time dewei devicename" + devicename);
			if (devicename != null) {
				devicesB.get(0).deviceName = devicename;
				devicesB.get(0).deviceID = data.substring(10, 14);
				HasInitSelf = true;
				Log.i(Tag, "deweidewei");
			} else {
				devicesB.get(0).deviceName = "持有人";
				HasInitSelf = true;
			}
		}

		devicesB.get(0).deviceID = data.substring(10, 14);
		selfpadId = devicesB.get(0).deviceID;
		devicesB.get(0).deviceAddress = data.substring(6, 10);
		selfpadAddress = devicesB.get(0).deviceAddress;
		devicesB.get(0).online = true;
		devicesB.get(0).count = 5;
		HasInitSelf = true;
		if (isFirstTime) {
			mHandler.post(runnableUI2);
		}
		isFirstTime = false;
		adapter.notifyDataSetChanged();
		/*
		 * for (int i = 0; i < devicesB.size(); i++) { if
		 * (devicesB.get(i).deviceID != null) { if
		 * (devicesB.get(i).deviceID.equals(data.substring(10,14))) { isContain
		 * = true; devicesB.get(i).deviceName = "本机"+data.substring(6,10);
		 * devicesB.get(i).deviceID = data.substring(10,14);
		 * devicesB.get(i).deviceAddress = data.substring(6, 10);
		 * devicesB.get(i).count = 5; devicesB.get(i).online = true; } } }
		 * 
		 * if (!isContain) { Device deviceB2 = new Device(); deviceB2.count = 5;
		 * deviceB2.deviceName = "持有"+data.substring(6,10);
		 * deviceB2.deviceAddress = data.substring(6, 10); deviceB2.deviceID =
		 * data.substring(10,14); //devicesB.add(deviceB2); }
		 */

	}

	private void notifiyDeviceB2(String data) {
		boolean isContain = false;
		data = data.substring(2, data.length());
		Log.i(Tag, "notifyDeviceB2 is aaaa " + data.substring(10, 14));
		try {
			for (int i = 0; i < devicesB.size(); i++) {
				Log.i(Tag, "notifyDeviceB2 is " + devicesB.get(i).deviceID);
				if (devicesB.get(i).deviceID == null)
					continue;
				if (devicesB.get(i).deviceID.equals(data.substring(10, 14))) {
					isContain = true;
					Log.i(Tag, "notifyDeviceB2 is " + data.substring(10, 14));
					devicesB.get(i).deviceAddress = data.substring(6, 10);
					devicesB.get(i).deviceID = data.substring(10, 14);
					devicesB.get(i).count = 5;
					devicesB.get(i).online = true;
					adapter.notifyDataSetChanged();
				}
			}
			if (!isContain) {
				Device deviceB2 = new Device();
				deviceB2.deviceName = "匿名队长" + data.substring(6, 10);
				if (deviceBtmp != null) {
					if (data.substring(10, 14).equals(deviceBtmp.deviceID)) {
						deviceB2.deviceName = deviceBtmp.deviceName;
					}
				}
				deviceB2.count = 5;
				deviceB2.online = true;
				deviceB2.deviceID = data.substring(10, 14);
				deviceB2.deviceAddress = data.substring(6, 10);
				devicesB.add(deviceB2);
				adapter.notifyDataSetChanged();
			}
			if (isFirstTime2) {
				mHandler.post(runnableUI2);
				isFirstTime2 = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notifiyDeviceC1(String data) {
		try {
			boolean isContain = false;
			data = data.substring(2, data.length());
			Device deviceB = new Device();
			// deviceB.deviceName = "协调器";
			deviceB.deviceID = data.substring(10, 14);
			Log.i(Tag, "devicec id is " + deviceB.deviceID);
			Log.i(Tag, "devicec data is " + data.substring(10, 14));
			deviceB.online = true;
			deviceB.count = 5;
			for (int i = 0; i < devicesB.size(); i++) {
				if (i == 1) {
					isContain = true;
					Log.i(Tag, "devicec contain is " + devicesB.get(i).deviceID);
					devicesB.get(i).deviceID = data.substring(10, 14);
					devicesB.get(i).deviceAddress = data.substring(6, 10);
					devicesB.get(i).count = 5;
					devicesB.get(i).online = true;
					adapter.notifyDataSetChanged();
				}
				if (devicesB.get(i).deviceName != null) {/*
														 * if
														 * (devicesB.get(i).deviceID
														 * .
														 * equals(data.substring
														 * (10, 14))) {
														 * isContain = true;
														 * Log.i(Tag,
														 * "devicec contain is "
														 * +
														 * devicesB.get(i).deviceID
														 * ); devicesB.get(i).
														 * deviceName = "协调器";
														 * devicesB
														 * .get(i).deviceID =
														 * data.substring(10,
														 * 14); devicesB.get(i).
														 * deviceAddress =
														 * data.substring(6,
														 * 10);
														 * devicesB.get(i).count
														 * = 5;
														 * devicesB.get(i).online
														 * = true; adapter.
														 * notifyDataSetChanged
														 * (); }
														 */
				}
			}

			if (!isContain) {
				Device deviceB2 = new Device();
				deviceB2.count = 5;
				deviceB2.online = true;
				//deviceB2.deviceName = "协调器";
				deviceB2.deviceAddress = data.substring(6, 10);
				deviceB2.deviceID = data.substring(10, 14);
				devicesB.add(deviceB2);
				adapter.notifyDataSetChanged();
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void notifyDeviceList(String data) {
		try {
			Log.i(Tag, "notify device list" + data);
			boolean isContain = false;
			if (devices.size() <= 0)
				Toast.makeText(getActivity(), "未导入战士文件或者导入错误", Toast.LENGTH_SHORT);
			// 未知设备栏
			if (devicesNull != null && devicesNull.size() > 0) {
				for (int k = 0; k < devicesNull.size(); k++) {
					if (devicesNull.get(k).deviceID != null) {
						Log.i(Tag,"devicesNull test id is " + devicesNull.get(k).deviceID);
						if (devicesNull.get(k).deviceID.equals(data.substring(12, 16))) {
							isContain = true;
							devicesNull.get(k).deviceID = data.substring(12, 16);
							devicesNull.get(k).deviceType = data.substring(6, 8);
							devices.get(k).parentAddress = data.substring(16, 20);
							// 显示在线
							devicesNull.get(k).online = true;
							devicesNull.get(k).count = 5;
						}
					}
				}
			}

			for (int i = 0; i < devices.size(); i++) {
				Log.i(Tag, "notify device list deviceid " + devices.get(i).deviceID + " xintiao deviceid" + data.substring(12, 16));

				if (devices.get(i).deviceID.equals(data.substring(12, 16))) {
					isContain = true;
					devices.get(i).deviceID = data.substring(12, 16);
					devices.get(i).deviceType = data.substring(6, 8);
					devices.get(i).parentAddress = data.substring(16, 20);
					// 显示在线
					devices.get(i).online = true;
					devices.get(i).count = 5;
					// 如果终端父亲地址不等于路由地址，则后面加上附地址
					if (!devices.get(i).parentAddress.equals(selfpadAddress)) {
						if (devices.get(i).deviceName != null && (!devices.get(i).deviceName.contains("("))) {
							// devices.get(i).deviceName =
							// devices.get(i).deviceName + "(" +
							// devices.get(i).parentAddress + ")";
							// devices.get(i).deviceName =
							// devices.get(i).deviceName +"*其他父地址*";
							String paddr = devices.get(i).parentAddress;
							if (devicesB != null) {
								for (int z = 0; z < devicesB.size(); z++) {
									if (devicesB.get(z).deviceAddress != null && devicesB.get(z).deviceName != null && devicesB.get(z).deviceAddress.equals(paddr)) {
										devices.get(i).deviceName = devices.get(i).deviceName + "(" + devicesB.get(z).deviceName + ")";
									}
								}
							}
						}
					}
					/*
					 * if (devices.get(i).parentAddress.equals(selfpadAddress))
					 * { devices.get(i).online = true; devicesA.set(0, devices);
					 * if (devices.get(i).count < 5) { devices.get(i).count++; }
					 * else { devices.get(i).count = 5; } }
					 */
					adapter.notifyDataSetChanged();
				}
			}

			if (!isContain) {
				Log.i(Tag, "notify device list a1");
				Device devicetmp = new Device();
				devicetmp.deviceAddress = data.substring(6, 10);
				devicetmp.deviceID = data.substring(12, 16);
				devicetmp.deviceType = data.substring(6, 8);
				devicetmp.parentAddress = data.substring(16, 20);
				Log.i(Tag, "notify device list a1 parentaddress" + devicetmp.parentAddress);
				Log.i(Tag, "notify device list a1 selfpadaddress" + selfpadAddress);
				devicetmp.deviceName = "匿名";
				if (devicetmp.parentAddress.equals(selfpadAddress)) {
					Log.i(Tag, "notify device list a11");
					devicetmp.online = true;
					devicetmp.count = 5;
					devicesNull.add(devicetmp);
					// 增加在未知设备栏
					// 未知设备栏
					// devicesB.add(devicetmp);
				}
				// devicesA.set(0, devices);
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
					if (devices.get(i).deviceAddress.equals(data.substring(6, 10))) {
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
					if (devices.get(i).deviceAddress.equals(data.substring(6, 10))) {
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

	void getSelfInfo() {
		MainActivity.instance.getselfInfo();
		Log.i(Tag, "get self address and is!");
		mHandler.sendEmptyMessageDelayed(MSG_GET_SELF_ID, 4 * 1000);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		Log.i(Tag, "FragmentList onpause");
		super.onPause();
		getActivity().getContentResolver().unregisterContentObserver(PersonObserver);
		getActivity().unregisterReceiver(receiver);

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.i(Tag, "FragmentList onresume");
		super.onResume();
		smsHelper = new SmsHelper(getActivity());
		IntentFilter filter = new IntentFilter("com.rtk.bdtest.service.ZigbeeService.broadcast2");
		filter.addAction("ACTION_GET_SELF_INFO");
		filter.addAction("ACTION_NOTIFY_DEVICE");
		filter.addAction("ACTION_ZIGBEE_SMS");
		getActivity().registerReceiver(receiver, filter);
		getActivity().getContentResolver().registerContentObserver(Uri.parse("content://Personxxx/zigbee_person"), true, PersonObserver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(Tag, "FragmentList onCreateView");
		return inflater.inflate(R.layout.fragment_list_layout2, container, false);
	}

	// 更新人员姓名列表
	Runnable runnableUI2 = new Runnable() {

		@Override
		public void run() {
			// 更新界面
			synchronized (this) {
				Log.i(Tag, "notify the new data changed listener!");
				Cursor cursor = null;
				try {
					/*
					 * String selection = "id= '" + deviceId + "'"; Cursor
					 * cursor = null; try { cursor =
					 * getActivity().getContentResolver
					 * ().query(PersonProvider.CONTENT_URI, null, selection,
					 * null, null)
					 */
					Cursor cursor1 = null;
					String beizhu = "others";
					String selection = "beizhu!= '" + beizhu + "'";
					if (HasInitSelf) {
						ArrayList<String> nameAlist = new ArrayList<String>();
						cursor1 = getActivity().getContentResolver().query(PersonProvider.CONTENT_URI, null, selection, null, null);
						try {
							Log.i(Tag, "test remove, enter 1");
							while (cursor1.moveToNext()) {
								Log.i(Tag, "test remove,enter 2");
								Log.i(Tag, "test remove,selfid is " + selfpadId + "  this bind id is " + cursor1.getString(2));
								if ((selfpadId != null) && (cursor1.getString(2).equals(selfpadId)))
									continue;
								nameAlist.add(cursor1.getString(1));
								Log.i(Tag, "test remove , add this " + cursor1.getString(1));
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							cursor1.close();
						}
						if (nameAlist != null && nameAlist.size() > 0) {
							for (int k = 0; k < devices.size(); k++) {
								if (nameAlist.contains(devices.get(k).deviceName)) {
									Log.i(Tag, "test remove,contain this " + devices.get(k).deviceName);
									continue;
								} else {
									// 删除的前提是这个设备不是未知匿名设备，要保留匿名设备
									if ((devices.get(k).deviceName != null) && (!devices.get(k).deviceName.equals("匿名"))) {
										Log.i(Tag, "test remove,remove this " + devices.get(k).deviceName);
										devices.remove(k);
										k = k - 1;
									}
								}
							}
							// devicesA.set(0, devices);
							adapter.notifyDataSetChanged();
						} else {
							// 如果nameAlist为null，说明没有编辑队员中，没有队员，那么将devices全清掉,匿名设备不用清除
							for (int k = 0; k < devices.size(); k++) {
								if ((devices.get(k).deviceName != null) && (!devices.get(k).deviceName.equals("匿名"))) {
									devices.remove(k);
									k = k - 1;
								}
							}
							adapter.notifyDataSetChanged();
						}
					}

					cursor = getActivity().getContentResolver().query(PersonProvider.CONTENT_URI, null, null, null, null);
					deviceB1tmp = null;
					deviceBtmp = null;
					if (cursor != null)
						Log.i(Tag, "cursor is not null");
					else
						Log.i(Tag, "cursor is null");
					boolean hasSelfName = false;
					if (HasInitSelf) {
						if (cursor != null) {
							while (cursor.moveToNext()) {
								Log.i(Tag, "The cursor is  id" + cursor.getString(2));
								// 如果是自己
								if ((selfpadId != null) && (cursor.getString(2).equals(selfpadId))) {
									Log.i(Tag, "the self id is " + cursor.getString(2));
									devicesB.get(0).deviceName = cursor.getString(1);
								} else if (cursor.getString(2).equals("-001")) {
									// -001是协调器
									Log.i(Tag, "the a2 id is " + cursor.getString(2));
									String bindName = cursor.getString(1) + "(协调器)";
									String type = cursor.getString(3);
									updateC1(null, bindName, null);
								} else if (isContainInA2(cursor.getString(2))) {
									Log.i(Tag, "the a2 id is " + cursor.getString(2));
									String bindName = cursor.getString(1);
									String bindId = cursor.getString(2);
									String type = cursor.getString(3);
									Log.i(Tag, "type is" + type);
									updateA2(bindId, bindName, type);
								} else if (isCotainInA1(cursor.getString(2))) {
									Log.i(Tag, "the a1 id is " + cursor.getString(2));
									String bindName = cursor.getString(1);
									String bindId = cursor.getString(2);
									String type = cursor.getString(3);
									Log.i(Tag, "type is" + type);
									updateA1(bindId, bindName, type);
								} else if (cursor.getString(8).equals("others")) {
									// 如果不在A1也不再A2上
									String nametmp = cursor.getString(1);
									Log.i(Tag, "dewei b2 name is " + nametmp);
									String bindid = cursor.getString(2);
									Device devicetmpB = new Device();
									devicetmpB.deviceName = nametmp;
									devicetmpB.deviceID = bindid;
									devicetmpB.online = false;
									devicetmpB.type = cursor.getString(3);
									Log.i(Tag, "type is" + devicetmpB.type);
									devicesB.add(devicetmpB);
								} else {
									// 如果不在A1也不再A2上,新增队员，那么有个问题，原来的队员就要删除掉
									String nametmp = cursor.getString(1);
									Log.i(Tag, "dewei dewei name is " + nametmp);
									String bindid = cursor.getString(2);
									Device devicetmp = new Device();
									devicetmp.deviceName = nametmp;
									devicetmp.deviceID = bindid;
									devicetmp.type = cursor.getString(3);
									Log.i(Tag, "type is" + devicetmp.type);
									devices.add(devicetmp);
								}
							}
							adapter.notifyDataSetChanged();
						}
					} else {
						Log.i(Tag, "The B1 has not instanted!");
					}

					hasInitSystem = true;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					cursor.close();
				}
			}
		}
	};

	public void updateA2(String bindId, String bindName, String type) {
		if (HasInitSelf) {
			if (devicesB.size() > 0) {
				for (int i = 0; i < devicesB.size(); i++) {
					if (devicesB.get(i).deviceID == null) continue;
					if (devicesB.get(i).deviceID.equals(bindId))
						devicesB.get(i).setDeviceName(bindName);
					devicesB.get(i).setDeviceType(type);
				}
			}
		}
	}

	public void updateC1(String bindId, String bindName, String type) {
		if (HasInitSelf) {
			if (devicesB.size() > 0) {
				for (int i = 0; i < devicesB.size(); i++) {
					if (devicesB.get(i).deviceName.contains("协调器")) {
						devicesB.get(i).setDeviceName(bindName);
						devicesB.get(i).setDeviceType(type);
					}
				}
			}
		}
	}

	public void updateA1(String bindId, String bindName, String type) {
		if (HasInitSelf) {
			Log.i(Tag, "tye is " + type);
			if (devices.size() > 0) {
				for (int i = 0; i < devices.size(); i++) {
					if (devices.get(i).deviceID.equals(bindId))
						devices.get(i).setDeviceName(bindName);
					devices.get(i).setDeviceType(type);
				}
			}
		}
	}

	boolean isContainInA2(String bindid) {
		if (HasInitSelf) {
			Log.i(Tag, "is contian A2");
			if (devicesB.size() > 0) {
				for (int i = 0; i < devicesB.size(); i++) {
					if (devicesB.get(i).deviceID != null) {
						if (devicesB.get(i).deviceID.equals(bindid)) {
							Log.i(Tag, "is contain a2222");
							return true;
						}
					}
				}
			}
		} else {
			Log.i(Tag, "is not init self");
			return false;
		}
		return false;
	}

	boolean isCotainInA1(String bindid) {
		if (HasInitSelf) {
			Log.i(Tag, "enter is containina1");
			if (devices.size() > 0) {
				for (int i = 0; i < devices.size(); i++) {
					if (devices.get(i).deviceID.equals(bindid)) {
						Log.i(Tag, "is equal a1");
						return true;
					}
				}
			}
		} else
			return false;
		return false;
	}
	
	boolean isCotainInWeizhi(String bindid) {
		if (HasInitSelf) {
			Log.i(Tag, "enter is containinweizhi");
			if (devicesNull!=null && devicesNull.size() > 0) {
				for (int i = 0; i < devicesNull.size(); i++) {
					if (devicesNull.get(i).deviceID==null)continue;
					if (devicesNull.get(i).deviceID.equals(bindid)) {
						Log.i(Tag, "is equal weizhi");
						return true;
					}
				}
			}
		} else
			return false;
		return false;
	}

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
			Log.i(Tag, "the bundle is not null ,and the argement sendi is " + send);
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
			Log.i(Tag, "Get argument from bindfragment, the bundle is " + bindbundle + " The bindname is " + bindname + " The bind id is " + bindid);
		}
		// Thread readThread = new Thread(new Runnable() {
		// @Override
		// public void run() {
		/*
		 * File SDFile = android.os.Environment.getExternalStorageDirectory();
		 * String path = SDFile.getAbsolutePath() + File.separator + "name.txt";
		 * Log.d(Tag, "soldier file path is : " + path); try { FileInputStream
		 * fileIS = new FileInputStream(path); BufferedReader buf = new
		 * BufferedReader(new InputStreamReader(fileIS, "GB2312")); String
		 * readString = new String(); namelist.clear(); while ((readString =
		 * buf.readLine()) != null) { Log.d(Tag, "line: " + readString);
		 * namelist.add(readString); } if (namelist.size() != 0) {
		 * mHandler.post(runnableUI); } fileIS.close(); } catch
		 * (FileNotFoundException e) { mHandler.post(runnableUI2); Message msg =
		 * new Message(); msg.what = 3; mHandler.sendMessage(msg);
		 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
		 */

		devicesB.clear();
		Device deviceB1 = new Device();

		deviceB1.deviceName = "持有人";
		devicesB.add(deviceB1);
		// Device deviceB2 = new Device();
		// deviceB2.deviceName = "路由器2";
		// devicesB.add(deviceB2);
		Device deviceC1 = new Device();
		deviceC1.deviceName = "协调器";
		devicesB.add(deviceC1);

		Device devicesC2 = new Device();
		devicesC2.deviceID = "****";
		devicesC2.deviceName = "未知设备栏";
		devicesB.add(devicesC2);

		dbDeviceHelper = new DbDeviceHelper(getActivity());
		deviceList = (ExpandableListView) getActivity().findViewById(R.id.groupdevice);
		devices = new ArrayList<Device>();
		devicesA.add(devices);

		devicesA.add(null);

		// 未知设备栏
/*		Device Devicetmpxxx = new Device();
		Devicetmpxxx.deviceName = "test";
		devicesNull.add(Devicetmpxxx);*/
		devicesA.add(devicesNull);

		adapter = new DeviceExpandableListAdapter(getActivity(), devicesB, devicesA);
		deviceList.setAdapter(adapter);

		deviceList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
				// menuinfo该对象提供了选中对象的附加信息
				int type = ExpandableListView.getPackedPositionType(info.packedPosition);
				final int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
				int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
				System.out.println("LongClickListener*type-------------------------" + type);
				System.out.println("LongClickListener*group-------------------------" + group);
				System.out.println("LongClickListener*child-------------------------" + child);
				if (type == 0) {// 分组长按事件
					final EditText mInputGroup = new EditText(getActivity());
					mInputGroup.setMaxLines(4);
					String namettmp = devicesB.get(group).deviceName;
					if (group == 0)
						return;
					AlertDialog dilalog = new AlertDialog.Builder(getActivity()).setTitle("发送短信").setView(mInputGroup).setPositiveButton("发送", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							String sms = mInputGroup.getText().toString();
							String destAddr = devicesB.get(group).deviceAddress;
							String destId = devicesB.get(group).deviceID;
							if (sms==null) {
								Toast.makeText(getActivity(), "输入不能为空", Toast.LENGTH_SHORT).show();
								return ;
							}
							if (sms.length()>20) {
								Toast.makeText(getActivity(), "输入长度不能超过20个字符", Toast.LENGTH_SHORT).show();
								return;
							}
							try {
								MainActivity.instance.sendLongSms(sms, destAddr, destId, null);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Date tmpDate = new Date();
							SimpleDateFormat formatt = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
							String xx = formatt.format(tmpDate);
							smsHelper.insert(devicesB.get(group).deviceName, xx, sms, "true");
						}
					}).setNegativeButton("取消", null).create();
					dilalog.show();

					// menu.add(Menu.NONE, MENU_MODIFY, 0, "修改备注名");
				} else if (type == 1) {// 长按好友列表项
					//如果是本组平板
					if (group == 0) {
						menu.add(Menu.NONE, MENU_MODIFY, 0, "修改备注名");
						idxx = child;
					}
					//如果在未知设备栏
					if (group == 2) {
					   menu.add(Menu.NONE, MENU_QUERY, 1, "查询");
					idxx = child;
					}
				}
			}

		});

		deviceList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> child, View v, int postion, long id) {
				// TODO Auto-generated method stub
				return false;
			}

		});
		deviceList.setOnGroupClickListener(new OnGroupClickListener() {
			private EditText mInput2;

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id) {

				Toast.makeText(getActivity(), "The B device count " + groupPosition + "is clicked! and the name is " + devicesB.get(groupPosition).deviceName, Toast.LENGTH_SHORT);
				FragmentManager rightfm = getActivity().getSupportFragmentManager();
				Fragment rfm = rightfm.findFragmentById(R.id.detail_container);
				if (rfm instanceof MapActivity) {
					String name2 = devicesB.get(groupPosition).deviceAddress;
					MapActivity rfma = (MapActivity) rfm;
					try {
						boolean isCentered = rfma.setCenterIn(name2);
						if (isCentered = true) {
							Toast.makeText(getActivity(), "找到地址" + name2, Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getActivity(), "没有找到地址", Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					/*
					 * mInput2 = new EditText(getActivity());
					 * mInput2.setMaxLines(4); String name2 =
					 * devicesB.get(groupPosition).deviceName; AlertDialog
					 * dialog2 = new
					 * AlertDialog.Builder(getActivity()).setTitle("给" + name2 +
					 * "发送短信息:").setView(mInput2).setPositiveButton("发送", new
					 * DialogInterface.OnClickListener() {
					 * 
					 * @Override public void onClick(DialogInterface dialog, int
					 * which) { String sms = mInput2.getText().toString();
					 * String destAddr =
					 * devicesB.get(groupPosition).deviceAddress; String destId
					 * = devicesB.get(groupPosition).deviceID;
					 * MainActivity.instance.sendSMS(sms, destAddr, destId);
					 * Date tmpDate = new Date(); SimpleDateFormat formatt = new
					 * SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒"); String xx =
					 * formatt.format(tmpDate);
					 * smsHelper.insert(devicesB.get(groupPosition).deviceName,
					 * xx, sms, "true"); } }).setNegativeButton(R.string.cancel,
					 * null).create(); if (!(groupPosition == 0)) {
					 * dialog2.show(); } else { if
					 * (devicesB.get(groupPosition).unread) { //
					 * devicesB.get(groupPosition).unread = false; Bundle
					 * arguments2 = new Bundle();
					 * arguments2.putBoolean("issend", false); Fragment
					 * detailFragment = new HistoryActivity();
					 * detailFragment.setArguments(arguments2); final
					 * FragmentManager fragmentManager =
					 * getActivity().getSupportFragmentManager(); final
					 * FragmentTransaction fragmentTransaction =
					 * fragmentManager.beginTransaction();
					 * fragmentTransaction.replace(R.id.detail_container,
					 * detailFragment); fragmentTransaction.commit(); } else {
					 * 
					 * Toast.makeText(getActivity(), "无未读信息！",
					 * Toast.LENGTH_LONG).show();
					 * 
					 * } }
					 */} else if (rfm instanceof HistoryActivity) {
					String name2 = devicesB.get(groupPosition).deviceName;
					HistoryActivity rfma = (HistoryActivity) rfm;
					if (groupPosition == 0) {
						rfma.slectbyAll();
					} else {
						rfma.selectbyname(name2);
					}
					Toast.makeText(getActivity(), "查找" + name2 + "短信息记录", Toast.LENGTH_SHORT).show();
					return true; // 不弹出子设备列表
				} else {

				}
				return false;
			}
		});
		FragmentManager rightfm = getActivity().getSupportFragmentManager();
		Fragment rfm = rightfm.findFragmentById(R.id.detail_container);
		if (rfm instanceof MapActivity) {
			reduceDeviceCount();
		}
		
		//getselfInfo();
		//MainActivity.instance.sendData2Zigbee(CharConverter.hexStringToBytes(REQUEST_JOIN));
		// mHandler.post(runnableUI2);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MODIFY:
			final EditText mInput3 = new EditText(getActivity());
			mInput3.setText(devices.get(idxx).deviceName);
			if (devices.get(idxx).deviceName.equals("匿名")) {
				isAnonymous = true;
			}
			AlertDialog dialog2 = new AlertDialog.Builder(getActivity()).setTitle("给ID为" + devices.get(idxx).deviceID + "修改备注名").setView(mInput3)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							String namex = mInput3.getText().toString();
							ContentValues values = new ContentValues();
							values.put("name", namex);
							values.put("id", devices.get(idxx).deviceID);
							// devices.get(idxx).setDeviceName(namex);
							if (isAnonymous) {
								isAnonymous = false;
								Log.i(Tag, "dewei is anonymous");
								getActivity().getContentResolver().insert(PersonProvider.CONTENT_URI, values);
							} else {
								isAnonymous = false;
								Log.i(Tag, "dewei is not anonymous");
								String selection = "name= '" + devices.get(idxx).deviceName + "'";
								Log.i(Tag, "dewei the name is " + selection);
								getActivity().getContentResolver().update(PersonProvider.CONTENT_URI, values, selection, null);
							}
							// devices.get(idxx).setDeviceName(namex);
							devicesA.set(0, devices);
							// adapter.notifyDataSetChanged();
						}
					}).setNegativeButton(R.string.cancel, null).create();
			dialog2.show();
			break;
		case MENU_QUERY:
			if (devicesNull != null) {
				if (devicesNull.get(idxx) != null) {
					String idSend = devicesNull.get(idxx).deviceID;
					Toast.makeText(getActivity(), "查询设备id为" + idSend + "的名称", Toast.LENGTH_SHORT).show();
					MainActivity.instance.sendQuerySMS(idSend, "0000", "FFFF");
				}
			}

			break;

		}
		return super.onContextItemSelected(item);
	}

	/**
	 * 
	 * @param msg
	 */
	private void showTost(String msg) {
		Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHandler.removeCallbacks(runnableUI2);
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