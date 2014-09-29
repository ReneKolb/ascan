package com.hbm.devices.scan.ui.android;

import android.os.AsyncTask;
import android.util.Log;

import com.hbm.devices.configure.ConfigCallback;
import com.hbm.devices.configure.ConfigurationService;
import com.hbm.devices.scan.MissingDataException;
import com.hbm.devices.scan.messages.ConfigureParams;

public class ConfigServiceThread extends Thread {

	private ConfigurationService configService;

	@Override
	public void run() {
		this.configService = new ConfigurationService();
	}

	public void kill() {
		try {
			this.configService.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void sendConfiguration(ConfigureParams configParams,
			ConfigCallback callback, int timeout) throws NullPointerException,
			IllegalArgumentException, MissingDataException {
		if (this.configService != null) {
			new sendConfigTask().execute(new SendParams(configParams, callback,
					timeout));
		} else {
			Log.e("ConfigSender",
					"Cannot send the configuration. The service is null");
		}
	}

	private class SendParams {
		ConfigureParams params;
		ConfigCallback callback;
		int timeout;

		public SendParams(ConfigureParams params, ConfigCallback callback,
				int timeout) {
			this.params = params;
			this.callback = callback;
			this.timeout = timeout;
		}
	}

	private class sendConfigTask extends AsyncTask<SendParams, Integer, Void> {

		protected Void doInBackground(SendParams... params) {
			for (SendParams sendParam : params) {
				try {
					configService.sendConfiguration(sendParam.params,
							sendParam.callback, sendParam.timeout);
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (MissingDataException e) {
					e.printStackTrace();
				}
				// Escape early if cancel() is called
				if (isCancelled())
					break;
			}
			return null;
		}
	}
}
