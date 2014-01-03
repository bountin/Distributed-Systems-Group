package message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import message.response.MessageResponse;

public class ResponseUtil
{
	protected ObjectOutputStream out;
	protected ObjectInputStream in;

	protected <A extends Response> A send(Request request)
	{
		try
		{
			out.writeObject(request);
			out.flush();

			return (A)in.readObject();
		}
		catch(Exception e)
		{
			return (A)new MessageResponse("Error occured (" + e.getMessage() + "), please try again");
		}
	}

}
