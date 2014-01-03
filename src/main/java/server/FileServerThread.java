package server;

import java.io.IOException;
import java.net.Socket;

import message.request.DownloadFileRequest;
import message.request.DownloadForReplicationRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.MessageResponse;
import util.MyUtil;
import util.SocketThread;

public class FileServerThread extends SocketThread
{
	private FileServerManager fileServerManager;

	public FileServerThread(FileServerManager fileServerManager, Socket socket, String name) throws IOException
	{
		super(socket, name);
		this.fileServerManager = fileServerManager;
	}

	@Override
	public void run()
	{
		try
		{
			Object inRequest, outResponse = null;
			if(!socket.isClosed() && (inRequest = in.readObject()) != null)
			{
				outResponse = new MessageResponse("command \"" + inRequest.toString() + "\" not found on fileserver");

				if(inRequest instanceof DownloadFileRequest)
				{
					outResponse = fileServerManager.download((DownloadFileRequest)inRequest);
				}
				if(inRequest instanceof DownloadForReplicationRequest)
				{
					outResponse = fileServerManager.downloadForReplication((DownloadForReplicationRequest)inRequest);
				}
				if(inRequest instanceof InfoRequest)
				{
					outResponse = fileServerManager.info((InfoRequest)inRequest);
				}
				if(inRequest instanceof ListRequest)
				{
					outResponse = fileServerManager.list();
				}
				if(inRequest instanceof UploadRequest)
				{
					outResponse = fileServerManager.upload((UploadRequest)inRequest);
				}
				if(inRequest instanceof VersionRequest)
				{
					outResponse = fileServerManager.version((VersionRequest)inRequest);
				}

				out.writeObject(outResponse);
				out.flush();
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
