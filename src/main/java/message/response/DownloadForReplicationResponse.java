package message.response;

import java.nio.charset.Charset;

import message.Response;

/**
 * Requests a {@link model.DownloadTicket} in order to download a file from a file server.
 * <p/>
 * <b>Request (client to proxy)</b>:<br/>
 * {@code !download &lt;filename&gt;}<br/>
 * <b>Response (proxy to client):</b><br/>
 * {@code !download &lt;ticket&gt;}<br/>
 * 
 * @see message.request.DownloadTicketRequest
 */
public class DownloadForReplicationResponse implements Response
{
	private static final long serialVersionUID = 7645562304132169580L;
	private static final Charset CHARSET = Charset.forName("ISO-8859-1");
	private final String filename;
	private final byte[] content;

	public DownloadForReplicationResponse(String filename, byte[] content)
	{
		this.filename = filename;
		this.content = content;
	}

	public byte[] getContent()
	{
		return content;
	}

	public String getFilename()
	{
		return filename;
	}

	@Override
	public String toString()
	{
		return "!datareplication " + new String(getContent(), CHARSET);
	}
}
