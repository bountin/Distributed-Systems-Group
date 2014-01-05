package channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectByteArrayConverterChannel extends ChannelDecorator implements ObjectChannel
{
	public ObjectByteArrayConverterChannel(Channel channel)
	{
		super(channel);
	}

	public static Object convertByteArrayToObject(byte[] bytes)
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream in = null;
		try
		{
			in = new ObjectInputStream(bis);
			return in.readObject();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				if(in != null)
				{
					in.close();
				}
			}
			catch(IOException ex)
			{
				// ignore close exception
			}
			try
			{
				bis.close();
			}
			catch(IOException ex)
			{
				// ignore close exception
			}
		}
	}

	public static byte[] convertObjectToByteArray(Object source)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try
		{
			out = new ObjectOutputStream(bos);
			out.writeObject(source);
			return bos.toByteArray();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				if(out != null)
				{
					out.close();
				}
			}
			catch(IOException ex)
			{
				// ignore close exception
			}
			try
			{
				bos.close();
			}
			catch(IOException ex)
			{
				// ignore close exception
			}
		}
	}

	@Override
	public void close()
	{
		super.close();

	}

	@Override
	public Object receiveObject() throws IOException
	{
		byte[] response = super.receiveBytes();
		return convertByteArrayToObject(response);
	}

	@Override
	public void sendObject(Object object) throws IOException
	{
		super.sendBytes(convertObjectToByteArray(object));
	}
}
