package com.rtk.bdtest.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtk.bdtest.util.Device;
import com.rtk.bdtest.R;

public class DeviceListAdapter extends BaseAdapter {
	private ArrayList<Device> list;
	private LayoutInflater inflater;
	private Context context;

	public DeviceListAdapter(Context context, ArrayList<Device> list) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		holder = new ViewHolder();
		convertView = inflater.inflate(R.layout.device_list_item_layout, null);
		holder.name = (TextView)convertView.findViewById(R.id.device_name);
		holder.online = (TextView)convertView.findViewById(R.id.online);
		holder.address = (TextView) convertView
				.findViewById(R.id.device_address);
		holder.type = (TextView) convertView.findViewById(R.id.device_type);
		holder.id = (TextView) convertView.findViewById(R.id.device_id);
		holder.parentAddress = (TextView) convertView
				.findViewById(R.id.parent_address);
        holder.name.setText("设备名:"+list.get(position).deviceName);

        holder.online.setText("离线");
		holder.address.setText(context.getString(R.string.device_address)
				+ list.get(position).deviceAddress);
		holder.type.setText(context.getString(R.string.device_type)
				+ list.get(position).deviceType);
		holder.id.setText(context.getString(R.string.device_id)
				+ list.get(position).deviceID);
		holder.parentAddress.setText(context
				.getString(R.string.device_parent_address)
				+ list.get(position).parentAddress);
		return convertView;
	}

	class ViewHolder {
		TextView name;
		TextView address;
		TextView type;
		TextView id;
		TextView parentAddress;
		TextView online;
	}

}
