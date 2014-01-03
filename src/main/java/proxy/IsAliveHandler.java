package proxy;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;

import server.IsAlivePacket;

public class IsAliveHandler extends Thread
{
	private boolean closed;
	private DatagramSocket datagramSocket;
	private Timer timer = new Timer();
	private FileServerOfflineHandler fileServerOfflineHandler;

	public IsAliveHandler(DatagramSocket datagramSocket, long timeout, long checkPeriod)
	{
		super();
		this.datagramSocket = datagramSocket;
		fileServerOfflineHandler = new FileServerOfflineHandler(timeout);
		timer.schedule(fileServerOfflineHandler, 0, checkPeriod);
	}

	public boolean checkIfClosed()
	{
		return closed || datagramSocket.isClosed();
	}

	@Override
	public void run()
	{
		byte[] buf;
		while(!checkIfClosed())
		{
			try
			{
				buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				datagramSocket.receive(packet);

				ByteArrayInputStream in = new ByteArrayInputStream(packet.getData());
				ObjectInputStream is = new ObjectInputStream(in);
				IsAlivePacket isAlivePacket = (IsAlivePacket)is.readObject();

				fileServerOfflineHandler.addPacket(isAlivePacket);
				ProxyInfo.getInstance().setOnline(isAlivePacket.getNetworkId());
			}

			catch(Exception e)
			{
				if(!e.getMessage().equals("socket closed"))
				{
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void setClosed(boolean closed)
	{
		this.closed = closed;
	}

	public synchronized void shutdown()
	{
		setClosed(true);
		datagramSocket.close();
		timer.cancel();
	}

}
