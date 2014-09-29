package com.hbm.devices.scan.ui.android;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hbm.devices.scan.CommunicationPath;
import com.hbm.devices.scan.messages.Device;
import com.hbm.devices.scan.messages.IPv4Entry;
import com.hbm.devices.scan.messages.IPv6Entry;
import com.hbm.devices.scan.messages.Interface;
import com.hbm.devices.scan.messages.NetSettings;
import com.hbm.devices.scan.messages.ServiceEntry;

public class ShowDeviceSettingsFragment extends Fragment {

	private CommunicationPath communicationPath;

	public ShowDeviceSettingsFragment(CommunicationPath communicationPath) {
		ScanActivity.lastShownCommunicationPath = communicationPath;
		this.communicationPath = communicationPath;
	}
	

	public ShowDeviceSettingsFragment(){
		this.communicationPath = ScanActivity.lastShownCommunicationPath;
		if(this.communicationPath == null){
		}
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
				MeasureSpec.AT_MOST);
		int totalHeight = 0;
		View view = null;

		System.out.println("Adapter Type: " + listAdapter.getClass().getName());
		for (int i = 0; i < listAdapter.getCount(); i++) {
			view = listAdapter.getView(i, view, listView);
			if (i == 0) {
				view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
						LayoutParams.WRAP_CONTENT));
			}
			view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += view.getMeasuredHeight();
		}

		System.out.println("totHeight: " + totalHeight);

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings_display, container,
				false);

		// Update Device properties
		Device device = communicationPath.getAnnounce().getParams().getDevice();

		TextView nameView = (TextView) view.findViewById(R.id.DeviceName);
		nameView.setText(device.getName());

		TextView labelView = (TextView) view.findViewById(R.id.DeviceLabel);
		// labelView
		// .setText("Label... nicht in Device object, aber in spezifikation!");

		TextView typeView = (TextView) view.findViewById(R.id.DeviceType);
		typeView.setText("Type: " + device.getType());

		TextView familyTypeView = (TextView) view
				.findViewById(R.id.DeviceFamilyType);
		familyTypeView.setText("Family: " + device.getFamilyType());

		TextView firmwareView = (TextView) view
				.findViewById(R.id.DeviceFirmware);
		firmwareView.setText("FW: " + device.getFirmwareVersion());

		TextView uuidView = (TextView) view.findViewById(R.id.DeviceUUID);
		uuidView.setText("UUID: " + device.getUuid());

		TextView routerView = (TextView) view.findViewById(R.id.DeviceRouter);
		if (device.isRouter()) {
			routerView.setText("This device is a router");
		} else {
			routerView.setText("This device is not a router");
		}

		// Update NetSettings

		NetSettings netSettings = communicationPath.getAnnounce().getParams()
				.getNetSettings();

		ListView interfacesView = (ListView) view
				.findViewById(R.id.interfacesListView);
		InterfacesAdapter adapter = new InterfacesAdapter(this,
				netSettings.getInterface());

		interfacesView.setAdapter(adapter);

		if (netSettings.getDefaultGateway() != null) {
			TextView gatewayV4 = (TextView) view
					.findViewById(R.id.gateway_ipv4_view);
			gatewayV4.setText(netSettings.getDefaultGateway().getIpv4Address());
			TextView gatewayV6 = (TextView) view
					.findViewById(R.id.gateway_ipv6_view);
			gatewayV6.setText(netSettings.getDefaultGateway().getIpv6Address());
		}

		ListView servicesListView = (ListView) view
				.findViewById(R.id.services_list_view);

		ArrayList<ServiceEntry> servicesList = new ArrayList<ServiceEntry>();
		if (communicationPath.getAnnounce().getParams().getServices() != null) {
			for (ServiceEntry ser : communicationPath.getAnnounce().getParams()
					.getServices()) {
				servicesList.add(ser);
			}
		}
		ServicesAdapter servicesAdapter = new ServicesAdapter(
				this.getActivity(), servicesList);

		servicesListView.setAdapter(servicesAdapter);

		setListViewHeightBasedOnChildren(servicesListView);
		setListViewHeightBasedOnChildren(interfacesView);

		return view;
	}

	class InterfacesAdapter extends BaseAdapter {

		private Interface<LinkedList<IPv4Entry>, LinkedList<IPv6Entry>> interfaces;
		private LayoutInflater layoutInflater;
		private Activity activity;

		public InterfacesAdapter(
				Fragment fragment,
				Interface<LinkedList<IPv4Entry>, LinkedList<IPv6Entry>> interfaces) {
			activity = fragment.getActivity();
			layoutInflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// plural, but its actually only one.
			this.interfaces = interfaces;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Interface<LinkedList<IPv4Entry>, LinkedList<IPv6Entry>> getItem(
				int arg0) {
			return interfaces;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			InterfaceHolderItem viewHolder;

			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.interface_item,
						parent, false);
				viewHolder = new InterfaceHolderItem();
				viewHolder.interfaceName = (TextView) convertView
						.findViewById(R.id.interfaceNameView);
				viewHolder.methodName = (TextView) convertView
						.findViewById(R.id.interfaceConfigMethod);
				viewHolder.ipsItem = (ListView) convertView
						.findViewById(R.id.interfacesListView);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (InterfaceHolderItem) convertView.getTag();
			}

			viewHolder.interfaceName.setText(this.interfaces.getName() + ":");
			viewHolder.methodName.setText("Configuration method: "+this.interfaces
					.getConfigurationMethod());

			List<String> ips = new ArrayList<String>();

			for (IPv4Entry ip : this.interfaces.getIPv4()) {
				ips.add(ip.toString());
			}
			for (IPv6Entry ip : this.interfaces.getIPv6()) {
				ips.add(ip.toString());
			}

			viewHolder.ipsItem.setAdapter(new IPsAdapter(this.activity, ips));
			setListViewHeightBasedOnChildren(viewHolder.ipsItem);
			return convertView;
		}
	}

	static class InterfaceHolderItem {
		TextView interfaceName;
		TextView methodName;
		ListView ipsItem;
	}

	class IPsAdapter extends BaseAdapter {

		private List<String> items;
		private LayoutInflater inflater;

		public IPsAdapter(Activity activity, List<String> items) {
			this.items = items;
			this.inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return this.items.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public String getItem(int position) {
			return this.items.get(position);
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			IPHolderItem holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.ip_item, parent, false);

				holder = new IPHolderItem();
				holder.ipView = (TextView) convertView
						.findViewById(R.id.ipItemView);
				convertView.setTag(holder);
			} else {
				holder = (IPHolderItem) convertView.getTag();
			}

			String p = getItem(position);

			if (p != null) {
				if (holder.ipView != null) {
					holder.ipView.setText(p);
				}
			}

			return convertView;
		}

	}

	static class IPHolderItem {
		TextView ipView;
	}

	class ServicesAdapter extends BaseAdapter {

		private List<ServiceEntry> services;
		private LayoutInflater inflater;

		public ServicesAdapter(Activity activity, List<ServiceEntry> services) {
			this.services = services;
			this.inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return this.services.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public ServiceEntry getItem(int position) {
			return this.services.get(position);
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ServiceHolderItem holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.ip_item, parent, false);

				// Should do the trick
				holder = new ServiceHolderItem();
				holder.serviceView = (TextView) convertView
						.findViewById(R.id.ipItemView);
				convertView.setTag(holder);
			} else {
				holder = (ServiceHolderItem) convertView.getTag();
			}

			ServiceEntry p = getItem(position);

			if (p != null) {
				if (holder.serviceView != null) {
					holder.serviceView.setText(p.toString());
				}
			}

			return convertView;
		}

	}

	static class ServiceHolderItem {
		TextView serviceView;
	}
}
