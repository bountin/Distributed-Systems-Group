package proxy;

import client.ManagementConfig;
import model.RmiServerData;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class ManagementComponent{
	private final ManagementConfig manageConfig;
	private RmiServerData rmiServerData;

	public ManagementComponent(ManagementConfig manageConfig) {

		this.manageConfig = manageConfig;
	}

	public void start(ProxyInfo proxyInfo, ProxyConfig proxyConfig) {
		try {
			LocateRegistry.createRegistry(manageConfig.getPort());
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		try {
			rmiServerData = new RmiServerData(proxyInfo, proxyConfig);
			Naming.rebind(manageConfig.getUrl(), rmiServerData);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	public void stop() {
		try {
			UnicastRemoteObject.unexportObject(rmiServerData, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
