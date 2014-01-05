package channel;

import java.io.IOException;

public interface Channel
{
	public void close();

	public byte[] receiveBytes() throws IOException;

	public void sendBytes(byte[] request) throws IOException;
}
