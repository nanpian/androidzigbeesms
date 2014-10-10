package com.rtk.bdtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private static EditText username;
	private static EditText password2;
	private static Button ok;
	private static Button cancel;
	private static String usernamex = null;
	private static String passwordx = null;
	private static final String Tag = "LoginActivity";
	private  Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 3: 
				Toast toast = Toast.makeText(getApplicationContext(), "没有导入用户鉴权文件！", 1000);
       	        toast.show();
        	     break;
			case 2:
		   	     Toast toast2 = Toast.makeText(getApplicationContext(), "导入文件中没有有效用户名和密码！", 1000);
		   	     toast2.show();
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				File SDFile = android.os.Environment.getExternalStorageDirectory();  
			    String path= SDFile.getAbsolutePath()+File.separator+ "authority.txt";
			    Log.d(Tag,"authority file path is : " + path);
			    try{
			        FileInputStream fileIS = new FileInputStream(path);
			        BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
			        String readString = new String();

			        while((readString = buf.readLine())!= null){
			                    Log.d(Tag ,"line: " + readString);
			                    String temp[] = readString.split("=");
			                    if(temp[0].equalsIgnoreCase("username"))usernamex=temp[1];
			                    else if (temp[0].equalsIgnoreCase("password"))passwordx=temp[1];
			                 }
			        if((usernamex==null)||(passwordx==null)) {
			        	Message msg = new Message();       
			               msg.what = 2;                   
			               mHandler.sendMessage(msg);
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
			}
		};
	   mHandler.postDelayed(runnable, 0);
		username = (EditText)findViewById(R.id.username);
		password2 = (EditText)findViewById(R.id.password);
		ok = (Button)findViewById(R.id.ok);
		cancel = (Button)findViewById(R.id.cancel);
		ok.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String name = username.getText().toString();
				String password = password2.getText().toString();
				Log.i(Tag,"Click!the username is "+ name +"the password is "+ password);
				Log.i(Tag,"The authority file,the username is " +usernamex + "the password is " + passwordx);
				if ((usernamex!=null) && (passwordx!=null)) {
					if (name.equalsIgnoreCase(usernamex)&& password.equalsIgnoreCase(passwordx)) {
						Log.i(Tag,"Ready to enter mainactivity!");
						Toast toast = Toast.makeText(LoginActivity.this, "登陆成功，进入主界面", 1000);
		       	        toast.show();
						Intent intent = new Intent();
						intent.setClass(LoginActivity.this,MainActivity.class);
						startActivity(intent);
					} else {
						Toast.makeText(LoginActivity.this, "用户名密码错误，请重新输入", 1000).show();
					}
				} else {
					Toast.makeText(LoginActivity.this, "用户名密码为空！", Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		cancel.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				username.setText("");
				password2.setText("");
			}	
		});
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
