package server;

import java.io.Serializable;

public class FileServerData implements Comparable<FileServerData>, Serializable
{
	private static final long serialVersionUID = 6556828586570295071L;

	private NetworkId networkId;
	private long usage;
	private boolean online;

	public FileServerData(NetworkId networkId)
	{
		this.networkId = networkId;
	}

	@Override
	public int compareTo(FileServerData o)
	{
		int same = new Long(usage).compareTo(o.getUsage());
		if(same == 0)
		{
			return new Integer(getNetworkId().getPort()).compareTo(o.getNetworkId().getPort());
		}
		else
		{
			return same;
		}
	}

	public NetworkId getNetworkId()
	{
		return networkId;
	}

	public long getUsage()
	{
		return usage;
	}

	public boolean isOnline()
	{
		return online;
	}

	public void setNetworkId(NetworkId networkId)
	{
		this.networkId = networkId;
	}

	public void setOnline(boolean online)
	{
		this.online = online;
	}

	public void setUsage(long usage)
	{
		this.usage = usage;
	}

	@Override
	public String toString()
	{
		return getNetworkId() + " " + isOnline() + " " + getUsage();
	}

}
