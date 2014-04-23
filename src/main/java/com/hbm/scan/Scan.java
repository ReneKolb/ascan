package com.hbm.scan;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hbm.devices.scan.AnnouncePath;
import com.hbm.devices.scan.messages.Device;
import com.hbm.devices.scan.messages.IPv4Entry;
import com.hbm.devices.scan.RegisterDeviceEvent;
import com.hbm.devices.scan.UnregisterDeviceEvent;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Scan extends ListActivity implements AdapterView.OnItemLongClickListener {

	private ModuleListAdapter adapter;
	private ScanThread scanThread;
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bitmap routerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_router);

		setContentView(R.layout.list);

		adapter = new ModuleListAdapter(this, routerBitmap);
		setListAdapter(adapter);

		ListView list = getListView();
		list.setOnItemLongClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case R.id.action_settings:
				System.out.println("Settings");
				startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
		        return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) { 
		System.out.println("long click");
		AnnouncePath ap = (AnnouncePath)adapter.getItem(pos);
		InetAddress connectAddress = (InetAddress)ap.cookie;
		if (connectAddress != null) {
			BrowserStartTask browserTask = new BrowserStartTask(this);
			browserTask.execute(new InetAddress[] {connectAddress});
		}
		return true; 
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
		adapter.clearEntries();
	}

	@Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
	}
}

class ModuleListAdapter extends BaseAdapter {

	private Bitmap routerBitmap;
	private static final int DARK_OLIVE_GREEN = Color.rgb(85, 107, 47);
	private ListActivity activity;
	private LayoutInflater mLayoutInflater;
	private ArrayList<AnnouncePath> entries;
	private Comparator<AnnouncePath> listComparator;

	public ModuleListAdapter(Context context, Bitmap routerBitmap) {
		this.routerBitmap = routerBitmap;
		activity = (ListActivity)context;
		mLayoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		entries = new ArrayList<AnnouncePath>();
		listComparator = new UuidComparator();
	}

	public void clearEntries() {
		entries.clear();
		notifyDataSetChanged();
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
		AnnouncePath ap = (AnnouncePath)entries.get(position);
		return ap.cookie != null;
	}

	@Override
	public View getView(int position, View convertView,	ViewGroup parent) {
		ViewHolderItem viewHolder;

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.item, parent, false);
			viewHolder = new ViewHolderItem();
			viewHolder.moduleType = (TextView)convertView.findViewById(R.id.moduleType);
			viewHolder.moduleUUID = (TextView)convertView.findViewById(R.id.moduleUUID);
			viewHolder.moduleName = (TextView)convertView.findViewById(R.id.moduleName);
			viewHolder.router = (ImageView)convertView.findViewById(R.id.router);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderItem) convertView.getTag();
		}

		AnnouncePath ap = entries.get(position);
		if (ap != null) {
			InetAddress connectAddress = (InetAddress)ap.cookie;
			int color;
			if (connectAddress == null) {
				color = Color.RED;
			} else {
				color = DARK_OLIVE_GREEN;
			}

			Device device = ap.getAnnounce().getParams().getDevice();
			viewHolder.moduleType.setText(device.getType());
			viewHolder.moduleType.setTextColor(color);
			viewHolder.moduleUUID.setText(device.getUuid());
			viewHolder.moduleUUID.setTextColor(color);
			viewHolder.moduleName.setText(device.getName());
			viewHolder.moduleName.setTextColor(color);
			viewHolder.router.setImageBitmap(routerBitmap);
			if (device.isRouter()) {
				viewHolder.router.setVisibility(View.VISIBLE);
			} else {
				viewHolder.router.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

	public void updateEntries(final Object arg) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (arg instanceof RegisterDeviceEvent) {
					AnnouncePath ap = ((RegisterDeviceEvent)arg).getAnnouncePath();
					entries.add(ap);		
					Collections.sort(entries, listComparator);
				} else if (arg instanceof UnregisterDeviceEvent) {
					AnnouncePath ap = ((UnregisterDeviceEvent)arg).getAnnouncePath();
					entries.remove(ap);		
					Collections.sort(entries, listComparator);
				}
				notifyDataSetChanged();
			}
		});
	}

	static class ViewHolderItem {
    	TextView moduleType;
    	TextView moduleUUID;
    	TextView moduleName;
		ImageView router;
	}
}

class UuidComparator implements Comparator<AnnouncePath> {
	public int compare(AnnouncePath a1, AnnouncePath a2) {
		String uuid1 = a1.getAnnounce().getParams().getDevice().getUuid();
		String uuid2 = a2.getAnnounce().getParams().getDevice().getUuid();
		return uuid1.compareToIgnoreCase(uuid2);
	}
}
