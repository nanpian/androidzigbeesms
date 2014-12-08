package com.rtk.bdtest;

import java.util.ArrayList;

import com.rtk.bdtest.db.SmsHelper;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/** 
 * @author 作者 E-mail: zdwxxx@qq.com
 * @version 创建时间：2014-12-8 上午10:36:26 
 * 类说明 历史消息记录中左边的名称栏目
 */

public class HistroyNameFragment extends Fragment {

	private static final String Tag = "HistroyNameFragment";
	private SmsHelper smsHelper;
	public ArrayList<String> histroyNameList;
	private ArrayAdapter historyNameAdater;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		smsHelper = new SmsHelper(getActivity());
		histroyNameList = getNameList();
		if (histroyNameList != null) {
			historyNameAdater = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, histroyNameList);
			ListView listView = (ListView) getActivity().findViewById(R.id.history_device_list);
			listView.setAdapter(historyNameAdater);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					// TODO 点击左侧栏目，需要向右侧信息记录发送intent
					FragmentManager rightfm = getActivity().getSupportFragmentManager();
					Fragment rfm = rightfm.findFragmentById(R.id.detail_container);
					HistoryActivity rfma = (HistoryActivity) rfm;
					String historyName = histroyNameList.get(position);
					if(historyName.equals("自己")) {
						Toast.makeText(getActivity(), "查找自己的信息记录",Toast.LENGTH_SHORT).show();
						rfma.slectbyAll();
					}
					else {
						Toast.makeText(getActivity(), "查找"+historyName+"信息记录", Toast.LENGTH_SHORT).show();
						rfma.selectbyname(historyName);
					}
					}
			});

		}
	}

	/**
	 * 日期 2014-12-8 
	 * 作者 zhudewei 
	 * 说明 获取历史记录左侧名称列表 
	 * 返回 ArrayList<String>
	 * 
	 * @return
	 */
	@SuppressWarnings("null")
	public ArrayList<String> getNameList() {
		Cursor cursor = null;
		ArrayList<String> nameList = new ArrayList<String>();
		nameList.add("自己");
		try {
			cursor = smsHelper.select();
			while (cursor.moveToNext()) {
				String name = cursor.getString(1);
				Log.i(Tag,"the name is "+ name);
	            if(!nameList.contains(name))nameList.add(name);
			}
			if (nameList == null)
				return null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return nameList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i(Tag, "FragmentList onCreateView");
		return inflater.inflate(R.layout.history_fragment_name_list, container, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
