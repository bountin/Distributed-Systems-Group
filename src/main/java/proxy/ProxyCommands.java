package proxy;

import java.util.ArrayList;
import java.util.List;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import model.FileServerInfo;
import model.UserInfo;
import objects.User;
import server.FileServerData;
import cli.Command;

public abstract class ProxyCommands implements IProxyCli
{
	private ProxyInfo proxyInfo = ProxyInfo.getInstance();

	@Override
	@Command
	public MessageResponse exit()
	{
		shutdown();
		return new MessageResponse("proxy exit");
	}

	@Override
	@Command
	public Response fileservers()
	{
		List<FileServerInfo> fileServerInfos = new ArrayList<FileServerInfo>();
		for(FileServerData data : proxyInfo.getFileServerData().values())
		{
			fileServerInfos.add(new FileServerInfo(data.getNetworkId().getAddress(), data.getNetworkId().getPort(), data.getUsage(), data.isOnline()));
		}
		FileServerInfoResponse fileServerInfoResponse = new FileServerInfoResponse(fileServerInfos);
		return fileServerInfoResponse;
	}

	public abstract void shutdown();

	@Override
	@Command
	public Response users()
	{
		List<UserInfo> userInfo = new ArrayList<UserInfo>();
		for(User user : proxyInfo.getUsers().values())
		{
			userInfo.add(new UserInfo(user.getUsername(), user.getCredits(), user.isOnline()));
		}
		UserInfoResponse fileServerInfoResponse = new UserInfoResponse(userInfo);
		return fileServerInfoResponse;
	}
}
