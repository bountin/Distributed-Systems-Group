package channel;

import java.io.IOException;

public interface ObjectChannel
{
	public void close();

	public Object receiveObject() throws IOException;

	public void sendObject(Object object) throws IOException;
}
