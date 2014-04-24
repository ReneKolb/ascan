package com.hbm.scan;

// TODO: rename Scan to ScanActivity

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Scan extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_scan);
		if (findViewById(R.id.fragment_container) != null) {
			if (savedInstanceState != null) {
				return;
			}
			DeviceFragment devices = new DeviceFragment();
			devices.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
				.add(R.id.fragment_container, devices).commit();

		}
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
				startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
		        return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
