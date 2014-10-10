package com.rtk.bdtest;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.ServiceManager;
import android.util.Log;
import com.android.server.IRGKService;
import android.content.Context;

public class SerialPort {

	private static final String TAG = "SerialPort";
	private Context context;

	/*
	 * Do not remove or rename the field mFd: it is used by native method
	 * close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	public SerialPort(Context context, File device, int baudrate, int flags)
			throws SecurityException, IOException {
		this.context = context;
		Log.d("dewei","Serial init is " + mFileInputStream);
		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Log.d("dewei","cannot open " + mFileInputStream);
				sendCommand(1000,
						new String[] { "chmod", "777", "/dev/ttyMT0" });
				sendCommand(1000,
						new String[] { "chmod", "777", "/dev/ttyMT1" });
				/*
				 * Process su; su = Runtime.getRuntime().exec("/system/bin/su");
				 * String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" +
				 * "exit\n"; su.getOutputStream().write(cmd.getBytes()); if
				 * ((su.waitFor() != 0) || !device.canRead() ||
				 * !device.canWrite()) { throw new SecurityException(); }
				 */
			} catch (Exception e) {
				Log.d("dewei","Serial error is " + mFileInputStream);
				e.printStackTrace();
				throw new SecurityException();
			}
		}
		Log.d("dewei","Serial init open is " + mFileInputStream);
		try {
			mFd = open(device.getAbsolutePath(), baudrate, flags);
			if (mFd == null) {
				Log.e(TAG, "native open returns null");
				Log.d("dewei","Serial open is " + mFd);
				throw new IOException();
			}
			Log.d("dewei","Serial open is " + mFd);
			mFileInputStream = new FileInputStream(mFd);
			Log.d("dewei","mBDInputStream2 is " + mFileInputStream);
			mFileOutputStream = new FileOutputStream(mFd);
		} catch (Throwable e) {
			// TODO: handle exception
			Log.d("dewei","Serial open error is " + mFileInputStream);
			e.printStackTrace();
		}
		
	}

	private int sendCommand(int timeout_seconds, String[] cmdLine) {
		try {
			IRGKService service = IRGKService.Stub.asInterface(ServiceManager
					.getService("zigbeeservice"));
			//IRGKService service = (IRGKService)context.getSystemService("rgkservice");
			if (service == null) {
				Log.d(TAG, "failed to get service");
				return -1;
			}
			return service.runSysCommand(timeout_seconds, cmdLine);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
			return -1;
		}
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate,
			int flags);

	public native void close();

	static {
		try {
			System.loadLibrary("serial_port");
		} catch (Throwable e) {
			// TODO: handle exception
		}
	}
}
