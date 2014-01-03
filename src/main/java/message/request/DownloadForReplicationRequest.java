package message.request;

import message.Request;

public class DownloadForReplicationRequest implements Request
{
	private static final long serialVersionUID = 3766587519945013185L;

	private final String filename;

	public DownloadForReplicationRequest(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return filename;
	}

	@Override
	public String toString()
	{
		return "!downloadrequlication " + filename;
	}
}
