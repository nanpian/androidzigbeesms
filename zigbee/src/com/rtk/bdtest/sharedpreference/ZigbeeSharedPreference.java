/**
 * 
 */ 
package com.rtk.bdtest.sharedpreference; 

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/** 
 * @author 作者 E-mail: zdwxxx@qq.com
 * @version 创建时间：2014-12-8 下午12:48:25 
 * 类说明 保存自己的信息
 */

public class ZigbeeSharedPreference {
	
	private static final String PREFERENCES_NAME = "zigbee_sp_person";
	private static final String PREFERENCES_SEIFINFO = "zigbee_sp_self";
	/**自己的信息*/
	public static final String SELF_PADINFO = "self_padinfo";
	/**自己的id**/
	public static final String SELF_ID = "self_id";
	private SharedPreferences sp;
	private Editor editor;
	
	public ZigbeeSharedPreference(Context context) {
		sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		editor = sp.edit();
	}
	
	/**数据*/
	public void setSelfData(String data) {
		editor.putString(SELF_PADINFO, data);
		editor.commit();
	}
	public String getSelfData() {
		return sp.getString(SELF_PADINFO, "");
	}
	
	/**
	*  日期 2014-12-8
	*  作者 lenovo
	*  说明 存放设备自己的id
	*  返回 void
	 * @param id
	 */
	public void setSelfId(String id) {
		editor.putString(SELF_ID, id);
		editor.commit();
	}
	
	/**
	*  日期 2014-12-8
	*  作者 lenovo
	*  说明 获得设备id
	*  返回 String
	 * @param id
	 * @return
	 */
	public String getSeflId() {
		return sp.getString(SELF_ID, "");
	}
}
 