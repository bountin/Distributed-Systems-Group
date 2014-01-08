package model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface IRmiServerData extends Remote {
	int readQuorum() throws RemoteException;
	int writeQuorum() throws RemoteException;
	TopDownloads topDownloads(int count) throws RemoteException;
	PublicKey getProxyPublicKey() throws RemoteException;
	String setUserKey(String username, PublicKey key) throws RemoteException;
	void subscribe(IRmiClientData data) throws RemoteException;
}
