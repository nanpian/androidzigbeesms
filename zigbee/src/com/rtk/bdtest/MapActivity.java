package com.rtk.bdtest;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MapActivity extends Fragment implements MKOfflineMapListener{
	
	/**
	 * MapView �ǵ�ͼ���ؼ�
	 */
	private MapView mMapView;
	private static  BaiduMap mBaiduMap;
	private static Marker mMarkerA;
	private static Marker mMarkerB;
	private static Marker mMarkerC;
	private static Marker mMarkerD;
	private static Marker mMarkerE;
	private InfoWindow mInfoWindow;
	private MKOfflineMap mOffline = null;
	private final static String Tag = "MapActivity";
	private static final int MSG_UPDATE_SELF_GPS= 0;
	private static final int MSG_UPDATE_OTHER_GPS  = 1;
	private static int count  = 0;

	static BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marka);
	static BitmapDescriptor bdB = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markb);
	private static BitmapDescriptor bdC = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markc);
	static BitmapDescriptor bdD = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markd);
	static BitmapDescriptor bd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_gcoding);
	BitmapDescriptor bdGround = BitmapDescriptorFactory
			.fromResource(R.drawable.ground_overlay);
	
	Handler gpsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_UPDATE_SELF_GPS:
				BitmapDescriptor bdC = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka);

				if (count > 0) {
					LatLng ll = mMarkerE.getPosition();
					LatLng llNew = new LatLng(ll.latitude + 0.005,
							ll.longitude + 0.005);
					mMarkerE.setPosition(llNew);
				} else {
					LatLng jingwei = new LatLng(39.33, 116.400244);
					OverlayOptions selfgps = new MarkerOptions().position(jingwei)
							.icon(bdC).perspective(false).anchor(0.5f, 0.5f)
							.rotate(30).zIndex(7);
				    mMarkerE = (Marker) (mBaiduMap.addOverlay(selfgps));
				MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(jingwei);
				mMapView.getMap().animateMapStatus(status);
				}
				count++;
				gpsHandler.sendEmptyMessageDelayed(MSG_UPDATE_SELF_GPS, 3000);
				break;
			}

			super.handleMessage(msg);
		}

	};
	  
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
        return inflater.inflate(R.layout.activity_overlay, container, false);  
    }  
    
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent gpsIntent) {
			// 收到gps intent以后，发给gps刷新handler，ui显示
			if (gpsIntent.getAction().equals("ACTION_UPDATE_SELF_GPS")) {
				String id = ""; // 得到gps所属的id
				String longitude = gpsIntent.getExtras().getString("longitude");
				String latitude = gpsIntent.getExtras().getString("longitude");
				Log.i(Tag,"update self gps info the longitude is " + longitude + "the latitude is " +latitude );
				String[] jingwei = {longitude,latitude};
				Message gpsMessage = new Message();
				gpsMessage.what = MSG_UPDATE_SELF_GPS;
				gpsMessage.obj = jingwei;
				gpsHandler.sendMessage(gpsMessage);
				
			} else if (gpsIntent.getAction().equals("ACTION_UPDATE_OTHERS_GPS")) {

			}
		}

	};
    
	public void importFromSDCard(View view) {
		int num = mOffline.importOfflineData();
		String msg = "";
		if (num == 0) {
			msg = "没有导入离线包，这可能是离线包放置位置不正确，或离线包已经导入过";
		} else {
			msg = String.format("成功导入 %d 个离线包，可以在下载管理查看", num);
		}
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
	}
	
    @Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);  
		mMapView = (MapView)getActivity().findViewById(R.id.bmapView);
		
		mOffline = new MKOfflineMap();
		mOffline.init(this);
		importFromSDCard(mMapView);
		
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		mBaiduMap.setMapStatus(msu);
		
		
		initOverlay();
		// for test use 
		Message gpsMessage = new Message();
		gpsMessage.what = MSG_UPDATE_SELF_GPS;
		gpsHandler.sendMessageDelayed(gpsMessage, 3000);
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				Button button = new Button(getActivity());
				button.setBackgroundResource(R.drawable.popup);
				OnInfoWindowClickListener listener = null;
				if (marker == mMarkerA || marker == mMarkerD) {
					button.setText("测试用");
					listener = new OnInfoWindowClickListener() {
						public void onInfoWindowClick() {
							LatLng ll = marker.getPosition();
							LatLng llNew = new LatLng(ll.latitude + 0.005,
									ll.longitude + 0.005);
							marker.setPosition(llNew);
							mBaiduMap.hideInfoWindow();
						}
					};
					LatLng ll = marker.getPosition();
					mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
					mBaiduMap.showInfoWindow(mInfoWindow);
				} else if (marker == mMarkerB) {
					button.setText("地址222");
					button.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							marker.setIcon(bd);
							mBaiduMap.hideInfoWindow();
						}
					});
					LatLng ll = marker.getPosition();
					mInfoWindow = new InfoWindow(button, ll, -47);
					mBaiduMap.showInfoWindow(mInfoWindow);
				} else if (marker == mMarkerC) {
					button.setText("地址1");
					button.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							marker.remove();
							mBaiduMap.hideInfoWindow();
						}
					});
					LatLng ll = marker.getPosition();
					mInfoWindow = new InfoWindow(button, ll, -47);
					mBaiduMap.showInfoWindow(mInfoWindow);
				}
				return true;
			}
		});
    }
    
    
	public void initOverlay() {
		// add marker overlay
		LatLng llA = new LatLng(39.963175, 116.400244);
		LatLng llB = new LatLng(39.942821, 116.369199);
		LatLng llC = new LatLng(39.939723, 116.425541);
		LatLng llD = new LatLng(39.906965, 116.401394);

		OverlayOptions ooA = new MarkerOptions().position(llA).icon(bdA)
				.zIndex(9).draggable(true);
		mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
		OverlayOptions ooB = new MarkerOptions().position(llB).icon(bdB)
				.zIndex(5);
		mMarkerB = (Marker) (mBaiduMap.addOverlay(ooB));
		OverlayOptions ooC = new MarkerOptions().position(llC).icon(bdC)
				.perspective(false).anchor(0.5f, 0.5f).rotate(30).zIndex(7);
		mMarkerC = (Marker) (mBaiduMap.addOverlay(ooC));
		OverlayOptions ooD = new MarkerOptions().position(llD).icon(bdD)
				.perspective(false).zIndex(7);
		mMarkerD = (Marker) (mBaiduMap.addOverlay(ooD));

	}

	/**
	 * �������Overlay
	 * 
	 * @param view
	 */
	public void clearOverlay(View view) {
		mBaiduMap.clear();
	}

	/**
	 * �������Overlay
	 * 
	 * @param view
	 */
	public void resetOverlay(View view) {
		clearOverlay(null);
		initOverlay();
	}
    
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		mMapView.onPause();
		gpsHandler.removeMessages(MSG_UPDATE_SELF_GPS);
		getActivity().unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		mMapView.onResume();
		super.onResume();
		IntentFilter filter = new IntentFilter("com.rtk.bdtest.service.BDService.broadcast");
		getActivity().registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mMapView.onDestroy();
		super.onDestroy();
		/*
		bdA.recycle();
		bdB.recycle();
		bdC.recycle();
		bdD.recycle();
		bd.recycle();
		bdGround.recycle();*/
	}


	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}


	@Override
	public void onGetOfflineMapState(int arg0, int arg1) {
		// TODO Auto-generated method stub
		Log.i(Tag,"the arg0 is " + arg0 + "the arg1 is " +arg1);
		
	}  
}
