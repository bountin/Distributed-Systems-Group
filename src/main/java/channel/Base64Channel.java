package channel;

import java.io.IOException;

import org.bouncycastle.util.encoders.Base64;

public class Base64Channel extends ChannelDecorator
{
	public Base64Channel(Channel channel)
	{
		super(channel);
	}

	@Override
	public byte[] receiveBytes() throws IOException
	{
		byte[] response = super.receiveBytes();
		return Base64.decode(response);
	}

	@Override
	public void sendBytes(byte[] request) throws IOException
	{
		byte[] base64Message = Base64.encode(request);
		super.sendBytes(base64Message);
	}
}
