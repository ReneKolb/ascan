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
import java.util.Collections;
import java.util.Comparator;
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
	Comparator<AnnouncePath> listComparator;

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
			ap.cookie = getDomainName(ap.getAnnounce());
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

class UuidComparator implements Comparator<AnnouncePath> {
	public int compare(AnnouncePath a1, AnnouncePath a2) {
		String uuid1 = a1.getAnnounce().getParams().getDevice().getUuid();
		String uuid2 = a2.getAnnounce().getParams().getDevice().getUuid();
		return uuid1.compareToIgnoreCase(uuid2);
	}
}
