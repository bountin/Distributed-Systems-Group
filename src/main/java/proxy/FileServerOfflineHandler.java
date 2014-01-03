package proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import server.IsAlivePacket;
import server.NetworkId;

public class FileServerOfflineHandler extends TimerTask
{
	private Map<NetworkId, Date> packets = Collections.synchronizedMap(new HashMap<NetworkId, Date>());
	private long timeout;
	private ProxyInfo proxyInfo = ProxyInfo.getInstance();

	public FileServerOfflineHandler(long timeout)
	{
		this.timeout = timeout;
	}

	public synchronized void addPacket(IsAlivePacket isAlivePacket)
	{
		packets.put(isAlivePacket.getNetworkId(), isAlivePacket.getAliveSent());
	}

	@Override
	public synchronized void run()
	{
		long now = new Date().getTime();
		List<NetworkId> toRemove = new ArrayList<NetworkId>();

		for(Map.Entry<NetworkId, Date> entry : packets.entrySet())
		{
			long lastAlive = now - entry.getValue().getTime();

			if(lastAlive > timeout)
			{
				NetworkId networkId = entry.getKey();
				proxyInfo.setOffline(networkId);
				toRemove.add(networkId);
			}
		}
		for(NetworkId networkId : toRemove)
		{
			packets.remove(networkId);
		}

	}
}
