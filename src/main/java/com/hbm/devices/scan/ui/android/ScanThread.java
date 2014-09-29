package com.hbm.devices.scan.ui.android;

import java.util.Observable;
import java.util.Observer;

import com.hbm.devices.scan.AnnounceReceiver;
import com.hbm.devices.scan.CommunicationPath;
import com.hbm.devices.scan.DeviceMonitor;
import com.hbm.devices.scan.FakeMessageReceiver;
import com.hbm.devices.scan.MessageParser;
import com.hbm.devices.scan.MessageReceiver;
import com.hbm.devices.scan.events.LostDeviceEvent;
import com.hbm.devices.scan.events.NewDeviceEvent;
import com.hbm.devices.scan.events.UpdateDeviceEvent;
import com.hbm.devices.scan.filter.Filter;
import com.hbm.devices.scan.filter.UUIDMatch;
import com.hbm.devices.scan.util.ConnectionFinder;
import com.hbm.devices.scan.util.ScanInterfaces;

class ScanThread extends Thread implements Observer {
	private ModuleListAdapter adapter;
	private MessageReceiver announceReceiver;
	private MessageParser parser;
	private Filter filter;

	private DeviceMonitor deviceMonitor;
	private ConnectionFinder connectionFinder;
	private boolean useFakeMessages;

	public ScanThread(ModuleListAdapter adapter, boolean useFakeMessages) {
		super("HBM scan thread");
		this.adapter = adapter;
		this.useFakeMessages = useFakeMessages;
	}

	@Override
	public void run() {
		try {
			System.out.println("start announce");
			connectionFinder = new ConnectionFinder(
					new ScanInterfaces().getInterfaces(), false);
			if (useFakeMessages) {
				announceReceiver = new FakeMessageReceiver();
			} else {
				announceReceiver = new AnnounceReceiver();
			}

			parser = new MessageParser(true);
			announceReceiver.addObserver(parser);

//			 String[] families = { "QuantumX" };
//			 filter = new Filter(new FamilytypeMatch(families));
			String[] uuids = { "0009E5001571" };
			filter = new Filter(new UUIDMatch(uuids));
			parser.addObserver(filter);

			deviceMonitor = new DeviceMonitor();
			filter.addObserver(deviceMonitor);

			deviceMonitor.addObserver(this);

			announceReceiver.start();
		} catch (Exception e) {
		}
	}

	public void kill() {
		System.out.println("kill announce");
		announceReceiver.stop();
		announceReceiver.deleteObservers();
		parser.deleteObservers();
		filter.deleteObservers();
		deviceMonitor.stop();
		deviceMonitor.deleteObservers();
	}

	public void update(Observable o, Object arg) {
		CommunicationPath cp;
		if (arg instanceof NewDeviceEvent) {
			NewDeviceEvent event = (NewDeviceEvent) arg;
			cp = event.getAnnouncePath();
			cp.cookie = connectionFinder
					.getConnectableAddress(cp.getAnnounce());
			adapter.updateEntries(arg);
		} else if (arg instanceof LostDeviceEvent) {
			// ap = ((UnregisterDeviceEvent)arg).getAnnouncePath();
			adapter.updateEntries(arg);
		} else if (arg instanceof UpdateDeviceEvent) {
			adapter.updateEntries(arg);
		}
	}
}
