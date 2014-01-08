package model;

import objects.User;
import proxy.FileInfo;
import proxy.ProxyConfig;
import proxy.ProxyInfo;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.Map;

public class RmiServerData extends UnicastRemoteObject implements IRmiServerData {
	private static final long serialVersionUID = -2673358994354617419L;
	private final ProxyInfo proxyInfo;
	private final ProxyConfig proxyConfig;

	public RmiServerData(ProxyInfo proxyInfo, ProxyConfig proxyConfig) throws RemoteException {
		super(0);
		this.proxyInfo = proxyInfo;
		this.proxyConfig = proxyConfig;
	}

	public int readQuorum() throws RemoteException {
		return proxyInfo.getReplicationInfo().getReadQuorum();
	}

	public int writeQuorum() throws RemoteException {
		return proxyInfo.getReplicationInfo().getWriteQuorum();
	}

	public TopDownloads topDownloads(int count) throws RemoteException {
		TopDownloads ds = new TopDownloads();

		Map<String,FileInfo> files = proxyInfo.getFiles();
		for (FileInfo info: files.values()) {
			ds.add(new TopDownload(info.getFilename(), info.getDownloadCounter()));
		}

		return ds.trimmedSet(count);
	}

	public PublicKey getProxyPublicKey() throws RemoteException {
		return proxyConfig.getPublicKey();
	}

	public String setUserKey(String username, PublicKey key) throws RemoteException {
		User user = proxyInfo.getUsers().get(username);
		if (user == null) {
			return "User not found";
		}

		try {
			proxyInfo.getUserKeyHolder().setPublicKey(username, key);
		} catch (IOException e) {
			return "Writing key failed: " + e.getMessage();
		}

		return null;
	}

	public void subscribe(IRmiClientData data) throws RemoteException {
		proxyInfo.addSubscription(data);
	}
}
