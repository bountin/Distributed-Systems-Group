package server;

import java.io.Serializable;
import java.util.Date;

public class IsAlivePacket implements Serializable
{
	private static final long serialVersionUID = 8863155602017905772L;

	private NetworkId networkId;
	private Date aliveSent;

	public IsAlivePacket(NetworkId networkId)
	{
		this.networkId = networkId;
		this.aliveSent = new Date();
	}

	public Date getAliveSent()
	{
		return aliveSent;
	}

	public NetworkId getNetworkId()
	{
		return networkId;
	}

	public void setAliveSent(Date aliveSent)
	{
		this.aliveSent = aliveSent;
	}

	public void setNetworkId(NetworkId networkId)
	{
		this.networkId = networkId;
	}

}
