package message.request;

import message.Request;

public class ExitRequest implements Request
{
	private static final long serialVersionUID = -8927994807051740903L;

	@Override
	public String toString()
	{
		return "!exit";
	}
}
