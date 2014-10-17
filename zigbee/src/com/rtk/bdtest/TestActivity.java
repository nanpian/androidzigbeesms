package com.rtk.bdtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TestActivity extends Fragment implements OnClickListener{
	
	private String currentMode = "";
	private static final String MODE_NORMAL = "nomal";
	private static final String WRITE_ID_SUCCESS = "AA";
	private static final String WRITE_ID_FAIL = "55";
	private static final String GET_FIRMWARE_INFO = "038003";
	private static final String REQUEST_JOIN = "8004";
	private TextView mBDInfo;
	private Button getBDVersionBtn;
	private TextView bdVersionTv;
	private Button firmwareUpdate;
	private Button writeId;
	private Button getFirmwareInfo;
	private Button requestJoin;
	private EditText mId;
	private Button sendSms;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		currentMode = MODE_NORMAL;

		writeId = (Button) getActivity().findViewById(R.id.write_id);
		writeId.setOnClickListener(this);

		requestJoin = (Button) getActivity().findViewById(R.id.request_join);
		requestJoin.setOnClickListener(this);
		
		sendSms = (Button)getActivity().findViewById(R.id.sendmessage);
	    sendSms.setOnClickListener(this);
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
        return inflater.inflate(R.layout.test, container, false);  
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == sendSms) {
			try {
				//sendData2BD(GET_BD_VERSION.getBytes("US-ASCII"));
				MainActivity.instance.sendData2Zigbee(CharConverter.hexStringToBytes(GET_FIRMWARE_INFO));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} else if (v == writeId) {
			mId = new EditText(getActivity());
			AlertDialog dialog = new AlertDialog.Builder(getActivity())
					.setTitle(R.string.input_id)
					.setView(mId)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									try {
										MainActivity.instance.sendData2Zigbee(CharConverter
												.hexStringToBytes("0C8001"
														+ mId.getText()
																.toString()));
									} catch (Exception e) {
										// TODO: handle exception
										e.printStackTrace();
									}
								}
							}).setNegativeButton(R.string.cancel, null)
					.create();
			dialog.show();
		} else if (v == getFirmwareInfo) {
			MainActivity.instance.sendData2Zigbee(CharConverter.hexStringToBytes(GET_FIRMWARE_INFO));
		} else if (v == requestJoin) {
			MainActivity.instance.sendData2Zigbee(CharConverter.hexStringToBytes(REQUEST_JOIN));
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
