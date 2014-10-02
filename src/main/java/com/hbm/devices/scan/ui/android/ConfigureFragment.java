package com.hbm.devices.scan.ui.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.hbm.devices.configure.ConfigCallback;
import com.hbm.devices.configure.ConfigQuery;
import com.hbm.devices.configure.Device;
import com.hbm.devices.configure.Interface;
import com.hbm.devices.configure.NetSettings;
import com.hbm.devices.scan.MissingDataException;
import com.hbm.devices.scan.messages.AnnounceParams;
import com.hbm.devices.scan.messages.ConfigureParams;
import com.hbm.devices.scan.messages.DefaultGateway;
import com.hbm.devices.scan.messages.IPv4EntryManual;
import com.hbm.devices.scan.messages.Interface.Method;
import com.hbm.devices.scan.messages.Response;

public class ConfigureFragment extends Fragment {

	private AnnounceParams oldParams;

	private ConfigServiceThread configThread;

	public ConfigureFragment() {
		this.oldParams = ScanActivity.lastConfiguredParams;
	}
	

	@Override
	public void onResume() {
		super.onResume();
		this.configThread = new ConfigServiceThread();
		configThread.start();
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.configThread.kill();
		try {
			configThread.join();
		} catch (InterruptedException e) {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		View view = inflater.inflate(R.layout.configure_layout, container,
				false);

		final EditText interfaceNameEdit = (EditText) view
				.findViewById(R.id.configure_interface_name_edit);

		RadioGroup methodGroup = (RadioGroup) view
				.findViewById(R.id.configure_method_radiogroup);

		final RadioButton dhcpRadio = (RadioButton) view
				.findViewById(R.id.configure_method_dhcp_radio);

		final RadioButton manualRadio = (RadioButton) view
				.findViewById(R.id.configure_method_manual_radio);

		final EditText ipEdit = (EditText) view
				.findViewById(R.id.configure_ip_address_edit);

		final EditText subnetEdit = (EditText) view
				.findViewById(R.id.configure_subnet_edit);

		final EditText gatewayIpEdit = (EditText) view
				.findViewById(R.id.configure_gateway_ip_edit);

		Button readyButton = (Button) view
				.findViewById(R.id.configure_ready_button);

		interfaceNameEdit.setText(oldParams.getNetSettings().getInterface()
				.getName());

		if (oldParams.getNetSettings().getInterface().getConfigurationMethod() != null
				&& oldParams.getNetSettings().getInterface()
						.getConfigurationMethod()
						.equals(Method.dhcp.toString())) {
			manualRadio.setChecked(false);
			dhcpRadio.setChecked(true);

			ipEdit.setEnabled(false);
			subnetEdit.setEnabled(false);
		} else {
			dhcpRadio.setChecked(false);
			manualRadio.setChecked(true);

			ipEdit.setHint(oldParams.getNetSettings().getInterface().getIPv4()
					.get(0).getAddress());
			subnetEdit.setHint(oldParams.getNetSettings().getInterface()
					.getIPv4().get(0).getNetmask());
		}

		methodGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (((RadioButton) group.findViewById(checkedId))
						.getText()
						.equals(getString(R.string.configure_method_dhcp_label))) {
					// new is dhcp
					ipEdit.setEnabled(false);
					subnetEdit.setEnabled(false);
				} else {
					// new is manual
					ipEdit.setEnabled(true);
					subnetEdit.setEnabled(true);
				}
			}
		});

		if (oldParams.getNetSettings().getDefaultGateway() != null) {
			gatewayIpEdit.setHint(oldParams.getNetSettings()
					.getDefaultGateway().getIpv4Address());
		}

		readyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// getFragmentManager().popBackStack();
				String interfaceName = interfaceNameEdit.getText().toString();
				Method method;

				if (dhcpRadio.isChecked()) {
					method = Method.dhcp;
				} else if (manualRadio.isChecked()) {
					method = Method.manual;
				} else {
					method = Method.RouterSolicitation;
				}

				String ip;
				if (ipEdit.getText() == null || ipEdit.getText().length() == 0) {
					if (ipEdit.getHint() != null) {
						ip = ipEdit.getHint().toString();
					} else {
						ip = "";
					}
				} else {
					ip = ipEdit.getText().toString();
				}
				String netMask;
				if (subnetEdit.getText() == null
						|| subnetEdit.getText().length() == 0) {
					if (subnetEdit.getHint() != null) {
						netMask = subnetEdit.getHint().toString();
					} else {
						netMask = "";
					}
				} else {
					netMask = subnetEdit.getText().toString();
				}

				String gatewayIp = null;
				if (gatewayIpEdit.getText() != null
						&& gatewayIpEdit.getText().length() > 0) {
					gatewayIp = gatewayIpEdit.getText().toString();
				}

				Device device = new Device(oldParams.getDevice().getUuid());
				Interface interfaceSettings;
				if (method != Method.dhcp) {
					interfaceSettings = new Interface(interfaceName, method,
							new IPv4EntryManual(ip, netMask));
				} else {
					interfaceSettings = new Interface(interfaceName, method,
							null);
				}

				NetSettings settings;
				if (gatewayIp != null) {
					settings = new NetSettings(interfaceSettings,
							new DefaultGateway(gatewayIp, null));
				} else {
					settings = new NetSettings(interfaceSettings);
				}

				ConfigureParams params = new ConfigureParams(device, settings);

				final Toast sendToast = Toast.makeText(
						ConfigureFragment.this.getActivity(),
						"Send configuration", Toast.LENGTH_SHORT);
				sendToast.show();

				try {
					configThread.sendConfiguration(params,
							new ConfigCallback() {

								@Override
								public void onSuccess(ConfigQuery arg0,
										Response arg1) {
									sendToast.cancel(); // Hide the toast and
														// show a new one
									ConfigureFragment.this.getActivity()
											.runOnUiThread(new Runnable() {
												public void run() {
													Toast.makeText(
															ConfigureFragment.this
																	.getActivity(),
															"Configuration successful",
															Toast.LENGTH_LONG)
															.show();
												}
											});

									getFragmentManager().popBackStack();
								}

								@Override
								public void onTimeout(ConfigQuery arg0) {
									sendToast.cancel();
									ConfigureFragment.this.getActivity()
											.runOnUiThread(new Runnable() {
												@Override
												public void run() {
													Toast.makeText(
															ConfigureFragment.this
																	.getActivity(),
															"Configuration Timeout...",
															Toast.LENGTH_LONG)
															.show();
												}
											});

								}

								@Override
								public void onError(ConfigQuery arg0,
										final Response arg1) {
									sendToast.cancel();
									ConfigureFragment.this.getActivity()
											.runOnUiThread(new Runnable() {
												@Override
												public void run() {
													Toast.makeText(
															ConfigureFragment.this
																	.getActivity(),
															"Error: "
																	+ arg1.getError()
																			.getMessage(),
															Toast.LENGTH_LONG)
															.show();
												}
											});

								}
							}, 5000);
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (MissingDataException e) {
					e.printStackTrace();
				}

			}
		});
		return view;
	}

}
