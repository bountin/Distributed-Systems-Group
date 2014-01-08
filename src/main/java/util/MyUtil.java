package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import message.Request;
import message.Response;
import message.response.HMACResponse;
import message.response.MessageResponse;
import server.NetworkId;
import cli.Shell;

public final class MyUtil
{

	public static File getDirectory(Config config, String key) throws UnvalidConfigException
	{
		File file = MyUtil.getFile(config, key);
		if(!file.isDirectory())
		{
			throw new UnvalidConfigException("not a directory: " + key);
		}
		return file;
	}

	public static File getFile(Config config, String key) throws UnvalidConfigException
	{
		File file = new File(MyUtil.getString(config, key));
		// TODO check can read?
		if(!file.exists())
		{
			throw new UnvalidConfigException("file " + key + " does not exist!");
		}
		return file;
	}

	public static float getFloat(Config config, String key) throws UnvalidConfigException
	{
		try
		{
			return new Float(MyUtil.getString(config, key));
		}
		catch(NumberFormatException nfe)
		{
			throw new UnvalidConfigException("value of " + key + " must be a valid decimal number!");
		}
	}

	public static long getLong(Config config, String key) throws UnvalidConfigException
	{
		try
		{
			return new Long(MyUtil.getString(config, key));
		}
		catch(NumberFormatException nfe)
		{
			throw new UnvalidConfigException("value of " + key + " must be a valid number!");
		}
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

	public static PrivateKey getPrivateKey(Config config, String key, String password) throws UnvalidConfigException
	{
		try
		{
			return EncryptionUtil.getPrivateKeyFromFile(MyUtil.getFile(config, key), password);
		}
		catch(UnvalidConfigException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new UnvalidConfigException("error reading private key from " + key + "\nCause: " + e.getMessage());
		}
	}

	public static PublicKey getPublicKey(Config config, String key) throws UnvalidConfigException
	{
		try
		{
			return EncryptionUtil.getPublicKeyFromFile(MyUtil.getFile(config, key));
		}
		catch(UnvalidConfigException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new UnvalidConfigException("error reading public key from " + key + "\nCause: " + e.getMessage());
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

	public static Response sendRequest(Request object, InetAddress inetAddress, Integer port) throws Exception
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

	public static Response sendRequest(Request request, NetworkId networkId, String hmacKeypath) throws Exception
	{
		for(int i = 0; i < 5; i++)
		{
			Response response = sendRequest(request, networkId.getAddress(), networkId.getPort());
			if(!(response instanceof HMACResponse))
			{
				String s = "Expected a HMAC signed response, got " + response.toString();
				System.out.println(s);
				throw new Exception(s);
			}

			HMACResponse hmacResponse = (HMACResponse)response;
			try
			{
				if(!hmacResponse.verify(hmacKeypath))
				{
					System.out.println("Verification of a fileserver's HMAC Response signature failed: " + hmacResponse.toString());
					continue;
				}

				if(hmacResponse.getResponse() instanceof MessageResponse && ((MessageResponse)hmacResponse.getResponse()).getMessage().contains(HMAC.VERIFICATION_ERROR_MESSAGE))
				{
					System.out.println("A fileserver failed verifying our HMAC of " + request.toString());
					continue;
				}

				return hmacResponse.getResponse();
			}
			catch(HMACException e)
			{
				System.out.println("Verifying HMAC failed: " + e.getMessage());
				// continue;
			}
		}

		return new MessageResponse("HMAC Verification failed five times");
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
