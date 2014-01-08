package model;

import proxy.FileInfo;
import proxy.ProxyInfo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

public class RmiData  extends UnicastRemoteObject implements IRmiData{
	private static final long serialVersionUID = -2673358994354617419L;
	private final ProxyInfo proxyInfo;

	public RmiData(ProxyInfo proxyInfo) throws RemoteException {
		super(0);
		this.proxyInfo = proxyInfo;
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
}
