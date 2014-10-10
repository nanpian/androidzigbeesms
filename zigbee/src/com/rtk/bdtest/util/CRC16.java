package com.rtk.bdtest.util;

public class CRC16 {
	private static final String TAG = "CRC16";
	
	public static short crc(int[] data) {
		short initValue = 0;
		//Log.d(TAG, "data length = " + data.length);
		int i = 0;
		for (i = 0; i < data.length; i++) {
			initValue = runPoly(initValue, (byte)data[i]);
		}
		initValue = runPoly(initValue, (byte)0);
		initValue = runPoly(initValue, (byte)0);
		return initValue;
	}

	public static short runPoly(short crc, byte val) {
		try {
			short poly = 0x1021;
			int cnt;
			for (cnt = 0; cnt < 8; cnt++, val <<= 1) {
				int msb;
				if ((crc & 0x8000) != 0) {
					msb = 1;
				} else {
					msb = 0;
				}
				crc <<= 1;
				if ((val & 0x80) != 0)
					crc |= 0x0001;
				if (msb == 1)
					crc ^= poly;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return crc;
	}

}
