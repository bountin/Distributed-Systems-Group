package server;

import message.response.MessageResponse;
import cli.Command;


public abstract class FileServerCommands implements IFileServerCli
{

	@Override
	@Command
	public MessageResponse exit()
	{
		shutdown();
		return new MessageResponse("fileserver exit");
	}

	public abstract void shutdown();

}
