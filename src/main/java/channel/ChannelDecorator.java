package channel;

import java.io.IOException;

public abstract class ChannelDecorator implements Channel
{
	protected Channel channel;

	public ChannelDecorator(Channel channel)
	{
		this.channel = channel;
	}

	public void close()
	{
		channel.close();
	}

	@Override
	public byte[] receiveBytes() throws IOException
	{
		return channel.receiveBytes();
	}

	@Override
	public void sendBytes(byte[] request) throws IOException
	{
		channel.sendBytes(request);
	}
}
