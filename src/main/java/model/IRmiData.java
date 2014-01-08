package model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiData extends Remote {
	public int readQuorum() throws RemoteException;
	public int writeQuorum() throws RemoteException;
}
