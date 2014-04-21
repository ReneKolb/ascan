package com.hbm.scan;

import com.hbm.devices.scan.AnnouncePath;
import com.hbm.devices.scan.AnnounceReceiver;
import com.hbm.devices.scan.FakeStringMessageMulticastReceiver;
import com.hbm.devices.scan.filter.AnnounceFilter;
import com.hbm.devices.scan.filter.FamilytypeMatch;
import com.hbm.devices.scan.filter.Filter;
import com.hbm.devices.scan.filter.JsonFilter;
import com.hbm.devices.scan.messages.*;
import com.hbm.devices.scan.RegisterDeviceEvent;
import com.hbm.devices.scan.UnregisterDeviceEvent;
import com.hbm.devices.scan.util.ConnectionFinder;
import com.hbm.devices.scan.util.IPv4ScanInterfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;

class ScanThread extends Thread implements Observer {
	private ModuleListAdapter adapter;
	private ArrayList<AnnouncePath> entries;
	//private AnnounceReceiver announceReceiver;
	private FakeStringMessageMulticastReceiver announceReceiver;
	private JsonFilter jf;
	private Filter ftFilter;
	private AnnounceFilter af;
	private Comparator<AnnouncePath> listComparator;
	private ConnectionFinder connectionFinder;

	public ScanThread(ModuleListAdapter adapter) {
		super("HBM scan thread");
		this.adapter = adapter;
		listComparator = new UuidComparator();
		entries = new ArrayList<AnnouncePath>();
		adapter.updateEntries(entries);
	}

	@Override
	public void run() {
		try {
			connectionFinder = new ConnectionFinder(new IPv4ScanInterfaces().getInterfaces(), false);
			announceReceiver = new FakeStringMessageMulticastReceiver();
	    	//announceReceiver = new AnnounceReceiver();
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
		synchronized(entries) {
			entries.clear();
		}
		adapter.updateEntries(entries);
	}

	public void update(Observable o, Object arg) {
        AnnouncePath ap;
        if (arg instanceof RegisterDeviceEvent) {
            ap = ((RegisterDeviceEvent)arg).getAnnouncePath();
			ap.cookie = connectionFinder.getConnectableAddress(ap.getAnnounce());
			synchronized(entries) {
				entries.add(ap);
				Collections.sort(entries, listComparator);
			}
        } else if (arg instanceof UnregisterDeviceEvent) {
            ap = ((UnregisterDeviceEvent)arg).getAnnouncePath();
			synchronized(entries) {
				entries.remove(ap);
				Collections.sort(entries, listComparator);
			}
        }
		adapter.updateEntries(entries);
	}
}

class UuidComparator implements Comparator<AnnouncePath> {
	public int compare(AnnouncePath a1, AnnouncePath a2) {
		String uuid1 = a1.getAnnounce().getParams().getDevice().getUuid();
		String uuid2 = a2.getAnnounce().getParams().getDevice().getUuid();
		return uuid1.compareToIgnoreCase(uuid2);
	}
}
