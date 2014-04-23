package com.hbm.scan;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hbm.devices.scan.AnnouncePath;
import com.hbm.devices.scan.messages.Device;
import com.hbm.devices.scan.messages.IPv4Entry;

import java.net.InetAddress;
import java.util.ArrayList;

public class Scan extends ListActivity {

	private ModuleListAdapter adapter;
	private ScanThread scanThread;
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		ListView list = getListView();
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){ 
			@Override 
			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) { 
				return true; 
        	} 
		}); 
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		adapter = new ModuleListAdapter(this);
		setListAdapter(adapter);

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
		InetAddress connectAddress = (InetAddress)ap.cookie;
		if (connectAddress != null) {
			BrowserStartTask browserTask = new BrowserStartTask(this);
			browserTask.execute(new InetAddress[] {connectAddress});
			return;
		}
	}
}

class ModuleListAdapter extends BaseAdapter {

	private static final int DARK_OLIVE_GREEN = Color.rgb(85, 107, 47);
	private ListActivity activity;
	private LayoutInflater mLayoutInflater;
	private ArrayList<AnnouncePath> entries = new ArrayList<AnnouncePath>();

	public ModuleListAdapter(Context context) {
		activity = (ListActivity)context;
		mLayoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return entries.size();
	}

	@Override
	public Object getItem(int position) {
		return entries.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		System.out.println("isEnabled");
		AnnouncePath ap = (AnnouncePath)entries.get(position);
		return ap.cookie != null;
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
		ImageView router = (ImageView)itemView.findViewById(R.id.router);

		AnnouncePath ap;
		ap = entries.get(position);
		InetAddress connectAddress = (InetAddress)ap.cookie;
		int color;
		if (connectAddress == null) {
			color = Color.RED;
		} else {
			color = DARK_OLIVE_GREEN;
		}

		Device device = ap.getAnnounce().getParams().getDevice();
		moduleType.setText(device.getType());
		moduleType.setTextColor(color);
		moduleUUID.setText(device.getUuid());
		moduleUUID.setTextColor(color);
		moduleName.setText(device.getName());
		moduleName.setTextColor(color);
		if (device.isRouter()) {
			router.setImageResource(R.drawable.ic_router);
		} else {
			router.setVisibility(View.INVISIBLE);
		}

		return itemView;
	}

	public void updateEntries(final ArrayList<AnnouncePath> newEntries) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				entries = newEntries;
				notifyDataSetChanged();
			}
		});
	}
}

