package server;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.TimerTask;

public class IsAliveSender extends TimerTask
{
	private NetworkId fileServerId;
	private NetworkId proxyUdpId;

	public IsAliveSender(NetworkId fileServerId, NetworkId proxyUdpId) throws SocketException
	{
		this.fileServerId = fileServerId;
		this.proxyUdpId = proxyUdpId;

	}

	@Override
	public void run()
	{
		try
		{
			DatagramSocket socket = new DatagramSocket();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			IsAlivePacket isAlivePacket = new IsAlivePacket(fileServerId);
			oos.writeObject(isAlivePacket);
			oos.flush();
			byte[] buf = bos.toByteArray();

			DatagramPacket packet = new DatagramPacket(buf, buf.length, proxyUdpId.getAddress(), proxyUdpId.getPort());

			socket.send(packet);
			socket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

}
