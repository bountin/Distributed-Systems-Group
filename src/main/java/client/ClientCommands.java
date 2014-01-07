package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

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
import util.MyUtil;
import auth.ClientAuthenticator;
import cli.Command;

public abstract class ClientCommands extends ResponseUtil implements IClientCli
{
	protected ClientConfig clientConfig;
	protected Socket proxySocket;

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
		}
	}

	public abstract void shutdown();

	/* !upload <filename> */
	@Override
	@Command
	public MessageResponse upload(String filename)
	{
		File file = new File(clientConfig.getDownloadDir(), filename);
		if(!file.exists())
		{
			return new MessageResponse("file not in downloadDir " + clientConfig.getDownloadDir());
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
}
