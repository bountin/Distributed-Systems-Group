package channel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import message.Response;
import util.MyUtil;

public class TCPChannel implements Channel
{
	protected DataOutputStream out;
	protected DataInputStream in;

	public TCPChannel(Socket socket) throws IOException
	{
		this.out = new DataOutputStream(socket.getOutputStream());
		this.in = new DataInputStream(socket.getInputStream());
	}

	@Override
	public void close()
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
		}
		catch(Exception e)
		{
			MyUtil.printStackTrace(e);
		}

	}

	@Override
	public byte[] receiveBytes() throws IOException
	{
		int len = in.readInt();
		byte[] data = new byte[len];
		if(len > 0)
		{
			in.readFully(data);
		}
		return data;
	}

	public <R extends Response> R receiveObject() throws Exception
	{
		byte[] byteResponse = receiveBytes();
		Object response = ObjectByteArrayConverterChannel.convertByteArrayToObject(byteResponse);
		return (R)response;
	}

	@Override
	public void sendBytes(byte[] request) throws IOException
	{
		int len = request.length;
		out.writeInt(len);
		if(len > 0)
		{
			out.write(request, 0, len);
		}
	}
}
