package com.hbm.devices.scan.ui.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hbm.devices.scan.CommunicationPath;
import com.hbm.devices.scan.messages.Device;
import com.hbm.devices.scan.messages.IPv4Entry;
import com.hbm.devices.scan.messages.IPv6Entry;
import com.hbm.devices.scan.messages.NetSettings;
import com.hbm.devices.scan.messages.ServiceEntry;

public class ShowDeviceSettingsFragment extends Fragment {

	private CommunicationPath communicationPath;

	public ShowDeviceSettingsFragment() {
		this.communicationPath = ScanActivity.lastShownCommunicationPath;
		if (this.communicationPath == null) {
		}
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
		labelView.setText("");

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

		this.fillInterfacesList(view, netSettings);

		if (netSettings.getDefaultGateway() != null) {
			TextView gatewayV4 = (TextView) view
					.findViewById(R.id.gateway_ipv4_view);
			gatewayV4.setText(netSettings.getDefaultGateway().getIpv4Address());
			TextView gatewayV6 = (TextView) view
					.findViewById(R.id.gateway_ipv6_view);
			gatewayV6.setText(netSettings.getDefaultGateway().getIpv6Address());
		}

		ArrayList<ServiceEntry> servicesList = new ArrayList<ServiceEntry>();
		if (communicationPath.getAnnounce().getParams().getServices() != null) {
			for (ServiceEntry ser : communicationPath.getAnnounce().getParams()
					.getServices()) {
				servicesList.add(ser);
			}
		}

		this.fillServices(view, servicesList);

		return view;
	}

	private void fillInterfacesList(View parentView, NetSettings netSettings) {
		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout layout = (LinearLayout) parentView
				.findViewById(R.id.settings_display_interfaces_List);

		View interfacesView = layoutInflater.inflate(R.layout.interface_item,
				layout, false);
		InterfaceHolderItem viewHolder = new InterfaceHolderItem();
		viewHolder.interfaceName = (TextView) interfacesView
				.findViewById(R.id.interfaceNameView);
		viewHolder.methodName = (TextView) interfacesView
				.findViewById(R.id.interfaceConfigMethod);
		viewHolder.ipsItem = (LinearLayout) interfacesView
				.findViewById(R.id.interface_item_ip_list);

		viewHolder.interfaceName.setText(netSettings.getInterface().getName()
				+ ":");
		viewHolder.methodName.setText("Configuration method: "
				+ netSettings.getInterface().getConfigurationMethod());

		List<String> ips = new ArrayList<String>();

		for (IPv4Entry ip : netSettings.getInterface().getIPv4()) {
			ips.add(ip.toString());
		}
		for (IPv6Entry ip : netSettings.getInterface().getIPv6()) {
			ips.add(ip.toString());
		}

		for (String ipAddress : ips) {
			View ipsView = layoutInflater.inflate(R.layout.ip_item,
					viewHolder.ipsItem, false);
			IPHolderItem ipHolder = new IPHolderItem();
			ipHolder.ipView = (TextView) ipsView.findViewById(R.id.ipItemView);
			ipHolder.ipView.setText(ipAddress);
			viewHolder.ipsItem.addView(ipsView);
		}

		layout.addView(interfacesView);
	}

	private void fillServices(View parentView, ArrayList<ServiceEntry> services) {
		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout layout = (LinearLayout) parentView
				.findViewById(R.id.settings_display_services_list);
		for (ServiceEntry service : services) {
			View servicesView = layoutInflater.inflate(R.layout.ip_item,
					layout, false);
			ServiceHolderItem viewHolder = new ServiceHolderItem();
			viewHolder.serviceView = (TextView) servicesView
					.findViewById(R.id.ipItemView);

			viewHolder.serviceView.setText(service.toString());

			layout.addView(servicesView);
		}

	}

	static class InterfaceHolderItem {
		TextView interfaceName;
		TextView methodName;
		LinearLayout ipsItem;
	}

	static class IPHolderItem {
		TextView ipView;
	}

	static class ServiceHolderItem {
		TextView serviceView;
	}
}
