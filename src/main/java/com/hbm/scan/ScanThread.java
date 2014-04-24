package com.hbm.devices.scan.ui.android;

import com.hbm.devices.scan.AnnouncePath;
import com.hbm.devices.scan.AnnounceReceiver;
import com.hbm.devices.scan.FakeStringMessageMulticastReceiver;
import com.hbm.devices.scan.filter.AnnounceFilter;
import com.hbm.devices.scan.filter.FamilytypeMatch;
import com.hbm.devices.scan.filter.Filter;
import com.hbm.devices.scan.filter.JsonFilter;
import com.hbm.devices.scan.MessageReceiver;
import com.hbm.devices.scan.messages.*;
import com.hbm.devices.scan.RegisterDeviceEvent;
import com.hbm.devices.scan.UnregisterDeviceEvent;
import com.hbm.devices.scan.util.ConnectionFinder;
import com.hbm.devices.scan.util.IPv4ScanInterfaces;

import java.util.Observable;
import java.util.Observer;

class ScanThread extends Thread implements Observer {
	private ModuleListAdapter adapter;
	private MessageReceiver announceReceiver;
	private JsonFilter jf;
	private Filter ftFilter;
	private AnnounceFilter af;
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
			connectionFinder = new ConnectionFinder(new IPv4ScanInterfaces().getInterfaces(), false);
			if (useFakeMessages) {
				announceReceiver = new FakeStringMessageMulticastReceiver();
			} else {
	    		announceReceiver = new AnnounceReceiver();
			}
	    	jf = new JsonFilter();
			announceReceiver.addObserver(jf);
			ftFilter = new Filter(new FamilytypeMatch("QuantumX"));
			jf.addObserver(ftFilter);
			af = new AnnounceFilter();
			ftFilter.addObserver(af);
			af.addObserver(this);
	    	announceReceiver.start();
		}
		catch (Exception e) {
		}
	}

	public void kill() {
		announceReceiver.stop();
		announceReceiver.deleteObservers();
		jf.deleteObservers();
		ftFilter.deleteObservers();
		af.deleteObservers();
		af.stop();
	}

	public void update(Observable o, Object arg) {
        AnnouncePath ap;
        if (arg instanceof RegisterDeviceEvent) {
            ap = ((RegisterDeviceEvent)arg).getAnnouncePath();
			ap.cookie = connectionFinder.getConnectableAddress(ap.getAnnounce());
			adapter.updateEntries(arg);
        } else if (arg instanceof UnregisterDeviceEvent) {
            ap = ((UnregisterDeviceEvent)arg).getAnnouncePath();
			adapter.updateEntries(arg);
        }
	}
}
