package com.hbm.scan;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class BrowserStartTask extends AsyncTask<String, Void, Integer> {

	private Activity activity;

	public BrowserStartTask(Activity activity) {
		this.activity = activity;
	}

	protected Integer doInBackground(String... ips) {
		try {
			for (String ip : ips) {
				InetAddress address = InetAddress.getByName(ip);
				String hostName = address.getCanonicalHostName();
				Uri.Builder b = new Uri.Builder();
				b.scheme("http");
				b.authority(hostName);
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, b.build());
				activity.startActivity(browserIntent);
				return 0;
			}
		} catch (UnknownHostException e) {
			return -1;
		}
		return 0;
	}
}

