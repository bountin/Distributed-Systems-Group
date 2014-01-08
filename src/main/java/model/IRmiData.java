package model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface IRmiData extends Remote {
	int readQuorum() throws RemoteException;
	int writeQuorum() throws RemoteException;
	TopDownloads topDownloads(int count) throws RemoteException;
	PublicKey getProxyPublicKey() throws RemoteException;
}
