package message.response;

import message.Response;

public class ReadQuorumResponse implements Response
{
	private static final long serialVersionUID = 6764148904975070420L;

	private final boolean inReadQuorum;

	public ReadQuorumResponse(boolean inReadQuorum)
	{
		this.inReadQuorum = inReadQuorum;
	}

	public boolean isInReadQuorum()
	{
		return inReadQuorum;
	}

}
