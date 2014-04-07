package com.hbm.scan;

import android.app.ListActivity;
import android.os.Bundle;


import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import java.util.ArrayList;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.ListView;
import android.util.Log;

import com.hbm.devices.scan.AnnouncePath;
import com.hbm.devices.scan.AnnounceReceiver;
import com.hbm.devices.scan.FakeStringMessageMulticastReceiver;
import com.hbm.devices.scan.filter.AnnounceFilter;
import com.hbm.devices.scan.filter.FamilytypeMatch;
import com.hbm.devices.scan.filter.Filter;
import com.hbm.devices.scan.filter.JsonFilter;
import com.hbm.devices.scan.IPv4ScanInterfaces;
import com.hbm.devices.scan.messages.*;
import com.hbm.devices.scan.RegisterDeviceEvent;
import com.hbm.devices.scan.ScanConstants;
import com.hbm.devices.scan.UnregisterDeviceEvent;


import java.net.SocketException;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Observer;
import java.util.Observable;

import android.content.Intent;
import android.net.Uri;
import android.view.WindowManager;
import android.graphics.Color;

public class Scan extends ListActivity {

	private static final String TAG = "Scan";
	private ModuleListAdapter adapter;
 
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		adapter = new ModuleListAdapter(this);
		setListAdapter(adapter);

		ScanThread st = new ScanThread(adapter);
		st.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.d(TAG, "onResume");
	}

	@Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
		AnnouncePath ap = (AnnouncePath)adapter.getItem(position);
		Iterable<IPv4Entry> ips = ap.getAnnounce().getParams().getNetSettings().getInterface().getIPv4();
		Iterator<IPv4Entry> iterator = ips.iterator();
		if (iterator.hasNext()) {
			IPv4Entry entry = iterator.next();
			String ip = entry.getAddress();
			Uri.Builder b = new Uri.Builder();
			b.scheme("http");
			b.authority(ip);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, b.build());
			startActivity(browserIntent);
		}
	}
}

class ModuleListAdapter extends BaseAdapter {

	private ListActivity activity;
	private LayoutInflater mLayoutInflater;
	private ArrayList<AnnouncePath> entries = new ArrayList<AnnouncePath>();

	public ModuleListAdapter(Context context) {
		activity = (ListActivity)context;
		mLayoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		synchronized(entries) {
			return entries.size();
		}
	}

	@Override
	public Object getItem(int position) {
		synchronized(entries) {
			return entries.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView,	ViewGroup parent) {
		RelativeLayout itemView;
		if (convertView == null) {
			itemView = (RelativeLayout) mLayoutInflater.inflate(R.layout.item, parent, false);
		} else {
			itemView = (RelativeLayout) convertView;
		}

		TextView moduleType = (TextView)itemView.findViewById(R.id.moduleType);
		TextView moduleUUID = (TextView)itemView.findViewById(R.id.moduleUUID);
		TextView moduleName = (TextView)itemView.findViewById(R.id.moduleName);

		AnnouncePath ap;
		synchronized(entries) {
			ap = entries.get(position);
		}
		Device device = ap.getAnnounce().getParams().getDevice();
		moduleType.setText(device.getType());
		moduleType.setTextColor(Color.YELLOW);
		moduleUUID.setText(device.getUuid());
		moduleUUID.setBackgroundColor(Color.RED);
		moduleName.setText(device.getName());

		return itemView;
	}

	public void updateEntries(ArrayList<AnnouncePath> entries) {
		synchronized(this.entries) {
			this.entries = entries;
		}
		activity.runOnUiThread(new Runnable() {
			public void run() {
				notifyDataSetChanged();
			}
		});
	}
}

class ScanThread extends Thread implements Observer {
	private ModuleListAdapter adapter;
	ArrayList<AnnouncePath> entries;

	public ScanThread(ModuleListAdapter adapter) {
		super("HBM scan thread");
		this.adapter = adapter;
		entries = new ArrayList<AnnouncePath>();
	}

	@Override
	public void run() {
		FakeStringMessageMulticastReceiver ar = new FakeStringMessageMulticastReceiver();
	    //AnnounceReceiver ar = new AnnounceReceiver();
	    JsonFilter jf = new JsonFilter();
		ar.addObserver(jf);
		Filter ftFilter = new Filter(new FamilytypeMatch("QuantumX"));
		jf.addObserver(ftFilter);
		AnnounceFilter af = new AnnounceFilter();
		ftFilter.addObserver(af);
		af.addObserver(this);

	    ar.start();
	}

	public void update(Observable o, Object arg) {
        AnnouncePath ap;
        if (arg instanceof RegisterDeviceEvent) {
            ap = ((RegisterDeviceEvent)arg).getAnnouncePath();
			synchronized(entries) {
				entries.add(ap);
			}
        } else if (arg instanceof UnregisterDeviceEvent) {
            ap = ((UnregisterDeviceEvent)arg).getAnnouncePath();
			synchronized(entries) {
				entries.remove(ap);
			}
        }
		adapter.updateEntries(entries);
	}
}

