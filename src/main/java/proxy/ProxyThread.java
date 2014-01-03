package proxy;

import java.io.IOException;
import java.net.Socket;

import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.ExitRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.MessageResponse;
import util.MyUtil;
import util.SocketThread;

public class ProxyThread extends SocketThread
{
	private ProxyManager proxyManager = new ProxyManager();

	public ProxyThread(Socket socket, String name) throws IOException
	{
		super(socket, name);
	}

	@Override
	public void run()
	{
		try
		{
			Object inRequest, outResponse = new MessageResponse("command not found");
			while(!socket.isClosed() && (inRequest = in.readObject()) != null)
			{
				outResponse = new MessageResponse("command \"" + inRequest.toString() + "\" not found on fileserver");
				if(inRequest instanceof LoginRequest)
				{
					outResponse = proxyManager.login((LoginRequest)inRequest);
				}
				if(inRequest instanceof LogoutRequest)
				{
					outResponse = proxyManager.logout();
				}
				if(inRequest instanceof CreditsRequest)
				{
					outResponse = proxyManager.credits();
				}
				if(inRequest instanceof BuyRequest)
				{
					outResponse = proxyManager.buy((BuyRequest)inRequest);
				}
				if(inRequest instanceof DownloadTicketRequest)
				{
					outResponse = proxyManager.download((DownloadTicketRequest)inRequest);
				}
				if(inRequest instanceof UploadRequest)
				{
					outResponse = proxyManager.upload((UploadRequest)inRequest);
				}
				if(inRequest instanceof ListRequest)
				{
					outResponse = proxyManager.list();
				}
				if(inRequest instanceof ExitRequest)
				{
					outResponse = proxyManager.logout();
				}

				out.writeObject(outResponse);
				out.flush();

				if(inRequest instanceof ExitRequest)
				{
					break;
				}
			}
		}
		catch(Exception e)
		{
			MyUtil.printStackTrace(e);
		}
		finally
		{
			shutdown();
		}
	}

}
