package server;

import java.io.Serializable;

public class FileServerData implements Serializable
{
	private static final long serialVersionUID = 6556828586570295071L;

	private NetworkId networkId;
	private long usage;
	private boolean online;

	public FileServerData(NetworkId networkId)
	{
		this.networkId = networkId;
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

}
