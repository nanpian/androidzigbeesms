package com.rtk.bdtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;  
import java.util.HashMap;
import java.util.List;  

import com.rtk.bdtest.db.DbDeviceHelper;
  
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;  
import android.database.Cursor;
import android.os.Bundle;  
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;  
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;  
import android.widget.EditText;
import android.widget.LinearLayout;  
import android.widget.ListView;  
import android.widget.Toast;

//将zigbee终端id与用户名绑定 

public class BindActivity extends Fragment {  

	    private static final String Tag = "BindActivity";
		private ArrayList<String> namelist;
		private HashMap<String,String> nameId = new HashMap<String, String>();
		OnBindedListener bindedListener;
		
	    @Override
		public void onAttach(Activity activity) {
			// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			bindedListener = (OnBindedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnBindedListener");
		}
		}

		public interface OnBindedListener {
	        public void OnBindedListener(String name, String id );
	    }
		
		   // 构建Runnable对象，在runnable中更新listview界面  
	     Runnable   runnableUI=new  Runnable(){  
	        @Override  
	        public void run() {  
	            //更新界面  
	        	Log.i(Tag,"notify the new data changed listener!");
	        	bindadapter.notifyDataSetChanged();
	        }  
	          
	    };  
	    
		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
		}

		@Override
		public void onPause() {
			// TODO Auto-generated method stub
			mHandler.removeCallbacks(runnableUI);
			super.onPause();
			//readThread.interrupt();
		}

		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
		}

		private  Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case 3: 
	        	     Toast toast = Toast.makeText(getActivity(), "没有导入相关战士姓名数据，请导入", 1000);
	        	     toast.show();
	        	     break;
				}
			}
			
		};
		private ArrayAdapter bindadapter;
		private Handler handler;
		private DbDeviceHelper dbDeviceHelper;
		private Thread readThread;
	    @Override  
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
	            Bundle savedInstanceState) {  
	        return inflater.inflate(R.layout.fragment_detail_layout, container, false);  
	    }  
	      
	    @Override  
	    public void onActivityCreated(Bundle savedInstanceState) {  
	        super.onActivityCreated(savedInstanceState);  
	        dbDeviceHelper = new DbDeviceHelper(getActivity());
	        namelist = new ArrayList<String>();  
	        handler = new Handler();
	        //readThread = new Thread(new Runnable() {
			//	@Override
			//	public void run() {
					File SDFile = android.os.Environment.getExternalStorageDirectory();  
				    String path= SDFile.getAbsolutePath()+File.separator+ "name.txt";
				    Log.d(Tag,"soldier file path is : " + path);
				    try{
				        FileInputStream fileIS = new FileInputStream(path);
				        BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS,"UTF-8"));
				        String readString = new String();
				        while((readString = buf.readLine())!= null){
				                    Log.d(Tag ,"line: " + readString);
				                    namelist.add(readString);
				                    //插入数据库
				                    Cursor cursor = dbDeviceHelper.select(readString);
				                    if(cursor.moveToFirst()) {
				                    } else {
				                    	dbDeviceHelper.insert(readString, null, null, null, null);
				                        Log.i(Tag,"dbDevicehelper insert data is " + readString);
				                    }
				                    mHandler.post(runnableUI);
				        }
				        

				        fileIS.close();
				        } catch (FileNotFoundException e) {
				        	Message msg = new Message();       
				               msg.what = 3;                   
				               mHandler.sendMessage(msg); 
				               e.printStackTrace();
				        } catch (IOException e){
				                e.printStackTrace();
				        }
				
		//		}  	
	     //   }
	     //   );
	        //readThread.start();
	        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.detail_container);  
	          
	        ListView listView = (ListView) getActivity().findViewById(R.id.fragment_detail);  
	        bindadapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_single_choice, namelist);
	        listView.setAdapter(bindadapter); 
	        listView.setOnItemClickListener(new OnItemClickListener() {
				private EditText mId;

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				mId = new EditText(getActivity());
				AlertDialog dialog = new AlertDialog.Builder(getActivity())
						.setTitle(R.string.input_id)
						.setView(mId)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											String idTemp = mId.getText()
													.toString();
											nameId.put(namelist.get(arg2),
													idTemp);
											Toast toast2 = Toast.makeText(
													getActivity(),
													"设备名" + namelist.get(arg2)
															+ "    对应的设备id是"
															+ idTemp,
													Toast.LENGTH_SHORT);
											// 把id对应的设备名字插入数据库，数据库应该有设备名、id、父节点地址、设备类型、设备名字对应的消息记录表
											dbDeviceHelper.update(
													namelist.get(arg2), idTemp);
											String sendbind = namelist.get(arg2)+"," + idTemp;
											//将绑定的id name对应的值传给左边的listview fragment
											bindedListener.OnBindedListener(namelist.get(arg2),idTemp);
											 MainActivity.instance.sendSMS(sendbind,"0000","FFFF","03");
											toast2.show();
										} catch (Exception e) {
											// TODO: handle exception
											e.printStackTrace();
										}
									}
								}).setNegativeButton(R.string.cancel, null)
						.create();
				dialog.show();
			}

		});
	        listView.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					Toast.makeText(getActivity(), "onitemSelected", Toast.LENGTH_SHORT).show();
					
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
	        	
	        });  
	        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//.CHOICE_MODE_SINGLE);  
	        listView.setItemChecked(0, true);  
	          
	        Configuration configuration = getActivity().getResources().getConfiguration();  
	        int ori = configuration.orientation;  
	      
	    }  
	  
	}   
  
  