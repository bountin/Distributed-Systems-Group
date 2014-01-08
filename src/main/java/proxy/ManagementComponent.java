package proxy;

import client.ManagementConfig;
import model.RmiData;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class ManagementComponent{
	private final ManagementConfig manageConfig;
	private RmiData rmiData;

	public ManagementComponent(ManagementConfig manageConfig) {

		this.manageConfig = manageConfig;
	}

	public void start(ProxyInfo proxyInfo) {
		try {
			LocateRegistry.createRegistry(manageConfig.getPort());
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		try {
			rmiData = new RmiData(proxyInfo);
			Naming.rebind(manageConfig.getUrl(), rmiData);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	public void stop() {
		try {
			UnicastRemoteObject.unexportObject(rmiData, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
