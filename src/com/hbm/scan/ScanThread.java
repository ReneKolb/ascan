package com.hbm.scan;

import com.hbm.devices.scan.AnnouncePath;
import com.hbm.devices.scan.AnnounceReceiver;
import com.hbm.devices.scan.FakeStringMessageMulticastReceiver;
import com.hbm.devices.scan.filter.AnnounceFilter;
import com.hbm.devices.scan.filter.FamilytypeMatch;
import com.hbm.devices.scan.filter.Filter;
import com.hbm.devices.scan.filter.JsonFilter;
import com.hbm.devices.scan.IPv4ScanInterfaces;
import com.hbm.devices.scan.messages.*;
import com.hbm.devices.scan.RegisterDeviceEvent;
import com.hbm.devices.scan.UnregisterDeviceEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

class ScanThread extends Thread implements Observer {
	private ModuleListAdapter adapter;
	ArrayList<AnnouncePath> entries;
	//AnnounceReceiver announceReceiver;
	FakeStringMessageMulticastReceiver announceReceiver;
	JsonFilter jf;
	Filter ftFilter;
	AnnounceFilter af;

	public ScanThread(ModuleListAdapter adapter) {
		super("HBM scan thread");
		this.adapter = adapter;
		entries = new ArrayList<AnnouncePath>();
	}

	@Override
	public void run() {
		try {
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
	}

	public void update(Observable o, Object arg) {
        AnnouncePath ap;
        if (arg instanceof RegisterDeviceEvent) {
            ap = ((RegisterDeviceEvent)arg).getAnnouncePath();
			ap.cookie = getDomainName(ap.getAnnounce());
			synchronized(entries) {
				entries.add(ap);
			}
        } else if (arg instanceof UnregisterDeviceEvent) {
            ap = ((UnregisterDeviceEvent)arg).getAnnouncePath();
			synchronized(entries) {
				entries.remove(ap);
			}
        }
		adapter.updateEntries(entries);
	}
	
	private String getDomainName(Announce announce) {
		Iterable<IPv4Entry> ips = announce.getParams().getNetSettings().getInterface().getIPv4();
		Iterator<IPv4Entry> iterator = ips.iterator();
		while (iterator.hasNext()) {
			IPv4Entry ipEntry = iterator.next();
			String ip = ipEntry.getAddress();
			try {
				InetAddress address = InetAddress.getByName(ip);
				String fqdn = address.getCanonicalHostName();
				return fqdn;
			} catch (UnknownHostException e) {
				return null;
			}
		}
		return null;
	}
}

