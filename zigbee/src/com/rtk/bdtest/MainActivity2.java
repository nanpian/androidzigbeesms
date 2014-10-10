package com.rtk.bdtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtk.bdtest.util.CRC16;
import com.rtk.bdtest.util.Device;
import com.rtk.bdtest.*;

public class MainActivity2 extends Fragment implements OnClickListener {
	private static final String TAG = "BDTEST";
	private static final String GET_BD_VERSION = "$PDTINFO,";
	private static final String PANID_STRING = "00000000FF";
	private static final String EXTRA_FILE_CHOOSER = "file_chooser";
	private static final int FILE_SELECT_CODE = 100;
	private static final String WRITE_ID_SUCCESS = "AA";
	private static final String WRITE_ID_FAIL = "55";
	private static final String GET_FIRMWARE_INFO = "8003";
	private static final String REQUEST_JOIN = "8004";

	public static final String WRITE_SUCCESS = "00";
	public static final String CMD_MODE_UPDATE = "80FF";
	public static final String CMD_HAND_SHAKE = "FE004D0449";
	public static final String CMD_ENABLE_RUNNING_MODE = "FE004D034E";
	public static final String CMD_HAND_SHAKE_SUCCESS = "FE014D8400C8";
	public static final String CMD_ENABLE_RUNNING_MODE_SUCCESS = "FE014D8300CF";

	private static ZigbeeApplication mApplication;
	private SerialPort mBDSerialPort;
	private OutputStream mBDOutputStream;
	private InputStream mBDInputStream;
	private byte[] bdBuffer = new byte[1024];

	private SerialPort mZigbeeSerialPort;
	private OutputStream mZigbeeOutputStream;
	private InputStream mZigbeeInputStream;
	private byte[] zigbeeBuffer = new byte[64];

	private BDThread bdThread;
	private ZigbeeThread mZigbeeThread;
	private TextView mBDInfo;
	private Button getBDVersionBtn;
	private TextView bdVersionTv;
	private Button firmwareUpdate;
	private Button writeId;
	private Button getFirmwareInfo;
	private Button requestJoin;
	private EditText mId;
	private SharedPreferences sp;
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

	public static final int crcStartIndex = 90;
	public static final int crcLength = 4;
	int[] res;
	private int[] crcResult;
	ProgressDialog checkBinPD;
	private int resIndex = 0;
	private int verifyIndex = 0;
	private static final String MODE_WRITE = "write";
	private static final String MODE_VERIFY = "verify";
	private static final String MODE_NORMAL = "nomal";
	private static final int RESULT_OK = 0;
	private String currentMode = "";
	private int currentProgress = 0;
	private String firmwareUpdateString = "";
	private boolean isVerifyData = false;
	private String defaultLongitude = "";
	private String defaultLatitude = "";
	private String padAddress = "";

	private ListView deviceList;
	private ArrayList<Device> devices;
	private DeviceListAdapter adapter;

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
				turnOnVoltage();
				break;
			case MSG_SHOW_CHECK_BIN:
				showCheckBinPD();
				break;
			case MSG_DISMISS_CHECK_BIN:
				if ((null != checkBinPD) && (checkBinPD.isShowing())) {
					checkBinPD.dismiss();
				}
				break;
			case MSG_SEND_HANDSHAKE:
				sendHandshake();
				break;
			case MSG_UPDATE_ERROR:
				currentMode = MODE_NORMAL;
				showToast(getString(R.string.update_firmware_error));
				break;
			case MSG_CONTINUE_WRITE:
				continueWriteThread();
				break;
			case MSG_CONTINUE_VERIFY:
				continueVerifyThread();
				break;
			case MSG_TURN_TO_RUNNING_MODE:
				turnOffVoltage();
				break;
			case MSG_SHOW_UPDATE_PROGRESS_DIALOG:
				showUpdate();
				break;
			case MSG_SHOW_CHANGE_MODE_PROGRESS_DIALOG:
				showChangeModePD();
				break;
			case MSG_REFRESH_DIALOG:
				if ((null != checkBinPD) && (checkBinPD.isShowing())) {
					checkBinPD.setProgress(currentProgress);
				}
				break;
			case MSG_SHOW_VERIFY_DIALOG:
				showVerify();
				break;
			case MSG_SEND_LOCATION_TO_ZIGBEE:
				sendLocation();
				break;
			case MSG_NOTIFY_DEVICE_LIST:
				notifyDeviceList(msg.obj.toString());
				break;
			case MSG_SHOW_TOAST:
				showToast(msg.obj.toString());
				break;
			case MSG_REDUCE_DEVICE_COUNT:
				reduceDeviceCount();
				break;
			case MSG_SEND_SMS:
				
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
        return inflater.inflate(R.layout.activity_main2, container, false);  
    }  
    
    @Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
    	super.onActivityCreated(savedInstanceState);  

		currentMode = MODE_NORMAL;

		mApplication = (ZigbeeApplication) getActivity().getApplicationContext();
		initBDSerialPort();
		initZigbeeSerialPort();

		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mBDInfo = (TextView) getActivity().findViewById(R.id.bd_info);
		getBDVersionBtn = (Button) getActivity().findViewById(R.id.get_bd_version_btn);
		getBDVersionBtn.setOnClickListener(this);
		bdVersionTv = (TextView) getActivity().findViewById(R.id.bd_version_tv);

		firmwareUpdate = (Button) getActivity().findViewById(R.id.firmware_update_btn);
		firmwareUpdate.setOnClickListener(this);

		writeId = (Button) getActivity().findViewById(R.id.write_id);
		writeId.setOnClickListener(this);

		getFirmwareInfo = (Button) getActivity().findViewById(R.id.get_firmware_info);
		getFirmwareInfo.setOnClickListener(this);

		requestJoin = (Button) getActivity().findViewById(R.id.request_join);
		requestJoin.setOnClickListener(this);

		bdThread = new BDThread();
		bdThread.setRunFlag(true);
		bdThread.start();

		mZigbeeThread = new ZigbeeThread();
		mZigbeeThread.setRunFlag(true);
		mZigbeeThread.start();

		sendData2Zigbee(CharConverter.hexStringToBytes(PANID_STRING));
		deviceList = (ListView) getActivity().findViewById(R.id.device_list);
		devices = new ArrayList<Device>();
		adapter = new DeviceListAdapter(getActivity(), devices);
		deviceList.setAdapter(adapter);

		sendLocation();
		reduceDeviceCount();

		sendData2Zigbee(CharConverter.hexStringToBytes(GET_FIRMWARE_INFO));

	}

	@Override
	public void onDestroy() {
		currentMode = MODE_NORMAL;
		bdThread.setRunFlag(false);
		mZigbeeThread.setRunFlag(false);
		try {
			// thread.join();
			bdThread.interrupt();
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

		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == getBDVersionBtn) {
			try {
				sendData2BD(GET_BD_VERSION.getBytes("US-ASCII"));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} else if (v == firmwareUpdate) {
			showFileChoose();
		} else if (v == writeId) {
			mId = new EditText(getActivity());
			AlertDialog dialog = new AlertDialog.Builder(getActivity())
					.setTitle(R.string.input_id)
					.setView(mId)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									try {
										sendData2Zigbee(CharConverter
												.hexStringToBytes("8001"
														+ mId.getText()
																.toString()));
									} catch (Exception e) {
										// TODO: handle exception
										e.printStackTrace();
									}
								}
							}).setNegativeButton(R.string.cancel, null)
					.create();
			dialog.show();
		} else if (v == getFirmwareInfo) {
			sendData2Zigbee(CharConverter.hexStringToBytes(GET_FIRMWARE_INFO));
		} else if (v == requestJoin) {
			sendData2Zigbee(CharConverter.hexStringToBytes(REQUEST_JOIN));
		}
	}

	private void initBDSerialPort() {
		try {
				mBDSerialPort = new SerialPort(getActivity().getApplication(), new File(
						"/dev/ttyMT0"), 9600, 0);
			mBDOutputStream = mBDSerialPort.getOutputStream();
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

	private void sendData2BD(byte[] data) {
		try {
			if (null != mBDOutputStream) {
				Log.d(TAG, "write data to bd");
				byte[] temp = new byte[data.length + 5];
				int a = 0;
				for (int i = 0; i < data.length; i++) {
					temp[i] = data[i];
					if (i != 0)
						a = a ^ data[i];
					// Log.d(TAG, "data i = " + i + " content = " +
					// String.format("%c", data[i]));
				}
				int b = a / 16 + 0x30;
				int c = a % 16 + 0x30;
				temp[data.length] = '*';
				temp[data.length + 1] = (byte) b;
				temp[data.length + 2] = (byte) c;
				temp[data.length + 3] = 0x0a;
				temp[data.length + 4] = 0x0d;
				for (int i = 0; i < temp.length; i++) {
					Log.d(TAG,
							"temp i = " + i + " content = "
									+ String.format("%c", temp[i]));
				}
				mBDOutputStream.write(temp);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
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
		if (null != adapter) {
			adapter.notifyDataSetChanged();
		}
	}

	public void reduceDeviceCount() {
		try {
			if ((null != devices) && (devices.size() > 0)) {
				for (int i = 0; i < devices.size(); i++) {
					devices.get(i).count--;
					if (devices.get(i).count <= 0) {
						devices.remove(i);
						i--;
					}
				}
			}
			if (null != adapter) {
				adapter.notifyDataSetChanged();
			}
			handler.sendEmptyMessageDelayed(MSG_REDUCE_DEVICE_COUNT, 3 * 1000);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public void sendLocation() {
		if (currentMode.equals(MODE_NORMAL)) {
			if (!padAddress.equals("")) {
				byte[] temp = new byte[("3002" + padAddress).getBytes().length
						+ (defaultLatitude).getBytes().length];
				// byte[] temp = CharConverter.hexStringToBytes("3002") +
				// (padAddress + defaultLatitude).getBytes();
				System.arraycopy(
						CharConverter.hexStringToBytes("3002" + padAddress), 0,
						temp, 0, 4);
				System.arraycopy((defaultLatitude).getBytes(), 0, temp, 4,
						(defaultLatitude).getBytes().length);
				sendData2Zigbee(temp);
			}
		}
		sendData2Zigbee(CharConverter.hexStringToBytes(GET_FIRMWARE_INFO));
		handler.sendEmptyMessageDelayed(MSG_SEND_LOCATION_TO_ZIGBEE, 5 * 1000);
	}

	public void showFileChoose() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(getActivity(), getString(R.string.sdcard_unmonted_hint),
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(getActivity(), FileChooserActivity.class);
		try {
			startActivityForResult(intent, FILE_SELECT_CODE);
		} catch (ActivityNotFoundException e) {
			// TODO: handle exception
			Toast.makeText(getActivity(), "Please install a file manager",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult");
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (RESULT_OK == resultCode) {
				String path = data.getStringExtra(EXTRA_FILE_CHOOSER);
				if (null != path) {
					File temp = new File(path);
					Log.d(TAG, "selected file name = " + temp.getName());
					Log.d(TAG, "selected file path = " + temp.getPath());
					if (temp.isFile()) {
						Log.d(TAG, "selected file is not directory");
					}
					handler.sendEmptyMessage(MSG_SHOW_CHECK_BIN);
					CheckBinThread c = new CheckBinThread(temp);
					c.start();
				} else {
					Log.d(TAG, "select file error --> path null");
				}
			}
			break;

		default:
			break;
		}
	}

	public void turnOnVoltage() {
		sendData2Zigbee(CharConverter.hexStringToBytes(CMD_MODE_UPDATE));
		handler.sendEmptyMessage(MSG_SHOW_CHANGE_MODE_PROGRESS_DIALOG);
		handler.sendEmptyMessageDelayed(MSG_SEND_HANDSHAKE, 5 * 1000);
	}
	
	public void sendHandshake() {
		sendData2Zigbee(CharConverter.hexStringToBytes(CMD_HAND_SHAKE));
	}

	public void turnOffVoltage() {
		currentMode = MODE_NORMAL;
		sendData2Zigbee(CharConverter.hexStringToBytes(CMD_ENABLE_RUNNING_MODE));
		showToast(getString(R.string.update_firmware_success));
	}

	public void continueWriteThread() {
		WriteDataThread mWriteDataThread = new WriteDataThread();
		mWriteDataThread.start();
	}

	public void continueVerifyThread() {
		currentMode = MODE_VERIFY;
		VerifyThread v = new VerifyThread();
		v.start();
	}

	public void showChangeModePD() {
		if ((null != checkBinPD) && (checkBinPD.isShowing())) {
			checkBinPD.dismiss();
		}
		checkBinPD = ProgressDialog.show(getActivity(),
				getString(R.string.update_firmware_changing_mode),
				getString(R.string.please_wait), true, false);
		checkBinPD.show();
	}

	public void showCheckBinPD() {
		Log.d(TAG, "show pd");
		if ((null != checkBinPD) && (checkBinPD.isShowing())) {
			checkBinPD.dismiss();
		}
		checkBinPD = ProgressDialog.show(getActivity(),
				getString(R.string.check_bin_title),
				getString(R.string.please_wait), true, false);
		checkBinPD.show();
	}

	public void showUpdate() {
		if ((null != checkBinPD) && (checkBinPD.isShowing())) {
			checkBinPD.dismiss();
		}
		/*
		 * checkBinPD = ProgressDialog.show(MainActivity.this,
		 * getString(R.string.updating_firmware),
		 * getString(R.string.please_wait), true, false); checkBinPD.show();
		 */
		checkBinPD = new ProgressDialog(getActivity());
		checkBinPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		checkBinPD.setTitle(getString(R.string.updating_firmware));
		checkBinPD.setIndeterminate(false);
		checkBinPD.setCancelable(false);
		checkBinPD.setProgress(100);
		checkBinPD.show();
	}

	public void showVerify() {
		if ((null != checkBinPD) && (checkBinPD.isShowing())) {
			checkBinPD.dismiss();
		}
		/*
		 * checkBinPD = ProgressDialog.show(MainActivity.this,
		 * getString(R.string.verifing_firmware),
		 * getString(R.string.please_wait), true, false); checkBinPD.show();
		 */
		checkBinPD = new ProgressDialog(getActivity());
		checkBinPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		checkBinPD.setTitle(getString(R.string.verifing_firmware));
		checkBinPD.setIndeterminate(false);
		checkBinPD.setCancelable(false);
		checkBinPD.setProgress(100);
		checkBinPD.show();
	}

	public String getCheckum(String data) {
		int result = 0;
		for (int i = 0; i < data.length(); i = i + 2) {
			result = result ^ Integer.parseInt(data.substring(i, i + 2), 16);
		}
		Log.d(TAG, "checksum = " + CharConverter.intToHexString(result));
		return CharConverter.intToHexString(result);
	}

	public boolean checkVerifyReceivedData(String data) {
		String temp = data.substring(14, 142);
		String verifyString = "";
		for (int i = 0; i < 64; i++) {
			verifyString = verifyString
					+ CharConverter.intToHexString(res[verifyIndex + i]);
		}
		if (temp.equals(verifyString)) {
			return true;
		}
		return false;
	}

	public void checkBin(File temp) {
		try {
			Log.d(TAG, "file length = " + temp.length());
			res = new int[(int) temp.length()];
			crcResult = new int[2];
			int len = 0;
			int tempBuffer = 0;
			InputStream is = new FileInputStream(temp);
			while ((tempBuffer = is.read()) != -1) {
				res[len] = tempBuffer;
				len++;
			}
			int[] tem = new int[len - 4];
			System.arraycopy(res, 0, tem, 0, 0x90);
			System.arraycopy(res, 0x94, tem, 0x90, len - 0x90 - 4);
			System.arraycopy(res, 0x90, crcResult, 0, 2);
			short tempValue = CRC16.crc(tem);
			if (crcResult[0] == Integer.parseInt(
					String.format("%04x", tempValue).substring(2, 4), 16)
					&& crcResult[1] == Integer.parseInt(
							String.format("%04x", tempValue).substring(0, 2),
							16)) {
				Log.d(TAG, "crc check success!!!");
				resIndex = 0;
				// continueWriteThread();
			}
			handler.sendEmptyMessage(MSG_DISMISS_CHECK_BIN);
			handler.sendEmptyMessage(MSG_TURN_ON_VOLTAGE);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void writeDataToFirmware() {
		Log.d(TAG, "writeDataToFirmware resIndex = " + resIndex);
		currentMode = MODE_WRITE;
		String address = String.format("%04x", resIndex / 4);
		String temp = "FE424D01" + address.substring(2, 4)
				+ address.substring(0, 2);
		for (int i = 0; i < 64; i++) {
			temp = temp + CharConverter.intToHexString(res[resIndex + i]);
		}
		temp = temp + getCheckum(temp.substring(2, temp.length()));
		sendData2Zigbee(CharConverter.hexStringToBytes(temp));
		currentProgress = (resIndex * 100 / res.length);
		handler.sendEmptyMessage(MSG_REFRESH_DIALOG);
		Log.d(TAG, "write data = " + temp);
	}

	public void verifyFirmware() {
		Log.d(TAG, "verifyFirmware");
		currentMode = MODE_VERIFY;
		String address = String.format("%04x", verifyIndex / 4);
		String temp = "FE024D02" + address.substring(2, 4)
				+ address.substring(0, 2);
		temp = temp + getCheckum(temp.substring(2, temp.length()));
		sendData2Zigbee(CharConverter.hexStringToBytes(temp));
		currentProgress = (verifyIndex * 100 / res.length);
		handler.sendEmptyMessage(MSG_REFRESH_DIALOG);
		Log.d(TAG, "verify data = " + temp);
	}

	private void showToast(String str) {
		Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
		;
	}

	private void sendData2Zigbee(byte[] data) {
		try {
			if (null != mZigbeeOutputStream) {
				Log.d(TAG, "write data = " + new String(data));
				mZigbeeOutputStream.write(data);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

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
					Thread.sleep(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int len = 0;
				try {
					len = mBDInputStream.read(bdBuffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (len != 0) {
					String s = new String(bdBuffer);
					Log.d(TAG, "s = " + s);
					if (len > 0) {
						Message message = handler
								.obtainMessage(MSG_SHOW_BD_DATA);
						message.obj = s;
						handler.sendMessage(message);
						defaultLatitude = s.substring(20, 48);
						defaultLongitude = s.substring(34, 46);
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
					len = mZigbeeInputStream.read(zigbeeBuffer);
					Log.d("dewei","mZigbeeInputStream is "+mZigbeeInputStream +"len is " +len);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (len > 0) {
					String receivedData = CharConverter.byteToHexString(
							zigbeeBuffer, len);
					Log.d(TAG, "zigbee data = " + receivedData);
					if (isVerifyData) {
						firmwareUpdateString = firmwareUpdateString
								+ receivedData;
						if (firmwareUpdateString.length() == 144) {
							isVerifyData = false;
							handleUpdate(firmwareUpdateString);
							firmwareUpdateString = "";
						} else if (firmwareUpdateString.length() > 144) {
							isVerifyData = false;
							firmwareUpdateString = "";
						}
					} else if ((receivedData.length() >= 4)
							&& receivedData.substring(0, 4).equals("FE43")) {
						isVerifyData = true;
						firmwareUpdateString = firmwareUpdateString
								+ receivedData;
					} else if (receivedData.substring(0, 2).equals("FE")) {
						handleUpdate(receivedData);
					} else {
						handleData(receivedData, zigbeeBuffer);
					}
				}
			}
		}
	}


	
	public void handleUpdate(String data) {
		try {
			if (currentMode.equals(MODE_WRITE)) {
				if (data.length() != 12) {
					handler.sendEmptyMessage(MSG_UPDATE_ERROR);
				} else {
					if (data.substring(8, 10).equals(WRITE_SUCCESS)) {
						handler.removeMessages(MSG_CONTINUE_WRITE);
						if (resIndex + 64 < res.length) {
							resIndex = resIndex + 64;
							handler.sendEmptyMessage(MSG_CONTINUE_WRITE);
						} else {
							// write finish, need to verify
							resIndex = 0;// restore index
							verifyIndex = 0;
							handler.sendEmptyMessage(MSG_CONTINUE_VERIFY);
							handler.sendEmptyMessage(MSG_DISMISS_CHECK_BIN);
							handler.sendEmptyMessage(MSG_SHOW_VERIFY_DIALOG);
							Log.d(TAG, "finish write , start verify");
						}
					}
				}
			} else if (currentMode.equals(MODE_VERIFY)) {
				if (data.length() != 144) {
					handler.sendEmptyMessage(MSG_UPDATE_ERROR);
				} else {
					handler.removeMessages(MSG_CONTINUE_VERIFY);
					if (checkVerifyReceivedData(data)) {
						if (verifyIndex + 64 < res.length) {
							verifyIndex = verifyIndex + 64;
							handler.sendEmptyMessage(MSG_CONTINUE_VERIFY);
						} else {
							// verify finish
							currentMode = MODE_NORMAL;
							verifyIndex = 0;
							resIndex = 0;
							handler.sendEmptyMessage(MSG_DISMISS_CHECK_BIN);
							handler.sendEmptyMessage(MSG_TURN_TO_RUNNING_MODE);
							Log.d(TAG, "verify success !!!");
						}
					} else {
						handler.sendEmptyMessage(MSG_UPDATE_ERROR);
					}
				}
			} else if (currentMode.equals(MODE_NORMAL)) {
				if (data.equals(CMD_HAND_SHAKE_SUCCESS)) {
					resIndex = 0;
					handler.sendEmptyMessage(MSG_CONTINUE_WRITE);
					handler.sendEmptyMessage(MSG_DISMISS_CHECK_BIN);
					handler.sendEmptyMessage(MSG_SHOW_UPDATE_PROGRESS_DIALOG);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void handleData(String data, byte[] b) {
		if (data.equals(WRITE_ID_SUCCESS)) {
			Message message = handler.obtainMessage(MSG_SHOW_TOAST);
			message.obj = getString(R.string.write_id_success);
			handler.sendMessage(message);
		} else if (data.equals(WRITE_ID_FAIL)) {
			Message message = handler.obtainMessage(MSG_SHOW_TOAST);
			message.obj = getString(R.string.write_id_fail);
			handler.sendMessage(message);
		} else if (data.length() == 18) {
			// device heart beat
			Message message = handler.obtainMessage(MSG_NOTIFY_DEVICE_LIST);
			message.obj = data;
			handler.sendMessage(message);
		} else if (data.length() == 16) {
			padAddress = data.substring(4, 8);
		} else {
			Log.d(TAG, "debug info = " + new String(b));
		}
	}

	class CheckBinThread extends Thread {
		private File f;

		public CheckBinThread(File f) {
			this.f = f;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			checkBin(f);
		}
	}

	class WriteDataThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			writeDataToFirmware();
			super.run();
		}
	}

	class VerifyThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			verifyFirmware();
			super.run();
		}
	}

	public class DeviceListAdapter extends BaseAdapter {
		private ArrayList<Device> list;
		private LayoutInflater inflater;
		private Context context;

		public DeviceListAdapter(Context context, ArrayList<Device> list) {
			this.context = context;
			this.inflater = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.device_list_item_layout,
					null);
			holder.address = (TextView) convertView
					.findViewById(R.id.device_address);
			holder.type = (TextView) convertView.findViewById(R.id.device_type);
			holder.id = (TextView) convertView.findViewById(R.id.device_id);
			holder.parentAddress = (TextView) convertView
					.findViewById(R.id.parent_address);
			holder.controlLed = (Button) convertView
					.findViewById(R.id.control_led);

			holder.address.setText(context.getString(R.string.device_address)
					+ list.get(position).deviceAddress);
			holder.type.setText(context.getString(R.string.device_type)
					+ list.get(position).deviceType);
			holder.id.setText(context.getString(R.string.device_id)
					+ list.get(position).deviceID);
			holder.parentAddress.setText(context
					.getString(R.string.device_parent_address)
					+ list.get(position).parentAddress);
			holder.controlLed
					.setOnClickListener(new LedClickListener(position));
			return convertView;
		}

		class LedClickListener implements OnClickListener {
			private int index;

			public LedClickListener(int position) {
				this.index = position;
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendData2Zigbee(CharConverter.hexStringToBytes("3001"
						+ list.get(index).deviceAddress + "01"));
			}

		}

		class ViewHolder {
			TextView address;
			TextView type;
			TextView id;
			TextView parentAddress;
			Button controlLed;
		}

	}

}
