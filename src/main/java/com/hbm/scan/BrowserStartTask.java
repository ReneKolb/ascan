package com.hbm.devices.scan.ui.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class BrowserStartTask extends AsyncTask<InetAddress, Void, Integer> {

	private Activity activity;

	public BrowserStartTask(Activity activity) {
		this.activity = activity;
	}

	protected Integer doInBackground(InetAddress... addresses) {
		for (InetAddress address : addresses) {
			String hostName = address.getCanonicalHostName();
			Uri.Builder b = new Uri.Builder();
			b.scheme("http");
			b.authority(hostName);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, b.build());
			activity.startActivity(browserIntent);
			return 0;
		}
		return 0;
	}
}

