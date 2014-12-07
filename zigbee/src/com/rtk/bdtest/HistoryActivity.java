package com.rtk.bdtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rtk.bdtest.db.SmsHelper;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HistoryActivity extends Fragment {

	private final static String Tag = "HistoryActivity";
	protected static final int MENU_ROM = 2;
	private SmsHelper smsHelper;
	private ArrayAdapter historyAdater;
	public static ArrayList<String> list;
	public static ArrayList<Integer> list2;
	private static List<Map<Integer, String>> listems = new ArrayList<Map<Integer, String>>();
	public static HistoryActivity instance;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_detail_layout, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager rightfm = getActivity().getSupportFragmentManager();
		instance = (HistoryActivity) rightfm.findFragmentById(R.id.detail_container);
		Log.d(Tag, Tag + "created!");
		Bundle bundle = getArguments();
		boolean isSend = bundle.getBoolean("issend", false);
		String username = bundle.getString("record_name", null);
		Log.i(Tag, "The argument issend is " + isSend);
		Log.i(Tag, "The argument name is " + username);
		list = new ArrayList<String>();
		list2 = new ArrayList<Integer>();
		smsHelper = new SmsHelper(getActivity());
		list.clear();
		list2.clear();
		Cursor cursor = null;
		if (username != null) {
			cursor = smsHelper.select(username);
		} else {
			cursor = smsHelper.select();
		}
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int idc = cursor.getInt(0);
				String name = cursor.getString(1);
				String time = cursor.getString(2);
				String text = cursor.getString(4);
				list.add("姓名:" + name + "  时间:" + time + " 内容:" + text);
				list2.add(idc);
				// listems.add(idc)
			}
		}
		/*
		 * if (isSend) { Cursor cursor = null; if (username != null) { cursor =
		 * smsHelper.select(username , "true"); } else { cursor =
		 * smsHelper.selectsend("true"); } if (cursor != null) { while
		 * (cursor.moveToNext()) { String name = cursor.getString(1); String
		 * time = cursor.getString(2); String text = cursor.getString(4);
		 * list.add("姓名:" + name + "  时间:" + time + " 内容:" + text); } } } else {
		 * Cursor cursor = null; if (username != null) { cursor =
		 * smsHelper.select(username , "false"); } else { cursor =
		 * smsHelper.selectsend("false"); } if (cursor != null) { while
		 * (cursor.moveToNext()) { String name = cursor.getString(1); String
		 * time = cursor.getString(2); String text = cursor.getString(4);
		 * list.add("姓名:" + name + "  时间:" + time + " 内容:" + text); } } }
		 */
		// if(list.size()==0) {
		// smsHelper.insert("张三", "201210121", "你好测试信息", "true");
		// smsHelper.insert("张三", "201210121", "你好测试信息2", "false");
		// smsHelper.insert("李四", "201210121", "你好测试信息", "false");
		// smsHelper.insert("李四", "201210121", "你好测试信息22222", "true");
		// }
		/*
		 * if(isSend) { Cursor cursor = smsHelper.selectsend("true");
		 * while(cursor.moveToNext()) { String name = cursor.getString(1);
		 * String time = cursor.getString(2); String text = cursor.getString(4);
		 * list.add("姓名:"+name+"  时间:"+time +" 内容:"+text); } } else { Cursor
		 * cursor = smsHelper.selectsend("false"); while(cursor.moveToNext()) {
		 * String name = cursor.getString(1); String time = cursor.getString(2);
		 * String text = cursor.getString(4); list.add("姓名:"+name+"  时间:"+time
		 * +" 内容:"+text); } } }
		 */

		LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.detail_container);

		historyAdater = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);

		ListView listView = (ListView) getActivity().findViewById(R.id.fragment_detail);
		listView.setAdapter(historyAdater);
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE, MENU_ROM, 0, "删除");
			}
		});

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int id;
		if (info != null) {
			id = (int) info.id;
		} else {
			id = 0;
		}

		String idx = String.valueOf(id);
		if (-1 == id) {
			super.onContextItemSelected(item);
		}
		switch (item.getItemId()) {
		case MENU_ROM:
			int idc = list2.get(id);
			list.remove(id);
			historyAdater.notifyDataSetChanged();
			smsHelper.delete(idc);
			Toast.makeText(getActivity(), "id is " + id + " sid is " + idc, Toast.LENGTH_LONG).show();
			break;
		}
		return super.onContextItemSelected(item);
	}

	public void selectbyname(String name2) {

		Cursor cursor = null;
		Log.i(Tag,"dewei select by name");
		list2.clear();
		list.clear();
		try {
			cursor = smsHelper.select(name2);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int idc = cursor.getInt(0);
					String name = cursor.getString(1);
					String time = cursor.getString(2);
					String text = cursor.getString(4);
					Log.i(Tag, "姓名:" + name + "  时间:" + time + " 内容:" + text);
					list.add("姓名:" + name + "  时间:" + time + " 内容:" + text);
					list2.add(idc);
					// listems.add(idc)
				}
			}
			historyAdater.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
	
		Log.i(Tag, "notify the data change because the user click fragmentlist");

	}

	public void slectbyAll() {
		Cursor cursor = null;
		Log.i(Tag,"dewei select by all");
		list2.clear();
		list.clear();
		try {
			cursor = smsHelper.select();
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int idc = cursor.getInt(0);
					String name = cursor.getString(1);
					String time = cursor.getString(2);
					String text = cursor.getString(4);
					list.add("姓名:" + name + "  时间:" + time + " 内容:" + text);
					list2.add(idc);
					// listems.add(idc)
				}
				historyAdater.notifyDataSetChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
	}
}
