package proxy;

import client.ManagementConfig;
import model.RmiData;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class ManagementComponent{
	private final ManagementConfig manageConfig;

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
			RmiData rmiData = new RmiData(proxyInfo);
			Naming.rebind(manageConfig.getUrl(), rmiData);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}
}
