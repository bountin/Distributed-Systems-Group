package server;

import java.io.Serializable;
import java.net.InetAddress;

public class NetworkId implements Serializable
{
	private static final long serialVersionUID = -9022285907152434039L;

	private InetAddress address;
	private Integer port;

	public NetworkId(InetAddress address, Integer port)
	{
		this.address = address;
		this.port = port;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		NetworkId other = (NetworkId)obj;
		if(address == null)
		{
			if(other.address != null)
			{
				return false;
			}
		}
		else if(!address.equals(other.address))
		{
			return false;
		}
		if(port == null)
		{
			if(other.port != null)
			{
				return false;
			}
		}
		else if(!port.equals(other.port))
		{
			return false;
		}
		return true;
	}

	public InetAddress getAddress()
	{
		return address;
	}

	public Integer getPort()
	{
		return port;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}

	public void setAddress(InetAddress address)
	{
		this.address = address;
	}

	public void setPort(Integer port)
	{
		this.port = port;
	}

	@Override
	public String toString()
	{
		return address + ":" + port;
	}

}
