package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketThread extends Thread
{
	protected ObjectInputStream in = null;
	protected ObjectOutputStream out = null;
	protected Socket socket = null;

	public SocketThread(Socket socket, String name) throws IOException
	{
		super(name);
		this.socket = socket;

		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.in = new ObjectInputStream(socket.getInputStream());
	}

	public void shutdown()
	{
		try
		{
			if(out != null)
			{
				out.close();
			}
			if(in != null)
			{
				in.close();
			}
			if(socket != null)
			{
				socket.close();
			}
		}
		catch(Exception e)
		{
			MyUtil.printStackTrace(e);
		}
		// System.err.println(this.getName() + " CLOSED");
	}

}
