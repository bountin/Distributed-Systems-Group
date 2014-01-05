package message;

import message.response.MessageResponse;
import channel.ObjectChannel;

public class ResponseUtil
{
	protected ObjectChannel aesChannel;

	@SuppressWarnings("unchecked")
	protected <R extends Response> R send(Request request)
	{
		try
		{
			// not logged in
			if(aesChannel == null)
			{
				return (R)new MessageResponse("login required");
			}
			aesChannel.sendObject(request);

			return (R)aesChannel.receiveObject();
		}
		catch(Exception e)
		{
			return (R)new MessageResponse("Error occured (" + e.getMessage() + "), please try again");
		}
	}

}
