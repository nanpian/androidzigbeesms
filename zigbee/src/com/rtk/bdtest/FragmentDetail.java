package com.rtk.bdtest;

import java.util.ArrayList;  
import java.util.List;  
  
import android.content.res.Configuration;  
import android.os.Bundle;  
import android.support.v4.app.Fragment;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.ArrayAdapter;  
import android.widget.LinearLayout;  
import android.widget.ListView;  
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
 
/** 
 * ����Fragment 
 * @author mining 
 * 
 */  
public class FragmentDetail extends Fragment {  
	
	/**
	 * MapView �ǵ�ͼ���ؼ�
	 */
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Marker mMarkerA;
	private Marker mMarkerB;
	private Marker mMarkerC;
	private Marker mMarkerD;
	private InfoWindow mInfoWindow;

	// ��ʼ��ȫ�� bitmap ��Ϣ������ʱ��ʱ recycle
	BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marka);
	BitmapDescriptor bdB = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markb);
	BitmapDescriptor bdC = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markc);
	BitmapDescriptor bdD = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markd);
	BitmapDescriptor bd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_gcoding);
	BitmapDescriptor bdGround = BitmapDescriptorFactory
			.fromResource(R.drawable.ground_overlay);

  
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
 
        
	       return inflater.inflate(R.layout.activity_overlay, container, false);  
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

		// add ground overlay
		LatLng southwest = new LatLng(39.92235, 116.380338);
		LatLng northeast = new LatLng(39.947246, 116.414977);
		LatLngBounds bounds = new LatLngBounds.Builder().include(northeast)
				.include(southwest).build();

		OverlayOptions ooGround = new GroundOverlayOptions()
				.positionFromBounds(bounds).image(bdGround).transparency(0.8f);
		mBaiduMap.addOverlay(ooGround);

		MapStatusUpdate u = MapStatusUpdateFactory
				.newLatLng(bounds.getCenter());
		mBaiduMap.setMapStatus(u);

		mBaiduMap.setOnMarkerDragListener(new OnMarkerDragListener() {
			public void onMarkerDrag(Marker marker) {
			}

			public void onMarkerDragEnd(Marker marker) {
				/*Toast.makeText(
						 FragmentDetail.this,
						"��ק������λ�ã�" + marker.getPosition().latitude + ", "
								+ marker.getPosition().longitude,
						Toast.LENGTH_LONG).show();*/
			}

			public void onMarkerDragStart(Marker marker) {
			}
		});
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
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);  
          
        //��ȡ�б?���Ĳ���  
        Bundle bundle = getArguments();  
        String date = bundle.getString("arg");  
          
        List<String> list = new ArrayList<String>();  
        for(int i=0, count=20; i<count; i++){  
            list.add("战士"+i+ "在线");  
        }  
          
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.detail_container);  
          
        ListView listView = (ListView) getActivity().findViewById(R.id.fragment_detail);  
        listView.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list));  
          
        Configuration configuration = getActivity().getResources().getConfiguration();  
        int ori = configuration.orientation;  
        mMapView = (MapView) getFragmentManager().findFragmentById(R.id.fragment_detail).getView().findViewById(R.id.bmapView); 
       // mMapView = (MapView) view.findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		mBaiduMap.setMapStatus(msu);
		initOverlay();
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				Button button = new Button(getActivity());
				button.setBackgroundResource(R.drawable.popup);
				OnInfoWindowClickListener listener = null;
				if (marker == mMarkerA || marker == mMarkerD) {
					button.setText("���λ��");
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
					button.setText("���ͼ��");
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
					button.setText("ɾ��");
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


    
    
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		mMapView.onResume();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mMapView.onDestroy();
		super.onDestroy();
		// ���� bitmap ��Դ
		bdA.recycle();
		bdB.recycle();
		bdC.recycle();
		bdD.recycle();
		bd.recycle();
		bdGround.recycle();
	}


	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}  
    
    
  
}  