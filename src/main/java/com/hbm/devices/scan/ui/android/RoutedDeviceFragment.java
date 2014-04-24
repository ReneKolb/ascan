package com.hbm.devices.scan.ui.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RoutedDeviceFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.routed_device_list, container, false);
		getActivity().getActionBar().setTitle("bla");
		return view;
	}
}

