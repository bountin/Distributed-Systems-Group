package util;

import java.io.IOException;
import java.net.Socket;

import channel.ObjectChannel;

public class SecureSocketThread extends Thread
{
	protected ObjectChannel aesChannel;
	protected Socket socket = null;

	public SecureSocketThread(Socket socket, String name) throws IOException
	{
		super(name);
		this.socket = socket;
	}

	public void shutdown()
	{
		if(aesChannel != null)
		{
			aesChannel.close();
		}
		try
		{
			if(socket != null)
			{
				socket.close();
			}
		}
		catch(Exception e)
		{
			MyUtil.printStackTrace(e);
		}
	}

}
