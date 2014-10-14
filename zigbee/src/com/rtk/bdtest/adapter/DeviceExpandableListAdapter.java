package com.rtk.bdtest.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtk.bdtest.R;
import com.rtk.bdtest.util.Device;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

public class DeviceExpandableListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<Device> listB;
	private ArrayList<List<Device>> listA;
	private LayoutInflater inflater;

	public DeviceExpandableListAdapter(Context context) {
		this.context = context;
	}

	public DeviceExpandableListAdapter(Context context,
			ArrayList<Device> listB, ArrayList<List<Device>> listA) {
		this.context = context;
		this.listB = listB;
		this.listA = listA;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getGroupCount() {
		return listB.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return listA.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return listB.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return listA.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupHolder holder = new GroupHolder();
		convertView = inflater.inflate(R.layout.device_list_item_layout, null);
		holder.name = (TextView) convertView.findViewById(R.id.device_name);
		holder.online = (TextView) convertView.findViewById(R.id.online);
		holder.address = (TextView) convertView
				.findViewById(R.id.device_address);
		holder.type = (TextView) convertView.findViewById(R.id.device_type);
		holder.id = (TextView) convertView.findViewById(R.id.device_id);
		holder.parentAddress = (TextView) convertView
				.findViewById(R.id.parent_address);
		holder.name.setText("姓名:" + listB.get(groupPosition).deviceName);
		if ((listB.get(groupPosition) != null)
				&& (listB.get(groupPosition).online)) {
			holder.online.setText("在线");
		} else {
			holder.online.setText("离线");
		}
		holder.address.setText(context.getString(R.string.device_address)
				+ listB.get(groupPosition).deviceAddress);
		holder.type.setText(context.getString(R.string.device_type)
				+ listB.get(groupPosition).deviceType);
		holder.id.setText("ID:" + listB.get(groupPosition).deviceID);
		holder.parentAddress.setText(context
				.getString(R.string.device_parent_address)
				+ listB.get(groupPosition).parentAddress);
		// 在线和不在线颜色不同
		if (!(listB.get(groupPosition).online)) {
			convertView.setBackgroundColor(this.context.getResources()
					.getColor(R.color.ivory));
		} else {
			convertView.setBackgroundColor(this.context.getResources()
					.getColor(R.color.green));
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
		convertView = (View) inflater.inflate(R.layout.device_list_item_layout,
				null);
		holder.name = (TextView) convertView.findViewById(R.id.device_name);
		holder.online = (TextView) convertView.findViewById(R.id.online);
		holder.address = (TextView) convertView
				.findViewById(R.id.device_address);
		holder.type = (TextView) convertView.findViewById(R.id.device_type);
		holder.id = (TextView) convertView.findViewById(R.id.device_id);
		holder.parentAddress = (TextView) convertView
				.findViewById(R.id.parent_address);
		holder.name.setText("姓名:"
				+ listA.get(groupPosition).get(childPosition).deviceName);
		if ((listA.get(groupPosition).get(childPosition) != null)
				&& (listA.get(groupPosition).get(childPosition).online)) {
			holder.online.setText("在线");
		} else {
			holder.online.setText("离线");
		}
		holder.address.setText(context.getString(R.string.device_address)
				+ listA.get(groupPosition).get(childPosition).deviceAddress);
		holder.type.setText(context.getString(R.string.device_type)
				+ listA.get(groupPosition).get(childPosition).deviceType);
		holder.id.setText("ID:"
				+ listA.get(groupPosition).get(childPosition).deviceID);
		holder.parentAddress.setText(context
				.getString(R.string.device_parent_address)
				+ listA.get(groupPosition).get(childPosition).parentAddress);
		// 在线和不在线颜色不同
		if (!(listA.get(groupPosition).get(childPosition).online)) {
			convertView.setBackgroundColor(this.context.getResources()
					.getColor(R.color.ivory));
		} else {
			convertView.setBackgroundColor(this.context.getResources()
					.getColor(R.color.green));
		}
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	class ViewHolder {
		TextView name;
		TextView address;
		TextView type;
		TextView id;
		TextView parentAddress;
		TextView online;
	}

	class GroupHolder {
		TextView name;
		TextView address;
		TextView type;
		TextView id;
		TextView parentAddress;
		TextView online;
	}
}
