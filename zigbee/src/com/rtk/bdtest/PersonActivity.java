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
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
	private static int typeCount = 0 ;
	private static int rankCount = 0;
	private static int jobCount = 0;
	private static final String[] typelist ={"人","装备","物资"};   
	private static final String[] ranklist = {"团长","营长","班长","队员"};
	private static final String[] joblist = {"团长","营长","班长","队员"};

	private Spinner spinnertype;
	private Spinner spinnerrank;
	private Spinner spinnerjob;
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapter2;
	private ArrayAdapter<String> adapter3;
	private static Button report;
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
        report = (Button)getActivity().findViewById(R.id.report);
        report.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					StringBuilder send2C = new StringBuilder();
					for(int i = 0 ;i <listems.size();i++) {
			 			String send2Ctmp = listems.get(i).get("name")+"#"+ listems.get(i).get("id")+"#"+ listems.get(i).get("beizhu")+"#"+ listems.get(i).get("job")+"#"+ listems.get(i).get("sex")+"#"+ listems.get(i).get("rank")+
			 					"#"+ listems.get(i).get("type");
			 			Log.i(Tag,"send2c tmp " + i +" string is" + send2Ctmp);
			 			send2C.append(send2Ctmp);
					}
					String send2Cstring = send2C.toString();
					MainActivity.instance.sendPersonInfo(send2Cstring, "0000", "FFFF", "03");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
        	
        });
		// 将GPS设备采集的原始GPS坐标转换成百度坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		double[] jingweitest2 = {118.48446487,32.031519864};
		double  tmp2 = (jingweitest2[1]%1)*100/60;
		int tmp1 = (int) (jingweitest2[1]/1);
		Toast.makeText(getActivity(), "tmp xxx" + tmp2 + "tmpxxxx"+ tmp1, Toast.LENGTH_LONG);
		jingweitest2[1] = (jingweitest2[1]%1)*100/60 + (int)jingweitest2[1]/1;
		jingweitest2[0] =  (jingweitest2[0]%1)*100/60 + (int)jingweitest2[0]/1;
		LatLng sourceLatLng = new LatLng(jingweitest2[1], jingweitest2[0]);
		// sourceLatLng待转换坐标
		converter.coord(sourceLatLng);
		LatLng desLatLng = converter.convert();
		Log.i(Tag, "The dest lat lng is " + desLatLng.latitude
				+ "dest xxxx lng" + desLatLng.longitude);
		Toast.makeText(
				getActivity(),
				"The dest lat lng is " + sourceLatLng.latitude + "dest xxxx lng"
						+ sourceLatLng.longitude, Toast.LENGTH_LONG).show();

		String[] selectSection = { "others" };
		Cursor cursor = getActivity().getContentResolver().query(
				PersonProvider.CONTENT_URI, null, "beizhu!=?", selectSection, null);
		if (cursor != null) {
			listems = new ArrayList<Map<String, Object>>();
			while (cursor.moveToNext()) {
				String name = cursor.getString(1);
				String id = cursor.getString(2);
				String type = cursor.getString(3);
				String rank = cursor.getString(4);
				String job = cursor.getString(5);
				String year = cursor.getString(6);
				String sex = cursor.getString(7);
				String beizhu = cursor.getString(8);
				if(beizhu!=null && beizhu.contains("others"))continue;
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
	  
			spinnertype = (Spinner)myAddView.findViewById(R.id.type111);
			spinnerrank = (Spinner)myAddView.findViewById(R.id.rank111);
			spinnerjob = (Spinner)myAddView.findViewById(R.id.job111);
			spinnertype.setPrompt("请选择类别：");
			spinnerrank.setPrompt("请选择军衔");
			spinnerjob.setPrompt("请选择职务");
			//将可选内容与ArrayAdapter连接起来   
			adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,typelist);   	    
			//设置下拉列表的风格   
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   		        
			//将adapter 添加到spinner中   
			spinnertype.setAdapter(adapter);   	     
			//添加事件Spinner事件监听     
			spinnertype.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub	
					typeCount = arg2;
					if(typeCount>0) {
						spinnerrank.setVisibility(View.GONE);
						spinnerjob.setVisibility(View.GONE);
						EditText year = (EditText) myAddView
								.findViewById(R.id.year);
						year.setVisibility(View.GONE);
						RadioButton sex = (RadioButton) myAddView
								.findViewById(R.id.radioMale);
						sex.setVisibility(View.GONE);
						RadioButton sex2 = (RadioButton) myAddView
								.findViewById(R.id.radioFemale);
						sex2.setVisibility(View.GONE);
					} else {
						spinnerrank.setVisibility(View.VISIBLE);
						spinnerjob.setVisibility(View.VISIBLE);
						EditText year = (EditText) myAddView
								.findViewById(R.id.year);
						year.setVisibility(View.VISIBLE);
						RadioButton sex = (RadioButton) myAddView
								.findViewById(R.id.radioMale);
						sex.setVisibility(View.VISIBLE);
						RadioButton sex1 = (RadioButton) myAddView
								.findViewById(R.id.radioFemale);
						sex1.setVisibility(View.VISIBLE);
					}
 				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					typeCount = 0;
				}
			});

			//设置默认值   
			spinnertype.setVisibility(View.VISIBLE);   
			//将可选内容与ArrayAdapter连接起来   
			adapter2 = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,ranklist);   	    
			//设置下拉列表的风格   
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   		        
			//将adapter 添加到spinner中   
			spinnerrank.setAdapter(adapter2);   	     
			//添加事件Spinner事件监听     
			spinnerrank.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					rankCount = arg2;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					rankCount = 0;
				}
				
			});   	  
			//设置默认值   
			spinnerrank.setVisibility(View.VISIBLE);   
			//将可选内容与ArrayAdapter连接起来   
			adapter3 = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,joblist);   	    
			//设置下拉列表的风格   
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   		        
			//将adapter 添加到spinner中   
			spinnerjob.setAdapter(adapter3);   	     
			//添加事件Spinner事件监听     
			spinnerjob.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					jobCount = arg2;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					jobCount = 0;
				}
			}); 
			//设置默认值   
			spinnerjob.setVisibility(View.VISIBLE);   
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
/*									EditText type = (EditText) myAddView
											.findViewById(R.id.type);
									EditText rank = (EditText) myAddView
											.findViewById(R.id.rank);
									EditText job = (EditText) myAddView
											.findViewById(R.id.job);*/
									EditText year = (EditText) myAddView
											.findViewById(R.id.year);
									RadioButton sex = (RadioButton) myAddView
											.findViewById(R.id.radioMale);
									EditText beizhu = (EditText) myAddView
											.findViewById(R.id.beizhu);
									String name1 = name.getText().toString();
									String id1 = id.getText().toString();
									String type1 = typelist[typeCount];
                                    String rank1 = null;
                                    String job1 = null;
                                    String year1 = null;
                                    String sex1 = null;
									if (type1.equals("人")) {
										rank1 = ranklist[rankCount];
										 job1 = joblist[jobCount];
									    year1 = year.getText()
												.toString();
										sex1 = male;
									} 
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
										//人员姓名#设备ID#班级#职务#性别#军衔#类别
							 			String send2C = name1+"#"+id1+"#"+beizhu1+"#"+job1+"#"+sex1+"#"+rank1+
							 					"#"+type1;
							 		/*	try {
											MainActivity.instance.sendPersonInfo(send2C, "0000", "FFFF", "03");
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}*/
							 			// MainActivity.instance.sendSMS(sendbind,"0000","FFFF","03");
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
	  
			spinnertype = (Spinner)myAddView2.findViewById(R.id.type111);
			spinnerrank = (Spinner)myAddView2.findViewById(R.id.rank111);
			spinnerjob = (Spinner)myAddView2.findViewById(R.id.job111);
			spinnertype.setPrompt("请选择类别：");
			spinnerrank.setPrompt("请选择军衔");
			spinnerjob.setPrompt("请选择职务");
			//将可选内容与ArrayAdapter连接起来   
			adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,typelist);   	    
			//设置下拉列表的风格   
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   		        
			//将adapter 添加到spinner中   
			spinnertype.setAdapter(adapter);   	     
			//添加事件Spinner事件监听     
			spinnertype.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub	
					typeCount = arg2;
					if(typeCount>0) {
						spinnerrank.setVisibility(View.GONE);
						spinnerjob.setVisibility(View.GONE);
						EditText year = (EditText) myAddView2
								.findViewById(R.id.year);
						year.setVisibility(View.GONE);
						RadioButton sex = (RadioButton) myAddView2
								.findViewById(R.id.radioMale);
						RadioButton sex2 = (RadioButton) myAddView2
								.findViewById(R.id.radioFemale);
						sex.setVisibility(View.GONE);
						sex2.setVisibility(View.GONE);
					} else {
						spinnerrank.setVisibility(View.VISIBLE);
						spinnerjob.setVisibility(View.VISIBLE);
						EditText year = (EditText) myAddView2
								.findViewById(R.id.year);
						year.setVisibility(View.VISIBLE);
						RadioButton sex = (RadioButton) myAddView2
								.findViewById(R.id.radioMale);
						sex.setVisibility(View.VISIBLE);
						RadioButton sex2 = (RadioButton) myAddView2
								.findViewById(R.id.radioFemale);
						sex2.setVisibility(View.VISIBLE);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					typeCount = 0;
				}
			});

			//设置默认值   
			spinnertype.setVisibility(View.VISIBLE);   
			//将可选内容与ArrayAdapter连接起来   
			adapter2 = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,ranklist);   	    
			//设置下拉列表的风格   
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   		        
			//将adapter 添加到spinner中   
			spinnerrank.setAdapter(adapter2);   	     
			//添加事件Spinner事件监听     
			spinnerrank.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					rankCount = arg2;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					rankCount = 0;
				}
				
			});   	  
			//设置默认值   
			spinnerrank.setVisibility(View.VISIBLE);   
			//将可选内容与ArrayAdapter连接起来   
			adapter3 = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,joblist);   	    
			//设置下拉列表的风格   
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   		        
			//将adapter 添加到spinner中   
			spinnerjob.setAdapter(adapter3);   	     
			//添加事件Spinner事件监听     
			spinnerjob.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					jobCount = arg2;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					jobCount = 0;
				}
			}); 
			//设置默认值   
			spinnerjob.setVisibility(View.VISIBLE);   
			EditText name = (EditText) myAddView2.findViewById(R.id.name);
			EditText id2 = (EditText) myAddView2.findViewById(R.id.id);

			//EditText type = (EditText) myAddView2.findViewById(R.id.type);
			//EditText rank = (EditText) myAddView2.findViewById(R.id.rank);
			//EditText job = (EditText) myAddView2.findViewById(R.id.job);
		    EditText year = (EditText) myAddView2.findViewById(R.id.year);
			if (listems!=null) {
				if(listems.size()>0) {
			    name.setText(listems.get(id).get("name").toString());
			    id2.setText(listems.get(id).get("id").toString());
				}
			}
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
/*									EditText type = (EditText) myAddView2
											.findViewById(R.id.type);
									EditText rank = (EditText) myAddView2
											.findViewById(R.id.rank);
									EditText job = (EditText) myAddView2
											.findViewById(R.id.job);*/
									EditText year = (EditText) myAddView2
											.findViewById(R.id.year);
									RadioButton sex = (RadioButton) myAddView2
											.findViewById(R.id.radioMale);
									EditText beizhu = (EditText) myAddView2
											.findViewById(R.id.beizhu);
									String name1 = name.getText().toString();
									String id1 = ids.getText().toString();
									String type1 = typelist[typeCount];
									String rank1 = null;
									String job1 = null;
									String year1 = null;
									String sex1 = null;
									if (type1.equals("人")) {
										rank1 = ranklist[rankCount];
										job1 = joblist[jobCount];
										year1 = year.getText().toString();
										sex1 = male;
									}
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
										//人员姓名#设备ID#班级#职务#性别#军衔#类别
							 			String send2Cx = name1+"#"+id1+"#"+beizhu1+"#"+job1+"#"+sex1+"#"+rank1+
							 					"#"+type1;
							 	/*		try {
											MainActivity.instance.sendPersonInfo(send2Cx, "0000", "FFFF", "03");
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}*/
										// simpleadapter.notifyDataSetChanged();
									}
								}
							}).create();
			WindowManager.LayoutParams layoutParams2 = alertDialog2.getWindow()
					.getAttributes();
			layoutParams2.width = 200;
			layoutParams2.height = LayoutParams.WRAP_CONTENT;
			alertDialog2.getWindow().setAttributes(layoutParams2);
			if (listems.size()>0) {	
				alertDialog2.show();
			} else {
				Toast.makeText(getActivity(), "没有数据，无法修改", Toast.LENGTH_LONG).show();
			}

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
