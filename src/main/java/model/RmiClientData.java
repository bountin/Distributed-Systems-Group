package model;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClientData extends UnicastRemoteObject implements IRmiClientData {
	private static final long serialVersionUID = -2673358994354617420L;
	private String user;
	private String filename;
	private int count;

	public RmiClientData(String user, String filename, int count) throws RemoteException {
		this.user = user;
		this.filename = filename;
		this.count = count;
	}

	public void notifyDownloadSubscription() throws RemoteException {
		System.out.println(String.format("Notification: %s got downloaded %d times!", filename, count));
	}

	public boolean test(String filename, int count) {
		return this.filename.equals(filename) && this.count == count;
	}

	public String getUser() throws RemoteException {
		return user;
	}

	public String getFilename() throws RemoteException {
		return filename;
	}

	public int getCount() throws RemoteException {
		return count;
	}

	public void unregister() throws RemoteException {
		try {
			UnicastRemoteObject.unexportObject(this, true);
		} catch (NoSuchObjectException ignored) {}
	}
}
