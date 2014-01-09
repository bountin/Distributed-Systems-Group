package proxy;

import java.io.IOException;
import java.net.Socket;

import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.ExitRequest;
import message.request.ListRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.MessageResponse;
import util.MyUtil;
import util.SecureSocketThread;
import auth.ProxyAuthenticator;

public class ProxyThread extends SecureSocketThread
{
	private ProxyManager proxyManager = new ProxyManager();
	private ProxyConfig proxyConfig;

	public ProxyThread(Socket socket, String name, ProxyConfig proxyConfig) throws IOException
	{
		super(socket, name);
		this.proxyConfig = proxyConfig;
	}

	@Override
	public void run()
	{
		try
		{
			while(!socket.isClosed())
			{
				while(aesChannel == null)
				{
					aesChannel = ProxyAuthenticator.authenticate(socket, proxyManager, proxyConfig);
				}

				Object inRequest, outResponse = new MessageResponse("command not found");
				while(aesChannel != null && (inRequest = aesChannel.receiveObject()) != null)
				{
					boolean loggedOut = false;
					outResponse = new MessageResponse("command \"" + inRequest.toString() + "\" not found on fileserver");

					if(inRequest instanceof LogoutRequest)
					{
						outResponse = proxyManager.logout();
						loggedOut = true;
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

					aesChannel.sendObject(outResponse);

					if(loggedOut)
					{
						aesChannel = null;
					}
					if(inRequest instanceof ExitRequest)
					{
						return;
					}
				}
			}
		}
		catch(Exception e)
		{
			MyUtil.printStackTrace(e);
		}
		finally
		{
			proxyManager.shutdown();
			shutdown();
		}
	}

}
