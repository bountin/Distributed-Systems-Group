package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import message.Request;
import message.Response;
import message.response.MessageResponse;
import server.NetworkId;
import cli.Shell;

public final class MyUtil
{

	public static File getFile(Config config, String key) throws UnvalidConfigException
	{
		return new File(MyUtil.getString(config, key));
	}

	public static long getMilliseconds(Config config, String key) throws UnvalidConfigException
	{
		try
		{
			return new Long(MyUtil.getString(config, key));
		}
		catch(NumberFormatException nfe)
		{
			throw new UnvalidConfigException("value of " + key + " must be a valid number of milliseconds!");
		}
	}

	public static Integer getPort(Config config, String key) throws UnvalidConfigException
	{
		try
		{
			Integer tcpPort = config.getInt(key);
			if(tcpPort <= 1023)
			{
				throw new UnvalidConfigException(key + ": don't use a well-known port!");
			}
			else
			{
				return tcpPort;
			}
		}
		catch(NumberFormatException nfe)
		{
			throw new UnvalidConfigException("value of " + key + " must be a valid port number!");
		}
	}

	public static String getString(Config config, String key) throws UnvalidConfigException
	{
		String value = config.getString(key);
		if(value == null || value.length() == 0)
		{
			throw new UnvalidConfigException("config-param " + key + " not set");
		}
		else
		{
			return value;
		}

	}

	public static void printStackTrace(Exception e)
	{
		if(e.getMessage() != null && e.getMessage().equals("socket closed"))
		{}
		else
		{
			e.printStackTrace();
		}

	}

	public static Response sendRequest(Request object, InetAddress inetAddress, Integer port, String errorMessage)
	{
		Socket socket = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try
		{
			socket = new Socket(inetAddress, port);
			oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

			oos.writeObject(object);
			oos.flush();

			return (Response)ois.readObject();
		}
		catch(Exception e)
		{
			return new MessageResponse(errorMessage + e.getMessage());
		}
		finally
		{
			try
			{
				if(oos != null)
				{
					oos.close();
				}
				if(ois != null)
				{
					ois.close();
				}
				if(socket != null)
				{
					socket.close();
				}
			}
			catch(IOException e)
			{}
		}

	}

	public static Response sendRequest(Request request, NetworkId networkId, String errorMessage)
	{
		return sendRequest(request, networkId.getAddress(), networkId.getPort(), errorMessage);
	}

	public static void writeToShell(Shell shell, String string)
	{
		try
		{
			if(shell != null)
			{
				shell.writeLine(string);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
