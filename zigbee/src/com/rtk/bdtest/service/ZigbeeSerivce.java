package com.rtk.bdtest.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.rtk.bdtest.CharConverter;
import com.rtk.bdtest.FragmentList_useless;
import com.rtk.bdtest.R;
import com.rtk.bdtest.SerialPort;
import com.rtk.bdtest.ZigbeeApplication;
import com.rtk.bdtest.util.Base64;
import com.rtk.bdtest.util.DesCrypt;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
//import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

/*
 * 问题：1.平板路由设备如何得到其他平板设备以及协调器的心跳
 *           2.平板路由设备如何得到其他平板路由设备的gps广播或者信息
 *           3.平板路由设备给其他平板路由或者协调器发送短信息的时候，如何判断发送成功？
 */

public class ZigbeeSerivce extends Service {

	private static final String TAG = "ZigbeeService";
	private static final boolean EnableDES = true;
	private static final String WRITE_ID_SUCCESS = "AA";
	private static final String WRITE_ID_FAIL = "55";
	public static final String WRITE_SUCCESS = "00";
	public static final String CMD_MODE_UPDATE = "80FF";
	public static final String CMD_HAND_SHAKE = "FE004D0449";
	public static final String CMD_ENABLE_RUNNING_MODE = "FE004D034E";
	public static final String CMD_HAND_SHAKE_SUCCESS = "FE014D8400C8";
	public static final String CMD_ENABLE_RUNNING_MODE_SUCCESS = "FE014D8300CF";
	private static final String GET_FIRMWARE_INFO = "038003";

	private static ZigbeeApplication mApplication;
	private SerialPort mZigbeeSerialPort;
	private OutputStream mZigbeeOutputStream;
	private InputStream mZigbeeInputStream;
	private byte[] zigbeeBuffer = new byte[100];
	private static String receiveData1=null;
	private static List<String> listBuffer = new ArrayList<String>();
	private static String receiveData2=null;
	private static boolean isSms = false;
	private static boolean isGps = false;
	private static boolean isLongSms = false;
	private static String receivedData;
	private static int iCount = 0;

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
	private final static String Tag = "ZigbeeService";
	private String currentMode = "";
	private int currentProgress = 0;
	private String firmwareUpdateString = "";
	private boolean isVerifyData = false;

	// 得到自己这个路由平板的设备id、设备短地址
	public void getselfInfo() {
		sendData2Zigbee(CharConverter.hexStringToBytes(GET_FIRMWARE_INFO));
	}

	public void sendData2Zigbee(byte[] data) {
		try {
			synchronized (mZigbeeSerialPort) {
				if (null != mZigbeeOutputStream) {
					Log.d(TAG,
							"write data = "
									+ CharConverter.byteToHexString(data,
											data.length) + " data byte lenth is" +data.length);
					mZigbeeOutputStream.write(data);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 发送短信，发送短信成功的标志是什么？？？
	public void sendsms2Zigbee2(String data, String destAddr, String destId) {
		/*
		 * Message msg = new Message(); msg.what = MSG_SEND_SMS; msg.obj = data;
		 * handler.sendMessage(msg);
		 */
		Log.i(Tag, "send sms!");
		try {
			// 0x2D为length，3003短信息标志位，00为index，01为包数，destAddr目标短地址
			// destId为目标id，0xffff，0xffff，短信息内容为32字节
			byte[] temp = new byte[45];
			String head = "2D30030001" + destAddr + destId;
			System.arraycopy(CharConverter.hexStringToBytes(head), 0, temp, 0,
					9);
			byte[] souDest = { (byte) 0xff, (byte) 0xff };
			byte[] sourId = { (byte) 0xff, (byte) 0xff };
			System.arraycopy(souDest, 0, temp, 9, 2);
			System.arraycopy(sourId, 0, temp, 11, 2);

			byte[] sms = data.getBytes("UTF-8");
			int smslength = sms.length + 2;
			String l = String.format("%02x", smslength);

			System.arraycopy(CharConverter.hexStringToBytes(l), 0, temp, 13, 1); // 字串长度
			System.arraycopy(CharConverter.hexStringToBytes("01"), 0, temp, 14,
					1);// 类型为01
			String head2 = "2D30030001" + destAddr + destId + l + "03";
			System.arraycopy(sms, 0, temp, 15, sms.length);
			Log.i(Tag, "The sms send data head is " + head2);
			Log.i(Tag, "The sms send data string is " + temp);
			sendData2Zigbee(temp);
			// sendData2Zigbee(data.getBytes("UTF-8"));
			// sendData2Zigbee(data.getBytes("Unicode"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	// 发送短信，发送短信成功的标志是什么？？？
	public void sendsms2Zigbee(String data, String destAddr, String destId) {
		/*
		 * Message msg = new Message(); msg.what = MSG_SEND_SMS; msg.obj = data;
		 * handler.sendMessage(msg);
		 */
		Log.i(Tag, "send sms!");
		try {
			// 0x2D为length，3003短信息标志位，00为index，01为包数，destAddr目标短地址
			// destId为目标id，0xffff，0xffff，短信息内容为32字节
			byte[] temp = new byte[45];
			String head = "2D30030001" + destAddr + destId;
			System.arraycopy(CharConverter.hexStringToBytes(head), 0, temp, 0,
					9);
			byte[] souDest = { (byte) 0xff, (byte) 0xff };
			byte[] sourId = { (byte) 0xff, (byte) 0xff };
			System.arraycopy(souDest, 0, temp, 9, 2);
			System.arraycopy(sourId, 0, temp, 11, 2);

			byte[] sms = data.getBytes("UTF-8");
			int smslength = sms.length + 2;
			String l = String.format("%02x", smslength);

			System.arraycopy(CharConverter.hexStringToBytes(l), 0, temp, 13, 1); // 字串长度
			System.arraycopy(CharConverter.hexStringToBytes("01"), 0, temp, 14,
					1);// 类型为01
			String head2 = "2D30030001" + destAddr + destId + l + "01";
			System.arraycopy(sms, 0, temp, 15, sms.length);
			Log.i(Tag, "The sms send data head is " + head2);
			Log.i(Tag, "The sms send data string is " + temp);
			sendData2Zigbee(temp);
			// sendData2Zigbee(data.getBytes("UTF-8"));
			// sendData2Zigbee(data.getBytes("Unicode"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	Runnable gpsRunnable = new Runnable() {
		@Override
		public void run() {
			// 每隔5s发送一个gps请求
			Message msg = new Message();
			msg.what = MSG_SEND_GPS_TOALL;
			Log.i(Tag, "send gps to all,the message is MSG_SEND_GPS_TOALL");
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
				Intent intent1 = new Intent(
						"com.rtk.bdtest.service.ZigbeeService.broadcast2");
				intent1.setAction(("ACTION_GET_SELF_INFO").toString());
				intent1.putExtra("self_data", msg.obj.toString());
				sendBroadcast(intent1);
				Log.i(Tag, "send!!!!data!!!!");
				break;
			case MSG_NOTIFY_DEVICE_LIST: {
				// 收到刷新用户设备列表信息，发送广播给activity
				Intent intent = new Intent(
						"com.rtk.bdtest.service.ZigbeeService.broadcast");
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
				Log.i(Tag, "send gps to all");
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};
	private static String typexxx="01";

	public void handleGps(String data){
		if(data.substring(2, 6).equals("3002")){
			Log.i(Tag, "Receive beidou broadcast information! and data is "
					+ data);
			//String beidou = new String(data);
			String duanAddress = data.substring(6,10);
			byte[] gpsdata = CharConverter.hexStringToBytes(data);

				String beidous = new String(gpsdata);
				Log.i(Tag, "Receive beidous data is " +beidous );
				String beidou = beidous.substring(5, beidous.length());
				Intent intent3 = new Intent(
						"com.rtk.bdtest.service.BDService.broadcast");
				intent3.setAction(("ACTION_UPDATE_GPS_INFO").toString());
				intent3.putExtra("gps", beidou);
				intent3.putExtra("address", duanAddress);
				sendBroadcast(intent3);
				Log.i(Tag, "send!!!!data!!!!");


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
		} else if (data.length() == 20) {
			// device heart beat
			Log.i(Tag, "the device heart beat is " + data);
			Message message = handler.obtainMessage(MSG_NOTIFY_DEVICE_LIST);
			message.obj = data;
			handler.sendMessage(message);
		} else if (data.length() == 18) {
			// make route id and address
			String padAddress = data.substring(6, 10);
			Log.i(Tag, "send data !!!!");
			Message message = handler.obtainMessage(MSG_GET_SELF_INFO);
			message.obj = data;
			handler.sendMessage(message);
		} else if (data.substring(2, 6).equals("3002")) {
			Log.i(Tag, "Receive beidou broadcast information! and data is "
					+ data);
			String beidou = data.substring(6, data.length());
			Intent intent3 = new Intent(
					"com.rtk.bdtest.service.BDService.broadcast");
			intent3.setAction(("ACTION_UPDATE_GPS_INFO").toString());
			intent3.putExtra("gps", beidou);
			sendBroadcast(intent3);
			Log.i(Tag, "send!!!!data!!!!");
		} else if (data.substring(2, 6).equals("3003")) {
			// 收到短信息处理！
			Log.i(Tag, "sms data received is sms " + data);

			String smsSourAddr = data.substring(18, 22);
			String smsSourId = data.substring(22, 26);
			String length = data.substring(26, 28);
			byte[] smslength = CharConverter.hexStringToBytes(length);
			int smslenth = smslength[0] & 0xff;
			Log.i(Tag, "sms length is " + smslenth);
			typexxx = data.substring(28, 30);
			
			String smsReceive = data.substring(30, data.length());
			Log.i(Tag,"the string is " + smsReceive + "type is " + typexxx);
			//byte[] temp2 = smsReceive.getBytes();
			byte[] temp2 = CharConverter.hexStringToBytes(smsReceive);
			byte[] tmp2 = new byte[smslenth-2+1];
			  System.arraycopy(temp2 , 0, tmp2, 0, smslenth-2);
			String tmpStr1 = new String(temp2);
			Base64 base64 = new Base64();
			byte[] tmpbyte = base64.decode(tmpStr1);
			if (EnableDES) {
				//byte[] temp3 = Base64.decode(temp2,  Base64.DEFAULT);
				String tmpdecode = CharConverter.byteToHexString(tmpbyte,tmpbyte.length);
				Log.i(Tag,"base64 decode is " + tmpdecode);
			    DesCrypt DesCryptInstance = new DesCrypt();
			    byte[] smsdatatmp = null;
				try {
				    if(((ZigbeeApplication) getApplication()).getKey()!=null) {
				    	String key = (String) ((ZigbeeApplication) getApplication()).getKey();
					     smsdatatmp = DesCryptInstance.decrypt(tmpbyte, key);
				    } else {
					     smsdatatmp = DesCryptInstance.decrypt(tmpbyte,"hellomys");
				    }
				    Log.i(Tag,"sms desecret is " + CharConverter.byteToHexString(smsdatatmp,smsdatatmp.length));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    smsReceive = new String(smsdatatmp);
			} 
//String smsutf8 = new String(temp, "utf-8");
			String smsutf8 = smsReceive;
			Log.i(Tag, "the sms utf8 is" + smsutf8);
			// 收到短信息，发送广播给activity，分两种情况处理，第一种是gps信息，第二种是普通短信息！！！！！！！
			Intent smsintent = new Intent(
					"com.rtk.bdtest.service.ZigbeeService.broadcast");
			smsintent.setAction(("ACTION_ZIGBEE_SMS").toString());
			smsintent.putExtra("zigbee_sms", smsutf8);
			smsintent.putExtra("smsSourAddr", smsSourAddr);
			smsintent.putExtra("smsSourId", smsSourId);
			//typexxx = "01";
			smsintent.putExtra("smsType", typexxx);
			Log.i(Tag,"broadcast info "+ smsutf8+" "+ typexxx);
			sendBroadcast(smsintent);
		} else {
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
					synchronized (mZigbeeSerialPort) {
						receivedData = CharConverter.byteToHexString(
								zigbeeBuffer, len);
						if (receivedData.length() > 8) {
							Log.d(Tag,
									"receivedData is " + receivedData
											+ "The substring"
											+ receivedData.substring(2, 6)
											+ " isSms" + isSms+" The length is" + receivedData.length());
							if (receivedData.substring(2, 6).equals("3003") && receivedData.length()>=90) {
								isSms = true;
								Log.i(Tag,"receivedData is finished!,and receivedData is " +receivedData);
							}
							if ((receivedData.substring(2, 6).equals("3003"))
									&& (!isSms)) {
								receiveData1 = receivedData;
								isSms = true;
							} else if (isSms) {
								Log.i(Tag, "isSms" + isSms + "");
								receiveData1 = receiveData1 + receivedData;
								isSms = false;
								Log.d(Tag, " sms data received is "
										+ receiveData1);
								byte[] tmp = CharConverter
										.hexStringToBytes(receiveData1
												.substring(8, 10));
								int countOfPackage = 1;
								countOfPackage = (int) tmp[0];
								Log.i(Tag,"The count of package is "+ countOfPackage);
								// 如果长度大于1，则需要合包
								if ((countOfPackage > 1)
										&& (iCount < countOfPackage)) {
									isLongSms = true;
									iCount = iCount + 1;
									listBuffer.add(receiveData1);
									receiveData1 = null;
                                    if(iCount>=countOfPackage) {
                                    	iCount = 0;
                                    	Log.i(Tag,"handle long sms");
    									handleLongSMS(listBuffer);
                                    }
								} else {
									// 如果长度等于1，说明短信内容较短，直接处理此包
									isLongSms = false;
									handleData(receiveData1, zigbeeBuffer);
								}
							}else if ((receivedData.substring(2, 6).equals("3002"))
									&& (!isGps)) {
								receiveData2 = receivedData;
							    isGps = true;
								Log.i(Tag, "gps data is" + receiveData2);
							} else if (isGps) {
								Log.i(Tag, "isGps" + isGps + "");
								receiveData2 = receiveData2 + receivedData;
								String lenstr = receiveData2.substring(0, 2);
								byte[]  gpslength = CharConverter.hexStringToBytes(lenstr);
								int gpslenth2 = gpslength[0];
								Log.i(Tag,"gps data length is" +gpslenth2 );
								if( CharConverter.hexStringToBytes(receiveData2).length>=gpslenth2){
								    isGps = true;
								    Log.i(Tag,"The gps whole data is "+receiveData2 );
									Log.i(Tag, "gps data is" + receiveData2);
									isGps = false;
									handleGps(receiveData2);
								}

							} else {
								Log.d(TAG,
										"zigbee data = " + receivedData
												+ "    The length is "
												+ receivedData.length());
								handleData(receivedData, zigbeeBuffer);

							}
						} 
					}
				}
			}
		}
	}
	
	private void handleGps() {
		
	}

	private void handleLongSMS(List<String> listBuffer2) {
		// TODO Auto-generated method stub
		String smsSourAddr = listBuffer2.get(0).substring(18, 22);
		String smsSourId = listBuffer2.get(0).substring(22, 26);
		String type = listBuffer2.get(0).substring(28, 30);
		int smslenghtTotal = 0;
		for (int i = 0; i < listBuffer2.size(); i++) {
			String length = listBuffer2.get(i).substring(26, 28);
			Log.i(Tag,"The long sms content is "+listBuffer2.get(i)+" The long sms length is" + length);
			byte[] smslength = CharConverter.hexStringToBytes(length);
			int smslenth2 = smslength[0] - 2;// 内容要减掉类型和长度字节
			Log.i(Tag, "long sms length is " + smslenth2);
			smslenghtTotal = smslenghtTotal + smslenth2;
		}
		byte[] temp = new byte[smslenghtTotal + 1];
		for (int i = 0; i < listBuffer2.size(); i++) {
			// 收到短信息处理！
			String smsReceive = listBuffer2.get(i).substring(30,
					listBuffer2.get(i).length());
			Log.i(Tag, "smsdata is " + smsReceive + "addr" + smsSourAddr
					+ "id " + smsSourId + " content" + smsReceive + "type is "
					+ type);
			byte[] bytes = CharConverter.hexStringToBytes(smsReceive);

			String length = listBuffer2.get(i).substring(26, 28);
			byte[] smslength = CharConverter.hexStringToBytes(length);
			int smslenth2 = smslength[0]- 2;// 内容要减掉类型和长度字节
			System.arraycopy(bytes, 0, temp, i * 30, smslenth2);
		}
        listBuffer.clear();
		try {
			String smsutf8 = "解码错误";
			if (EnableDES) {
				Log.i(Tag,"the long sms data is "+ temp);
				 String xx = new String(temp);
				 Base64 xx2 = new Base64();
				 
				byte[] temp2 = xx2.decode(xx);
			    DesCrypt DesCryptInstance = new DesCrypt();
			    byte[] smsdatatmp;
			    if(((ZigbeeApplication) getApplication()).getKey()!=null) {
			    	String key = (String) ((ZigbeeApplication) getApplication()).getKey();
				     smsdatatmp = DesCryptInstance.decrypt(temp2, key);
			    } else {
				     smsdatatmp = DesCryptInstance.decrypt(temp2, ("hellomys"));
				     Log.i(Tag,"sms desecret is " + CharConverter.byteToHexString(smsdatatmp,smsdatatmp.length));
			    }
               // smsutf8 = new String(smsdatatmp,"utf-8");
				     smsutf8 = new String(smsdatatmp);
			} else {
			   smsutf8 = new String(temp, "utf-8");
			}

			Log.i(Tag, "the sms utf8 is" + smsutf8);
			// 收到短信息，发送广播给activity，分两种情况处理，第一种是gps信息，第二种是普通短信息！！！！！！！
			Intent smsintent = new Intent(
					"com.rtk.bdtest.service.ZigbeeService.broadcast");
			smsintent.setAction(("ACTION_ZIGBEE_SMS").toString());
			smsintent.putExtra("zigbee_sms", smsutf8);
			smsintent.putExtra("smsSourAddr", smsSourAddr);
			smsintent.putExtra("smsSourId", smsSourId);
			smsintent.putExtra("smsType", type);
			sendBroadcast(smsintent);
		} catch (Exception e) {
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

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(Tag, "zigbeeservice created!");
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
