package com.rtk.bdtest;

import android.annotation.SuppressLint;

public class CharConverter {

	@SuppressLint("DefaultLocale")
	public static String byteToHexString(byte[] data, int size) {
		String receivedData = "";
		for (int i = 0; i < size; i++) {
			String hex = Integer.toHexString(data[i] & 0xff);
			if (hex.length() == 1)
				hex = '0' + hex;
			receivedData += hex.toUpperCase();
		}
		return receivedData;
	}

	@SuppressLint("DefaultLocale")
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals(""))
			return null;
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
			// Log.d("AppsActivity", "d " + i + " = " + d[i]);
		}
		return d;
	}

	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String intToHexString(int value) {
		String str = Integer.toHexString(value);
		if (str.length() == 1)
			str = "0" + str;
		str = str.toUpperCase();
		return str;
	}

	public static String stringToAscii(String value) {
		StringBuffer sbu = new StringBuffer();
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i != chars.length - 1) {
				sbu.append((int) chars[i]).append("");
			} else {
				sbu.append((int) chars[i]);
			}
		}
		return sbu.toString();
	}

	public static String asciiToString(String value)
	 {
	  StringBuffer sbu = new StringBuffer();
	  String[] chars = value.split(",");
	  for (int i = 0; i < chars.length; i++) {
	   sbu.append((char) Integer.parseInt(chars[i]));
	  }
	  return sbu.toString();
	 }
}
