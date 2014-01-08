package proxy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import message.MessageResponseException;
import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.HMACRequest;
import message.request.UploadRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
import objects.User;
import server.FileServerData;
import auth.AuthenticationException;

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

		String filename = request.getFilename();

		Integer highestVersion = null;
		FileServerData minUsageFileServer = null;

		try
		{
			// set highestVersion and minUsageFileServer
			VersionFileServerData verionFileServer = proxyInfo.getReplicationInfo().getLowestReadQuorumWithHighestVersion(filename);
			highestVersion = verionFileServer.getVersion();
			minUsageFileServer = verionFileServer.getFileServerData();
		}
		catch(MessageResponseException e)
		{
			return new MessageResponse(e.getMessage());
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

		String checkSum = util.ChecksumUtils.generateChecksum(user.getUsername(), request.getFilename(), highestVersion, filesize);
		DownloadTicket downloadTicket = new DownloadTicket(user.getUsername(), request.getFilename(), checkSum, minUsageFileServer.getNetworkId().getAddress(), minUsageFileServer.getNetworkId().getPort());
		proxyInfo.decreaseCredits(user, filesize);
		proxyInfo.increaseUsage(minUsageFileServer, filesize);
		proxyInfo.increaseFileDownloadCounter(request.getFilename());
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

	public void login(String username) throws AuthenticationException
	{
		User user = proxyInfo.getUsers().get(username);
		if(user == null)
		{
			throw new AuthenticationException("no user found on proxy");
		}
		this.user = user;
		this.user.setOnline(true);
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

		// get version from read quorum servers and update request
		int version;
		HMACRequest<UploadRequest> hmacRequest=null;
		try
		{
			version = proxyInfo.getNextFileVersion(request.getFilename());
			request = new UploadRequest(request.getFilename(), version, request.getContent());
			hmacRequest = new HMACRequest<UploadRequest>(request, proxyInfo.getHmacKeyPath());
		}
		catch(Exception e)
		{
			return new MessageResponse("error requesting current file version\ncause: " + e.getMessage());
		}

		// send to write quorum servers
		List<FileServerData> fileServersToWrite = proxyInfo.getWriteQuorumServers();
		MessageResponse response = proxyInfo.sendUploadRequestToServers(hmacRequest, fileServersToWrite);
		if(!response.getMessage().equals("success"))
		{
			return response;
		}

		// update infos
		proxyInfo.addFile(new FileInfo(request.getFilename(), request.getContent().length, version));
		proxyInfo.increaseCredits(user, request.getContent().length * 2);
		for(FileServerData data : fileServersToWrite)
		{
			proxyInfo.decreaseUsage(data.getNetworkId(), request.getContent().length * 2);
		}
		return response;
	}
}
