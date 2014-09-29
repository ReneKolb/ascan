package com.hbm.devices.scan.ui.android;

import java.util.LinkedList;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hbm.devices.scan.CommunicationPath;
import com.hbm.devices.scan.filter.Filter;
import com.hbm.devices.scan.messages.AnnounceParams;

public class ScanActivity extends Activity implements
		FragmentManager.OnBackStackChangedListener {

	// This is only needed when rotating the phone, so the
	// ShowDeviceSettingsFragment can be instantiated with the empty arguments
	// constructor.
	public static CommunicationPath lastShownCommunicationPath = null;
	public static AnnounceParams lastConfiguredParams = null;

	private DeviceFragment deviceFragment;

	public LinkedList<Filter> filterList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.filterList = new LinkedList<Filter>();

		setContentView(R.layout.device_scan);
		if (findViewById(R.id.fragment_container) != null) {
			if (savedInstanceState != null) {
				return;
			}
			getFragmentManager().addOnBackStackChangedListener(this);
			shouldDisplayHomeUp();

			deviceFragment = new DeviceFragment();
			deviceFragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, deviceFragment).commit();
		}
	}

	public void updateFilterSettings(LinkedList<Filter> filterList) {
		this.filterList = filterList;
	}

	@Override
	public void onBackStackChanged() {
		shouldDisplayHomeUp();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}

	}

	private void shouldDisplayHomeUp() {
		boolean canback = getFragmentManager().getBackStackEntryCount() > 0;
		getActionBar().setDisplayHomeAsUpEnabled(canback);
	}

	// @Override
	// public boolean onNavigateUp() {
	// // This method is called when the up button is pressed. Just the pop
	// // backstack.
	// getFragmentManager().popBackStack();
	// return true;
	// }

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
			startActivity(new Intent(getApplicationContext(),
					SettingsActivity.class));
			return true;
		case R.id.action_filters:
			FilterFragment filterFragment = new FilterFragment(this);
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction().replace(R.id.fragment_container,
							filterFragment);
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
