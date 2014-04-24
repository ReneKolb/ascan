package com.hbm.devices.scan.ui.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.app.FragmentManager;

public class ScanActivity extends Activity implements FragmentManager.OnBackStackChangedListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_scan);
		if (findViewById(R.id.fragment_container) != null) {
			if (savedInstanceState != null) {
				return;
			}
			getFragmentManager().addOnBackStackChangedListener(this);
			shouldDisplayHomeUp();

			DeviceFragment devices = new DeviceFragment();
			devices.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
				.add(R.id.fragment_container, devices).commit();

		}
	}

	@Override
	public void onBackStackChanged() {
	    shouldDisplayHomeUp();
	}

	private void shouldDisplayHomeUp(){
	   //Enable Up button only  if there are entries in the back stack
	   boolean canback = getFragmentManager().getBackStackEntryCount() > 0;
	   getActionBar().setDisplayHomeAsUpEnabled(canback);
	}
	
	@Override
	public boolean onNavigateUp() {
    	//This method is called when the up button is pressed. Just the pop back stack.
		getFragmentManager().popBackStack();
		return true;
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
