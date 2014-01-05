package server;

import java.io.IOException;
import java.net.Socket;

import message.request.*;
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
			Object inRequest, outResponse;
			if(!socket.isClosed() && (inRequest = in.readObject()) != null)
			{
				if (inRequest instanceof DownloadFileRequest) {
					outResponse = fileServerManager.download((DownloadFileRequest)inRequest);
				} else if (inRequest instanceof DownloadForReplicationRequest) {
					outResponse = fileServerManager.downloadForReplication((DownloadForReplicationRequest)inRequest);
				} else if (inRequest instanceof InfoRequest) {
					outResponse = fileServerManager.info((InfoRequest)inRequest);
				} else if(inRequest instanceof ListRequest) {
					outResponse = fileServerManager.list();
				} else if(inRequest instanceof UploadRequest) {
					outResponse = fileServerManager.upload((UploadRequest)inRequest);
				} else if(inRequest instanceof VersionRequest) {
					outResponse = fileServerManager.version((VersionRequest)inRequest);
				} else {
					outResponse = new MessageResponse("Request \"" + inRequest.getClass().getName() + "\" is not supported by this fileserver");
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
