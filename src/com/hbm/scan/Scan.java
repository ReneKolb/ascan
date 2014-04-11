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
	private ScanThread scanThread;
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		adapter = new ModuleListAdapter(this);
		setListAdapter(adapter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		scanThread = new ScanThread(adapter);
		scanThread.start();

	}

	@Override
	protected void onPause() {
		super.onPause();

		scanThread.kill();
		try {
			scanThread.join();
		} catch (InterruptedException e) {
		}

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
	AnnounceReceiver announceReceiver;

	public ScanThread(ModuleListAdapter adapter) {
		super("HBM scan thread");
		this.adapter = adapter;
		entries = new ArrayList<AnnouncePath>();
	}

	@Override
	public void run() {
		try {
			//FakeStringMessageMulticastReceiver announceReceiver = new FakeStringMessageMulticastReceiver();
	    	announceReceiver = new AnnounceReceiver();
	    	JsonFilter jf = new JsonFilter();
			announceReceiver.addObserver(jf);
			Filter ftFilter = new Filter(new FamilytypeMatch("QuantumX"));
			jf.addObserver(ftFilter);
			AnnounceFilter af = new AnnounceFilter();
			ftFilter.addObserver(af);
			af.addObserver(this);
	    	announceReceiver.start();
		}
		catch (Exception e) {
		}
	}

	public void kill() {
		announceReceiver.stop();
	}

	public void update(Observable o, Object arg) {
        AnnouncePath ap;
        if (arg instanceof RegisterDeviceEvent) {
            ap = ((RegisterDeviceEvent)arg).getAnnouncePath();
			ap.cookie = getDomainName(ap.getAnnounce());
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
	
	private String getDomainName(Announce announce) {
		Iterable<IPv4Entry> ips = announce.getParams().getNetSettings().getInterface().getIPv4();
		Iterator<IPv4Entry> iterator = ips.iterator();
		while (iterator.hasNext()) {
			IPv4Entry ipEntry = iterator.next();
			String ip = ipEntry.getAddress();
			try {
				InetAddress address = InetAddress.getByName(ip);
				String fqdn = address.getCanonicalHostName();
				return fqdn;
			} catch (UnknownHostException e) {
				return null;
			}
		}
		return null;
	}
}

