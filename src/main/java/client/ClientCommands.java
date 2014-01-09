package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.security.PublicKey;

import message.Response;
import message.ResponseUtil;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.ExitRequest;
import message.request.ListRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.DownloadFileResponse;
import message.response.DownloadTicketResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
import model.IRmiServerData;
import model.RmiClientData;
import model.TopDownloads;
import util.MyUtil;
import auth.ClientAuthenticator;
import cli.Command;

public abstract class ClientCommands extends ResponseUtil implements IClientCli, IClientRmiCli
{
	protected ClientConfig clientConfig;
	protected ManagementConfig manageConfig;
	protected Socket proxySocket;
	protected IRmiServerData rmiData;
	protected String user;

	/* !buy <credits> */
	@Override
	@Command
	public Response buy(long credits)
	{
		return send(new BuyRequest(credits));
	}

	@Override
	@Command
	public Response credits()
	{
		return send(new CreditsRequest());
	}

	/* !download <filename> */
	@Override
	@Command
	public Response download(String filename)
	{
		File fileToWrite = new File(getDownloadDir(), filename);
		// request downloadTicket
		Object response = send(new DownloadTicketRequest(filename));
		if(response.getClass().equals(MessageResponse.class))
		{
			return (MessageResponse)response;
		}
		DownloadTicketResponse downloadTicketResponse = (DownloadTicketResponse)response;

		// request file
		DownloadTicket ticket = downloadTicketResponse.getTicket();
		try
		{
			response = MyUtil.sendRequest(new DownloadFileRequest(ticket), ticket.getAddress(), ticket.getPort());
			if(response.getClass().equals(MessageResponse.class))
			{
				return (MessageResponse)response;
			}
		}
		catch(Exception e)
		{
			return new MessageResponse("error sending downloadticket to fileserver\n cause: " + e.getMessage());
		}
		DownloadFileResponse downloadFileResponse = (DownloadFileResponse)response;
		try
		{
			if(fileToWrite.exists())
			{
				fileToWrite.delete();
			}
		}
		catch(Exception e)
		{
			return new MessageResponse("Error deleting file " + fileToWrite.getPath() + ": " + e.getMessage());
		}
		try
		{
			FileOutputStream fos = new FileOutputStream(fileToWrite);
			fos.write(downloadFileResponse.getContent());
			fos.close();
		}
		catch(Exception e)
		{
			return new MessageResponse("Error writing file to downloadDir: " + e.getMessage());
		}

		return downloadFileResponse;
	}

	@Override
	@Command
	public MessageResponse exit()
	{
		MessageResponse response = send(new ExitRequest());
		if(response.getMessage().equals("Error occured (Software caused connection abort: recv failed), please try again"))
		{
			response = new MessageResponse("proxy already stopped");
		}
		shutdown();
		return response;
	}

	public abstract File getDownloadDir();

	@Override
	@Command
	public MessageResponse getProxyPublicKey()
	{
		try
		{
			PublicKey key = rmiData.getProxyPublicKey();
			clientConfig.setPublicProxyKey(key);
			return new MessageResponse("Successfully received public key of Proxy.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new MessageResponse("An error occurred: " + e.getMessage());
		}
	}

	@Override
	@Command
	public Response list()
	{
		return send(new ListRequest());
	}

	/* !login <username> <password> */
	@Override
	@Command
	public LoginResponse login(String username, String password)
	{
		if(aesChannel != null)
		{
			return new LoginResponse(LoginResponse.Type.ALREADY_LOGGED_IN);
		}
		try
		{
			aesChannel = ClientAuthenticator.authenticate(username, clientConfig, proxySocket, password);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
		user = username;
		return new LoginResponse(LoginResponse.Type.SUCCESS);
	}

	@Override
	@Command
	public MessageResponse logout()
	{
		try
		{
			return send(new LogoutRequest());
		}
		finally
		{
			aesChannel = null;
			user = null;
		}
	}

	@Override
	@Command
	public MessageResponse readQuorum()
	{
		try
		{
			return new MessageResponse("Read-Quorum is set to " + rmiData.readQuorum() + ".");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new MessageResponse("An error occurred: " + e.getMessage());
		}
	}

	@Override
	@Command
	public MessageResponse setUserPublicKey(String username)
	{
		try
		{
			PublicKey key = clientConfig.getPrivateKeyDir().getPublicKey(username);
			String message = rmiData.setUserKey(username, key);
			if(message != null)
			{
				throw new Exception(message);
			}

			return new MessageResponse("Successfully transmitted public key of user " + username);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new MessageResponse("An error occurred: " + e.getMessage());
		}
	}

	public abstract void shutdown();

	@Override
	@Command
	public MessageResponse subscribe(String filename, int count)
	{
		if(user == null)
		{
			return new MessageResponse("Please log in to subscribe");
		}
		try
		{
			String result = rmiData.subscribe(new RmiClientData(user, filename, count));
			if(result == null)
			{
				return new MessageResponse("Successfully subscribed for file " + filename);
			}
			else
			{
				return new MessageResponse(result);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new MessageResponse("An error occurred: " + e.getMessage());
		}
	}

	@Override
	@Command
	public MessageResponse topThreeDownloads()
	{
		try
		{
			TopDownloads downloads = rmiData.topDownloads(3);
			StringBuilder sb = new StringBuilder();
			sb.append("Top Three Downloads:\n");

			sb.append((downloads.size() > 0 ? downloads.toString() : "No downloads at all."));

			return new MessageResponse(sb.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new MessageResponse("An error occurred: " + e.getMessage());
		}
	}

	/* !upload <filename> */
	@Override
	@Command
	public MessageResponse upload(String filename)
	{
		File file = new File(clientConfig.getDownloadDir(), filename);
		if(!file.exists())
		{
			return new MessageResponse("file " + filename + " not in downloadDir " + clientConfig.getDownloadDir());
		}
		byte[] content = new byte[new Long(file.length()).intValue()];
		try
		{
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(content);
			fileInputStream.close();
		}
		catch(Exception e)
		{
			return new MessageResponse("error reading file: " + e.getMessage());
		}
		return send(new UploadRequest(filename, 0, content));
	}

	@Override
	@Command
	public MessageResponse writeQuorum()
	{
		try
		{
			return new MessageResponse("Write-Quorum is set to " + rmiData.writeQuorum() + ".");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new MessageResponse("An error occurred: " + e.getMessage());
		}
	}
}
