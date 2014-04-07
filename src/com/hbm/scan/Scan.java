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
import com.hbm.devices.scan.FakeStringMessageMulticastReceiver;
import com.hbm.devices.scan.filter.JsonFilter;
import com.hbm.devices.scan.filter.Filter;
import com.hbm.devices.scan.filter.FamilytypeMatch;
import com.hbm.devices.scan.filter.AnnounceFilter;


import java.net.SocketException;
import java.net.NetworkInterface;
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
 
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		ModuleListAdapter adapter = new ModuleListAdapter(this);
		setListAdapter(adapter);

		LoadFeedData loadFeedData = new LoadFeedData(adapter);
		loadFeedData.execute();

		ScanThread st = new ScanThread();
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
			moduleType.setTextColor(Color.YELLOW);
			moduleUUID.setText(mEntries.get(position).getModuleUUID());
			moduleUUID.setBackgroundColor(Color.RED);
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

class ScanThread extends Thread implements Observer {

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
