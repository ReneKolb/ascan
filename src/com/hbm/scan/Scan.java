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


import com.hbm.devices.scan.IPv4ScanInterfaces;
import com.hbm.devices.scan.ScanConstants;
import com.hbm.devices.scan.AnnounceReceiver;
import com.hbm.devices.scan.filter.JsonFilter;

import java.net.SocketException;
import java.net.NetworkInterface;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import android.content.Intent;
import android.net.Uri;
import android.view.WindowManager;

public class Scan extends ListActivity {

	private static final String TAG = "Scan";
 
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		try {
			IPv4ScanInterfaces ifs = new IPv4ScanInterfaces();
			Collection<NetworkInterface> interfaces = ifs.getInterfaces();
        	Iterator<NetworkInterface> niIterator = interfaces.iterator();
        	while (niIterator.hasNext()) {
            	NetworkInterface ni = niIterator.next();
				Log.d(TAG, ni.toString());
        	}
		} catch (SocketException e) {
			Log.d(TAG, "SocketException");
			Log.d(TAG, e.toString());
			return;
		}

		ModuleListAdapter adapter = new ModuleListAdapter(this);
		setListAdapter(adapter);

		LoadFeedData loadFeedData = new LoadFeedData(adapter);
		loadFeedData.execute();

		ScanThread st = new ScanThread();
		st.start();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.d(TAG, "onResume");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

@Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.heise.de"));
		startActivity(browserIntent);
	}
}

class ModuleListAdapter extends BaseAdapter {

	private Context mContext;

	private LayoutInflater mLayoutInflater;

		private ArrayList<Entry> mEntries = new ArrayList<Entry>();

		public ModuleListAdapter(Context context) {
			mContext = context;
			mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

	@Override
		public int getCount() {
			return mEntries.size();
		}

	@Override
		public Object getItem(int position) {
			return mEntries.get(position);
		}

	@Override
		public long getItemId(int position) {
			return position;
		}

	@Override
		public View getView(int position, View convertView,
				ViewGroup parent) {
			RelativeLayout itemView;
			if (convertView == null) {
				itemView = (RelativeLayout) mLayoutInflater.inflate(
						R.layout.item, parent, false);

			} else {
				itemView = (RelativeLayout) convertView;
			}

			TextView moduleType = (TextView)
				itemView.findViewById(R.id.moduleType);
			TextView moduleUUID = (TextView)
				itemView.findViewById(R.id.moduleUUID);
			TextView moduleName = (TextView)
				itemView.findViewById(R.id.moduleName);

			moduleType.setText(mEntries.get(position).getModuleType());
			moduleUUID.setText(mEntries.get(position).getModuleUUID());
			moduleName.setText(mEntries.get(position).getModuleName());

			return itemView;
		}

	public void upDateEntries(ArrayList<Entry> entries) {
		mEntries = entries;
		notifyDataSetChanged();
	}
}

class Entry {

	String type;
	String uuid;
	String name;
	Entry(String t, String u, String n) {
		type = t;
		uuid = u;
		name = n;
	}

	String getModuleType() {
		return type;
	}

	String getModuleUUID() {
		return uuid;
	}

	String getModuleName() {
		return name;
	}
}

class ScanThread extends Thread {

	@Override
	public void run() {
		try {
		    AnnounceReceiver ar = new AnnounceReceiver();
		    JsonFilter jf = new JsonFilter();
		    ar.addObserver(jf);
		    ar.start();
		} catch (IOException e) {
		}
	}
}

class LoadFeedData extends
	AsyncTask<Void, Void, ArrayList<Entry>> {

	private final ModuleListAdapter mAdapter;

	public LoadFeedData(ModuleListAdapter adapter) {
		mAdapter = adapter;
	}

	@Override
	protected ArrayList<Entry> doInBackground(Void... params) {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX1609", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840A", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX1609", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840A", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX1609", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840A", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX1609", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840A", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX1609", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840A", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX1609", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840A", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		entries.add(new Entry("MX840", "0009e5123456", "Horst"));
		return entries;
	}

	protected void onPostExecute(ArrayList<Entry> entries) {
		mAdapter.upDateEntries(entries);
	}
}
