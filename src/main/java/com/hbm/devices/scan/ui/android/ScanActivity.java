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
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

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

	public DeviceFragment deviceFragment;

	public LinkedList<Filter> filterList;

	public static boolean enableFilterButton;

	public static ScanActivity activity;
	public Menu optionsMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = this;

		this.filterList = new LinkedList<Filter>();
		enableFilterButton = true;

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
			getFragmentManager().popBackStack();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}

	}

	private void shouldDisplayHomeUp() {
		boolean canback = getFragmentManager().getBackStackEntryCount() > 0;
		getActionBar().setDisplayHomeAsUpEnabled(canback);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		this.optionsMenu = menu;
		final SearchView searchView = (SearchView) menu.findItem(
				R.id.action_search).getActionView();

		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (deviceFragment != null) {
					deviceFragment.updateFilterString(query);
					searchView.clearFocus();
				}
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if (deviceFragment != null) {
					deviceFragment.updateFilterString(newText);
				}
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(getApplicationContext(),
					SettingsActivity.class));
			return true;
		case R.id.action_pause_control:
			if (this.deviceFragment.isPaused()) {
				item.setIcon(R.drawable.ic_action_pause);
				this.deviceFragment.resumeDeviceUpdates();
			} else {
				item.setIcon(R.drawable.ic_action_play);
				this.deviceFragment.pauseDeviceUpdates();
			}
			return true;
		case R.id.action_filters:
			if (enableFilterButton) {
				FilterFragment filterFragment = new FilterFragment();
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction().replace(R.id.fragment_container,
								filterFragment);
				transaction.addToBackStack(null);
				transaction.commit();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
