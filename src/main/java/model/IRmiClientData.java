package model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiClientData extends Remote {
	void notifyDownloadSubscription() throws RemoteException;
	boolean test(String filename, int count) throws RemoteException;
	String getUser() throws RemoteException;
}
