package com.rtk.bdtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
	private static String  male; ;
	private static List<Map<String, Object>> listems;
	private ArrayAdapter bindadapter;

	private String[] name = { "xxxxx", "yyyy", "hello", "zdsfdsf" };

	private String[] desc = { "sdfsdf", "dsfdsf", "dsefrwer", "sdfwer" };
	private static SimpleAdapter simpleadapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		LinearLayout layout = (LinearLayout) getActivity().findViewById(
				R.id.detail_container);

		ListView listView = (ListView) getActivity().findViewById(
				R.id.person_edit);

	    listems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < name.length; i++) {
			Map<String, Object> listem = new HashMap<String, Object>();
			listem.put("name", name[i]);
			listem.put("name", name[i]);
			listem.put("desc", desc[i]);
			listems.add(listem);
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
	}

	public void toast(String str) {
		Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
	     final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	     final int id = (int) info.id;
	     String idx = String.valueOf(id);
	     if (-1 == id) {
	         super.onContextItemSelected(item);
	        }
        switch(item.getItemId()){  
        case MENU_ADD:  
    		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
      		final View myAddView = layoutInflater.inflate(R.layout.add_new, null);
      		RadioGroup group = (RadioGroup)myAddView.findViewById(R.id.radioGroup);
      		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
		             @Override
		             public void onCheckedChanged(RadioGroup arg0, int arg1) {
		                 // TODO Auto-generated method stub
		                 //获取变更后的选中项的ID
		                 int radioButtonId = arg0.getCheckedRadioButtonId();
		                 //根据ID获取RadioButton的实例
		                 RadioButton rb = (RadioButton)myAddView.findViewById(radioButtonId);
		                 male = rb.getText().toString();
		                 //更新文本内容，以符合选中项
		                 //tv.setText("您的性别是：" + rb.getText());
		             }
		         });
      		Dialog alertDialog=new AlertDialog.Builder(getActivity())
      		.setTitle("新增记录")
      		.setIcon(R.drawable.ic_launcher)
      		.setView(myAddView)
      		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
      			public void onClick(DialogInterface dialog, int which) {
      				EditText name=	(EditText)myAddView.findViewById(R.id.name);
      				EditText  id=	(EditText)myAddView.findViewById(R.id.id);
      				EditText type=(EditText)myAddView.findViewById(R.id.type);
      		  		EditText rank=	(EditText)myAddView.findViewById(R.id.rank);
      		  		EditText job=	(EditText)myAddView.findViewById(R.id.job);
      		  		EditText year=	(EditText)myAddView.findViewById(R.id.year);
      		  		RadioButton sex=	(RadioButton)myAddView.findViewById(R.id.radioMale);
      		  		EditText beizhu=	(EditText)myAddView.findViewById(R.id.beizhu);
      		  		String name1 = name.getText().toString();
      		  	    String id1 = id.getText().toString();
      		  	    String type1 = type.getText().toString();
      		        String rank1 = rank.getText().toString();
      		        String job1 = job.getText().toString();
    		  	    String year1 = year.getText().toString();
    		  	    String sex1 =  male;
    		        String beizhu1 = beizhu.getText().toString();
					Toast.makeText(
							getActivity(),
							"name" + name + "id" + id + "type"
									+ type + "rank" + rank
									+ "job" + job + "year"
									+ year + "sex" + sex
									+ "beizhu" + beizhu,
							Toast.LENGTH_LONG).show();
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
					simpleadapter.notifyDataSetChanged();
      			}
      		})
      		.create();
            WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();  
            layoutParams.width = 200;  
            layoutParams.height = LayoutParams.WRAP_CONTENT;  
            alertDialog.getWindow().setAttributes(layoutParams);  
      		alertDialog.show();
            toast( item.getTitle() + idx);
            break;  
        case MENU_MODIFY:  
            toast(item.getTitle() + idx);  
            break;  
        case MENU_DELETE:  
            toast(item.getTitle() + idx);  
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
				Uri.parse("content://Personxxx/person"), true, PersonObserver);
		getActivity().getContentResolver().registerContentObserver(
				Uri.parse("content://Personxxx/person"), true, PersonsObserver);
	}

}
