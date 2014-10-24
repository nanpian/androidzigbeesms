package com.rtk.bdtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.rtk.bdtest.db.PersonProvider;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PersonActivity extends Fragment {

	protected static final int MENU_ADD = 1;
	protected static final int MENU_MODIFY = 2;
	protected static final int MENU_DELETE = 3;
	private static String male = "男";
	private static List<Map<String, Object>> listems;
	private ArrayAdapter bindadapter;
	private final static String Tag = "PersonFragment";

	private String[] name = { "xxxxx", "yyyy", "hello", "zdsfdsf" };

	private String[] desc = { "sdfsdf", "dsfdsf", "dsefrwer", "sdfwer" };
	private static SimpleAdapter simpleadapter;
	private static int idindex;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		LinearLayout layout = (LinearLayout) getActivity().findViewById(
				R.id.detail_container);

		ListView listView = (ListView) getActivity().findViewById(
				R.id.person_edit);
		LinearLayout title = (LinearLayout) getActivity().findViewById(
				R.id.title);

		// 将GPS设备采集的原始GPS坐标转换成百度坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		LatLng sourceLatLng = new LatLng(32.05253311, 118.80744145);
		// sourceLatLng待转换坐标
		converter.coord(sourceLatLng);
		LatLng desLatLng = converter.convert();
		Log.i(Tag, "The dest lat lng is " + desLatLng.latitude
				+ "dest xxxx lng" + desLatLng.longitude);
		Toast.makeText(
				getActivity(),
				"The dest lat lng is " + desLatLng.latitude + "dest xxxx lng"
						+ desLatLng.longitude, Toast.LENGTH_LONG).show();

		listems = new ArrayList<Map<String, Object>>();
		Cursor cursor = getActivity().getContentResolver().query(
				PersonProvider.CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String name = cursor.getString(1);
				String id = cursor.getString(2);
				String type = cursor.getString(3);
				String rank = cursor.getString(4);
				String job = cursor.getString(5);
				String year = cursor.getString(6);
				String sex = cursor.getString(7);
				String beizhu = cursor.getString(8);
				Map<String, Object> listem = new HashMap<String, Object>();
				listem.put("name", name);
				listem.put("id", id);
				listem.put("type", type);
				listem.put("rank", rank);
				listem.put("job", job);
				listem.put("year", year);
				listem.put("sex", sex);
				listem.put("beizhu", beizhu);
				listems.add(listem);
			}
		}

		simpleadapter = new SimpleAdapter(getActivity(), listems,
				R.layout.simple_item, new String[] { "name", "id", "type",
						"rank", "job", "year", "sex", "beizhu" }, new int[] {
						R.id.name, R.id.id, R.id.type, R.id.rank, R.id.job,
						R.id.year, R.id.sex, R.id.beizhu });
		// bindadapter = new ArrayAdapter(getActivity(),
		// android.R.layout.simple_list_item_single_choice, namelist);
		listView.setAdapter(simpleadapter);
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE, MENU_ADD, 0, "增加");
				menu.add(Menu.NONE, MENU_MODIFY, 0, "修改");
				menu.add(Menu.NONE, MENU_DELETE, 0, "删除");
			}
		});
		title.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE, MENU_ADD, 0, "增加");
				menu.add(Menu.NONE, MENU_MODIFY, 0, "修改");
				menu.add(Menu.NONE, MENU_DELETE, 0, "删除");
			}
		});
	}

	public void toast(String str) {
		Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
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
		case MENU_ADD:
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
			final View myAddView = layoutInflater.inflate(R.layout.add_new,
					null);
			RadioGroup group = (RadioGroup) myAddView
					.findViewById(R.id.radioGroup);
			group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup arg0, int arg1) {
					// TODO Auto-generated method stub
					// 获取变更后的选中项的ID
					int radioButtonId = arg0.getCheckedRadioButtonId();
					// 根据ID获取RadioButton的实例
					RadioButton rb = (RadioButton) myAddView
							.findViewById(radioButtonId);
					male = rb.getText().toString();
					// 更新文本内容，以符合选中项
					// tv.setText("您的性别是：" + rb.getText());
				}
			});
			Dialog alertDialog = new AlertDialog.Builder(getActivity())
					.setTitle("新增数据")
					.setIcon(R.drawable.ic_launcher)
					.setView(myAddView)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									EditText name = (EditText) myAddView
											.findViewById(R.id.name);
									EditText id = (EditText) myAddView
											.findViewById(R.id.id);
									EditText type = (EditText) myAddView
											.findViewById(R.id.type);
									EditText rank = (EditText) myAddView
											.findViewById(R.id.rank);
									EditText job = (EditText) myAddView
											.findViewById(R.id.job);
									EditText year = (EditText) myAddView
											.findViewById(R.id.year);
									RadioButton sex = (RadioButton) myAddView
											.findViewById(R.id.radioMale);
									EditText beizhu = (EditText) myAddView
											.findViewById(R.id.beizhu);
									String name1 = name.getText().toString();
									String id1 = id.getText().toString();
									String type1 = type.getText().toString();
									String rank1 = rank.getText().toString();
									String job1 = job.getText().toString();
									String year1 = year.getText().toString();
									String sex1 = male;
									String beizhu1 = beizhu.getText()
											.toString();
									if (name1.equals("") || id1.equals(""))// --------用户名为空时----------------
									{
										new AlertDialog.Builder(getActivity())
												.setTitle("提示：")
												.setIcon(
														android.R.drawable.ic_dialog_info)
												.setMessage("姓名与ID不能为空!")
												.setNegativeButton(
														"确定",
														new DialogInterface.OnClickListener() {
															// @Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																// TODO
																// Auto-generated
																// method stub
															}
														}).show();
										Toast.makeText(getActivity(),
												"请输入姓名及ID", Toast.LENGTH_LONG)
												.show();// 测试
									} else {
										/*
										 * Toast.makeText( getActivity(),
										 * "name1" + name1 + "id1" + id1 +
										 * "type1" + type1 + "rank1" + rank1 +
										 * "job1" + job1 + "year" + year1 +
										 * "sex" + sex1 + "beizhu" + beizhu1,
										 * Toast.LENGTH_LONG).show();
										 */
										Map<String, Object> listem = new HashMap<String, Object>();
										listem.put("name", name1);
										listem.put("id", id1);
										listem.put("type", type1);
										listem.put("rank", rank1);
										listem.put("job", job1);
										listem.put("year", year1);
										listem.put("sex", sex1);
										listem.put("beizhu", beizhu1);
										listems.add(listem);
										ContentValues values = new ContentValues();
										values.put("name", name1);
										values.put("id", id1);
										values.put("typep", type1);
										values.put("rank", rank1);
										values.put("job", job1);
										values.put("year", year1);
										values.put("sex", sex1);
										values.put("beizhu", beizhu1);
										getActivity()
												.getContentResolver()
												.insert(PersonProvider.CONTENT_URI,
														values);
										// simpleadapter.notifyDataSetChanged();
									}
								}
							}).create();
			WindowManager.LayoutParams layoutParams = alertDialog.getWindow()
					.getAttributes();
			layoutParams.width = 200;
			layoutParams.height = LayoutParams.WRAP_CONTENT;
			alertDialog.getWindow().setAttributes(layoutParams);
			alertDialog.show();
			toast(item.getTitle() + idx);
			break;
		case MENU_MODIFY:
			toast(item.getTitle() + idx);
			idindex = id;
			LayoutInflater layoutInflater2 = LayoutInflater.from(getActivity());
			final View myAddView2 = layoutInflater2.inflate(R.layout.add_new,
					null);
			EditText name = (EditText) myAddView2.findViewById(R.id.name);
			EditText id2 = (EditText) myAddView2.findViewById(R.id.id);
			EditText type = (EditText) myAddView2.findViewById(R.id.type);
			EditText rank = (EditText) myAddView2.findViewById(R.id.rank);
			EditText job = (EditText) myAddView2.findViewById(R.id.job);
			EditText year = (EditText) myAddView2.findViewById(R.id.year);
			name.setText(listems.get(id).get("name").toString());
			id2.setText(listems.get(id).get("id").toString());
			RadioButton sex = (RadioButton) myAddView2
					.findViewById(R.id.radioMale);
			EditText beizhu = (EditText) myAddView2.findViewById(R.id.beizhu);
			RadioGroup group2 = (RadioGroup) myAddView2
					.findViewById(R.id.radioGroup);
			group2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup arg0, int arg1) {
					// TODO Auto-generated method stub
					// 获取变更后的选中项的ID
					int radioButtonId = arg0.getCheckedRadioButtonId();
					// 根据ID获取RadioButton的实例
					RadioButton rb = (RadioButton) myAddView2
							.findViewById(radioButtonId);
					male = rb.getText().toString();
					// 更新文本内容，以符合选中项
					// tv.setText("您的性别是：" + rb.getText());
				}
			});
			Dialog alertDialog2 = new AlertDialog.Builder(getActivity())
					.setTitle("修改数据")
					.setIcon(R.drawable.ic_launcher)
					.setView(myAddView2)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									EditText name = (EditText) myAddView2
											.findViewById(R.id.name);
									EditText ids = (EditText) myAddView2
											.findViewById(R.id.id);
									EditText type = (EditText) myAddView2
											.findViewById(R.id.type);
									EditText rank = (EditText) myAddView2
											.findViewById(R.id.rank);
									EditText job = (EditText) myAddView2
											.findViewById(R.id.job);
									EditText year = (EditText) myAddView2
											.findViewById(R.id.year);
									RadioButton sex = (RadioButton) myAddView2
											.findViewById(R.id.radioMale);
									EditText beizhu = (EditText) myAddView2
											.findViewById(R.id.beizhu);
									String name1 = name.getText().toString();
									String id1 = ids.getText().toString();
									String type1 = type.getText().toString();
									String rank1 = rank.getText().toString();
									String job1 = job.getText().toString();
									String year1 = year.getText().toString();
									String sex1 = male;
									String beizhu1 = beizhu.getText()
											.toString();
									if (name1.equals("") || id1.equals(""))// --------用户名为空时----------------
									{
										new AlertDialog.Builder(getActivity())
												.setTitle("提示：")
												.setIcon(
														android.R.drawable.ic_dialog_info)
												.setMessage("姓名与ID不能为空!")
												.setNegativeButton(
														"确定",
														new DialogInterface.OnClickListener() {
															// @Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																// TODO
																// Auto-generated
																// method stub
															}
														}).show();
										Toast.makeText(getActivity(),
												"请输入姓名及ID", Toast.LENGTH_LONG)
												.show();// 测试
									} else {
										ContentValues values = new ContentValues();
										values.put("name", name1);
										values.put("id", id1);
										values.put("typep", type1);
										values.put("rank", rank1);
										values.put("job", job1);
										values.put("year", year1);
										values.put("sex", sex1);
										values.put("beizhu", beizhu1);
										String nametmp = listems.get(idindex).get("name").toString();
										String selection = "name= '" + nametmp + "'";
										getActivity()
												.getContentResolver()
												.update(PersonProvider.CONTENT_URI,
														values,selection,null);
										// simpleadapter.notifyDataSetChanged();
									}
								}
							}).create();
			WindowManager.LayoutParams layoutParams2 = alertDialog2.getWindow()
					.getAttributes();
			layoutParams2.width = 200;
			layoutParams2.height = LayoutParams.WRAP_CONTENT;
			alertDialog2.getWindow().setAttributes(layoutParams2);
			alertDialog2.show();
			toast(item.getTitle() + idx);
			break;
		case MENU_DELETE:

			String nametmp = listems.get(id).get("name").toString();
			toast(item.getTitle() + idx + " name is " + nametmp);
			String selection = "name= '" + nametmp + "'";
			listems.remove(id);
			getActivity().getContentResolver().delete(
					PersonProvider.CONTENT_URI, selection, null);
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.edit, container, false);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		getActivity().getContentResolver().unregisterContentObserver(
				PersonObserver);
		getActivity().getContentResolver().unregisterContentObserver(
				PersonsObserver);
	}

	private ContentObserver PersonObserver = new ContentObserver(new Handler()) {
		public void onChange(boolean selfChange) {
			// 此处可以进行相应的业务处理
			Log.i("PersonProvider", "observer find it!!!");
			listems.clear();
			Cursor cursor = getActivity().getContentResolver().query(
					PersonProvider.CONTENT_URI, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(1);
					String id = cursor.getString(2);
					String type = cursor.getString(3);
					String rank = cursor.getString(4);
					String job = cursor.getString(5);
					String year = cursor.getString(6);
					String sex = cursor.getString(7);
					String beizhu = cursor.getString(8);
					Map<String, Object> listem = new HashMap<String, Object>();
					listem.put("name", name);
					listem.put("id", id);
					listem.put("type", type);
					listem.put("rank", rank);
					listem.put("job", job);
					listem.put("year", year);
					listem.put("sex", sex);
					listem.put("beizhu", beizhu);
					listems.add(listem);
				}
				simpleadapter.notifyDataSetChanged();
			}

		}
	};

	private ContentObserver PersonsObserver = new ContentObserver(new Handler()) {
		public void onChange(boolean selfChange) {
			// 此处可以进行相应的业务处理
		}
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getActivity().getContentResolver().registerContentObserver(
				Uri.parse("content://Personxxx/zigbee_person"), true,
				PersonObserver);
		getActivity().getContentResolver().registerContentObserver(
				Uri.parse("content://Personxxx/person"), true, PersonsObserver);
	}

}
