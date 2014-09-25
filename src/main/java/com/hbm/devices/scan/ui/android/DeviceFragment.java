package com.hbm.devices.scan.ui.android;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.hbm.devices.scan.CommunicationPath;
import com.hbm.devices.scan.events.LostDeviceEvent;
import com.hbm.devices.scan.events.NewDeviceEvent;
import com.hbm.devices.scan.events.UpdateDeviceEvent;
import com.hbm.devices.scan.messages.Device;

public class DeviceFragment extends ListFragment implements
		AdapterView.OnItemLongClickListener {
	private boolean useFakeMessages = false;
	private ModuleListAdapter adapter;
	private ScanThread scanThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bitmap routerBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_router);

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		useFakeMessages = sharedPref.getBoolean("fake_messages", false);

		adapter = new ModuleListAdapter(this, routerBitmap);
		setListAdapter(adapter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		scanThread = new ScanThread(adapter, useFakeMessages);
		scanThread.start();
	}

	@Override
	public void onPause() {
		super.onPause();

		scanThread.kill();
		try {
			scanThread.join();
		} catch (InterruptedException e) {
		}
		adapter.clearEntries();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.device_list, container, false);
		getActivity().getActionBar().setTitle(R.string.app_name);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ListView list = getListView();
		list.setOnItemLongClickListener(this);
	}

	private void showDeviceSettings(CommunicationPath communicationPath) {
		ShowDeviceSettingsFragment settingsFrag = new ShowDeviceSettingsFragment(
				communicationPath);
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.fragment_container, settingsFrag);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	private void showConfigure(CommunicationPath comPath) {
		ConfigureFragment configFragment = new ConfigureFragment(comPath
				.getAnnounce().getParams().getNetSettings());

		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.fragment_container, configFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		CommunicationPath cp = adapter.getItem(pos);
		Device device = cp.getAnnounce().getParams().getDevice();
		if (device.isRouter()) {
			RoutedDeviceFragment newFragment = new RoutedDeviceFragment();
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.fragment_container, newFragment);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			showDeviceSettings(cp);
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> av, View v, int pos,
			long id) {
		final CommunicationPath cp = adapter.getItem(pos);
		InetAddress connectAddress = (InetAddress) cp.cookie;

		PopupMenu popupMenu = new PopupMenu(av.getContext(), v);
		popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());

		popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (item.getTitle().equals(
						getString(R.string.popup_show_settings))) {
					showDeviceSettings(cp);
				} else if (item.getTitle().equals(
						getString(R.string.popup_configure))) {
					showConfigure(cp);
				}
				return true;
			}
		});

		popupMenu.show();

		// if (connectAddress != null) {
		// BrowserStartTask browserTask = new BrowserStartTask(getActivity());
		// browserTask.execute(new InetAddress[] { connectAddress });
		// }
		return true;
	}
}

class ModuleListAdapter extends BaseAdapter {

	private Bitmap routerBitmap;
	private static final int DARK_OLIVE_GREEN = Color.rgb(85, 107, 47);
	private Activity activity;
	private LayoutInflater layoutInflater;
	private ArrayList<CommunicationPath> entries;
	private Comparator<CommunicationPath> listComparator;

	public ModuleListAdapter(Fragment fragment, Bitmap routerBitmap) {
		this.routerBitmap = routerBitmap;
		activity = fragment.getActivity();
		layoutInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		entries = new ArrayList<CommunicationPath>();
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
	public CommunicationPath getItem(int position) {
		return entries.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		// is the item clickable?
		CommunicationPath cp = (CommunicationPath) entries.get(position);
		Device device = cp.getAnnounce().getParams().getDevice();
		// return (cp.cookie != null) || (device.isRouter());
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderItem viewHolder;

		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.device_item, parent,
					false);
			viewHolder = new ViewHolderItem();
			viewHolder.moduleType = (TextView) convertView
					.findViewById(R.id.moduleType);
			viewHolder.moduleUUID = (TextView) convertView
					.findViewById(R.id.moduleUUID);
			viewHolder.moduleName = (TextView) convertView
					.findViewById(R.id.moduleName);
			viewHolder.router = (ImageView) convertView
					.findViewById(R.id.router);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderItem) convertView.getTag();
		}

		CommunicationPath cp = entries.get(position);
		if (cp != null) {
			InetAddress connectAddress = (InetAddress) cp.cookie;
			int color;
			if (connectAddress == null) {
				color = Color.RED;
			} else {
				color = DARK_OLIVE_GREEN;
			}

			Device device = cp.getAnnounce().getParams().getDevice();
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
				if (arg instanceof NewDeviceEvent) {
					NewDeviceEvent event = (NewDeviceEvent) arg;
					CommunicationPath cp = event.getAnnouncePath();
					entries.add(cp);
					Collections.sort(entries, listComparator);
				} else if (arg instanceof LostDeviceEvent) {
					LostDeviceEvent event = (LostDeviceEvent) arg;
					CommunicationPath cp = event.getAnnouncePath();
					entries.remove(cp);
					Collections.sort(entries, listComparator);
				} else if (arg instanceof UpdateDeviceEvent) {
					UpdateDeviceEvent event = (UpdateDeviceEvent) arg;
					entries.remove(event.getOldCommunicationPath());
					entries.add(event.getNewCommunicationPath());
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

class UuidComparator implements Comparator<CommunicationPath> {
	public int compare(CommunicationPath a1, CommunicationPath a2) {
		String uuid1 = a1.getAnnounce().getParams().getDevice().getUuid();
		String uuid2 = a2.getAnnounce().getParams().getDevice().getUuid();
		return uuid1.compareToIgnoreCase(uuid2);
	}
}
