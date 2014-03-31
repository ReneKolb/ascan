/**
 * Convenience class to gather all network interfaces eligible for
 * multicast scanning.
 */
package com.hbm.devices.scan;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

public class IPv4ScanInterfaces {

	private static final String TAG = "Scan";
	public IPv4ScanInterfaces() throws SocketException {
		interfaces = new LinkedList<NetworkInterface>();

		Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();

		while (ifs.hasMoreElements()) {
			NetworkInterface iface = ifs.nextElement();
			if (willScan(iface)) {
				interfaces.add(iface);
			}
		}
	}

	public Collection<NetworkInterface> getInterfaces() {
		return interfaces;
	}
	
	private static boolean willScan(NetworkInterface iface) throws SocketException {

		if (iface.isLoopback()) {
			return false;
		}
		if (!iface.isUp()) {
			return false;
		}
		if (!hasConfiguredIPv4Address(iface)) {
			return false;
		}
		if (iface.supportsMulticast()) {
			return true;
		}
		return false;
	}

	private static boolean hasConfiguredIPv4Address(NetworkInterface iface) {
		Enumeration<InetAddress> addrs = iface.getInetAddresses();
		while (addrs.hasMoreElements()) {
			InetAddress addr = addrs.nextElement();
			if (addr instanceof Inet4Address) {
				Inet4Address addr4 = (Inet4Address)addr;
				if (!addr4.isAnyLocalAddress()) {
					return true;
				}
			}
		}
		return false;
	}

	private LinkedList<NetworkInterface> interfaces;
}
