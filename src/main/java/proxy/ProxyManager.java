package proxy;

import java.io.FileNotFoundException;
import java.io.IOException;

import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.ListResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
import objects.User;
import server.FileServerData;

public class ProxyManager implements IProxy
{
	private User user;
	private ProxyInfo proxyInfo = ProxyInfo.getInstance();

	@Override
	public Response buy(BuyRequest credits) throws IOException
	{
		if(user == null)
		{
			return new MessageResponse("login required");
		}
		proxyInfo.increaseCredits(user, credits.getCredits());
		return new BuyResponse(user.getCredits());
	}

	@Override
	public Response credits() throws IOException
	{
		if(user == null)
		{
			return new MessageResponse("login required");
		}
		return new CreditsResponse(proxyInfo.getUsers().get(user.getUsername()).getCredits());
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException
	{
		if(user == null)
		{
			return new MessageResponse("login required");
		}
		long filesize;
		FileServerData fileServerData = proxyInfo.getFileServerWithLowestUsage();
		if(fileServerData == null)
		{
			return new MessageResponse("no fileserver available");
		}
		try
		{
			filesize = proxyInfo.getFileSize(request.getFilename());
			long credits = proxyInfo.getCredits(user);
			if(credits < filesize)
			{
				return new MessageResponse("not enough credits " + credits + ", needed: " + filesize);
			}
		}
		catch(FileNotFoundException e)
		{
			return new MessageResponse("file does not exist");
		}
		catch(Exception e)
		{
			return new MessageResponse("credits not verifiable");
		}

		// TODO lab2 version not 0
		int version = 0;
		String checkSum = util.ChecksumUtils.generateChecksum(user.getUsername(), request.getFilename(), version, filesize);
		DownloadTicket downloadTicket = new DownloadTicket(user.getUsername(), request.getFilename(), checkSum, fileServerData.getNetworkId().getAddress(), fileServerData.getNetworkId().getPort());
		proxyInfo.decreaseCredits(user, filesize);

		return new DownloadTicketResponse(downloadTicket);
	}

	@Override
	public Response list() throws IOException
	{
		if(user == null)
		{
			return new MessageResponse("login required");
		}
		return new ListResponse(proxyInfo.getFiles().keySet());
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException
	{
		User user = proxyInfo.getUsers().get(request.getUsername());
		if(user == null || !user.getPassword().equals(request.getPassword()))
		{
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		else
		{
			this.user = user;
			this.user.setOnline(true);
			return new LoginResponse(LoginResponse.Type.SUCCESS);
		}
	}

	@Override
	public MessageResponse logout() throws IOException
	{
		if(user == null)
		{
			return new MessageResponse("login required");
		}
		this.user.setOnline(false);
		this.user = null;
		return new MessageResponse("Successfully logged out.");
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException
	{
		if(user == null)
		{
			return new MessageResponse("login required");
		}
		MessageResponse response = proxyInfo.sendUploadRequestToServers(request, proxyInfo.getFileServerData().keySet());
		if(!response.getMessage().equals("success"))
		{
			return response;
		}
		proxyInfo.addFile(new FileInfo(request.getFilename(), request.getContent().length, request.getVersion()));
		proxyInfo.increaseCredits(user, request.getContent().length * 2);
		return response;
	}
}
