package com.rtk.bdtest;

import java.util.ArrayList;
import java.util.List;

import com.rtk.bdtest.db.SmsHelper;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class HistoryActivity extends Fragment {

	private final static String Tag = "HistoryActivity";
	private SmsHelper smsHelper;
	private ArrayAdapter historyAdater;
	public static  ArrayList<String> list;
	public static HistoryActivity instance;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_detail_layout, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager rightfm = getActivity()
				.getSupportFragmentManager();
		instance  = (HistoryActivity) rightfm.findFragmentById(R.id.detail_container);
		Log.d(Tag,Tag+"created!");
		Bundle bundle = getArguments();
		boolean isSend = bundle.getBoolean("issend",false);
		String username = bundle.getString("record_name",null);
		Log.i(Tag, "The argument issend is " + isSend);
		Log.i(Tag,"The argument name is " + username);
		list = new ArrayList<String>();
		smsHelper = new SmsHelper(getActivity());
		list.clear();
		Cursor cursor = null;
		if (username != null) {
			cursor = smsHelper.select(username );
		} else {
			cursor = smsHelper.select();
		}
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String name = cursor.getString(1);
				String time = cursor.getString(2);
				String text = cursor.getString(4);
				list.add("姓名:" + name + "  时间:" + time + " 内容:" + text);
			}
		}
/*		if (isSend) {
			Cursor cursor = null;
			if (username != null) {
				cursor = smsHelper.select(username , "true");
			} else {
				cursor = smsHelper.selectsend("true");
			}
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(1);
					String time = cursor.getString(2);
					String text = cursor.getString(4);
					list.add("姓名:" + name + "  时间:" + time + " 内容:" + text);
				}
			}
		} else {
			Cursor cursor = null;
			if (username != null) {
				cursor = smsHelper.select(username , "false");
			} else {
				cursor = smsHelper.selectsend("false");
			}
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(1);
					String time = cursor.getString(2);
					String text = cursor.getString(4);
					list.add("姓名:" + name + "  时间:" + time + " 内容:" + text);
				}
			}
		}*/
/*		if(list.size()==0) {
			smsHelper.insert("张三", "201210121", "你好测试信息", "true");
			smsHelper.insert("张三", "201210121", "你好测试信息2", "false");
			smsHelper.insert("李四", "201210121", "你好测试信息", "false");
			smsHelper.insert("李四", "201210121", "你好测试信息22222", "true");
			if(isSend) {
				Cursor cursor = smsHelper.selectsend("true");
				while(cursor.moveToNext()) {
					String name = cursor.getString(1);
					String time = cursor.getString(2);
					String text = cursor.getString(4);
					list.add("姓名:"+name+"  时间:"+time +" 内容:"+text);
				}
			} else {
				Cursor cursor = smsHelper.selectsend("false");
				while(cursor.moveToNext()) {
					String name = cursor.getString(1);
					String time = cursor.getString(2);
					String text = cursor.getString(4);
					list.add("姓名:"+name+"  时间:"+time +" 内容:"+text);
				}
			}
		}*/

		LinearLayout layout = (LinearLayout) getActivity().findViewById(
				R.id.detail_container);
		
	   historyAdater = 	new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1, list);

		ListView listView = (ListView) getActivity().findViewById(
				R.id.fragment_detail);
		listView.setAdapter(historyAdater);



	}
	
	public void selectbyname ( ArrayList<String> list2) {
		list.clear();
		list = (ArrayList<String>) list2.clone();
		
		historyAdater.notifyDataSetChanged();
		Log.i(Tag,"notify the data change because the user click fragmentlist");
		
	}

}