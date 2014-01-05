package proxy;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import message.Response;
import message.request.DownloadForReplicationRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.response.DownloadForReplicationResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import objects.User;
import server.FileServerData;
import server.NetworkId;
import util.MyUtil;

public class ProxyInfo
{
	private Map<String, User> users = Collections.synchronizedMap(new HashMap<String, User>());
	private Map<NetworkId, FileServerData> fileServerData = Collections.synchronizedMap(new HashMap<NetworkId, FileServerData>());
	private Map<String, FileInfo> files = Collections.synchronizedMap(new HashMap<String, FileInfo>());

	private static ProxyInfo instance;
	private String hmacKeyPath;

	public synchronized static ProxyInfo getInstance()
	{
		if(instance == null)
		{
			instance = new ProxyInfo();
		}
		return instance;
	}

	private ProxyInfo()
	{
		readUsers();
	}

	public void setHmacKeyPath(String hmacKeyPath) {
		this.hmacKeyPath = hmacKeyPath;
	}

	public synchronized void addFile(FileInfo toAdd)
	{
		FileInfo fileInfo = files.get(toAdd.getFilename());
		if(fileInfo != null)
		{
			// if newer version is uploaded
			if(fileInfo.getVersion() < toAdd.getVersion())
			{
				files.put(toAdd.getFilename(), toAdd);
			}
		}
		else
		{
			files.put(toAdd.getFilename(), toAdd);
		}
	}

	public synchronized void decreaseCredits(User user, long filesize)
	{
		User u = users.get(user.getUsername());
		if(u != null)
		{
			u.setCredits(u.getCredits() - filesize);
		}

	}

	private synchronized void downloadNewFiles(Set<String> filesToDownload, FileServerData data) throws Exception
	{
		for(String filename : filesToDownload)
		{
			// download from server with lowest usage
			DownloadForReplicationRequest request = new DownloadForReplicationRequest(filename);
			NetworkId networkId = getFileServerWithLowestUsage().getNetworkId();
			Response response = MyUtil.sendRequest(request, networkId, "download for replication failed");
			if(response.getClass().equals(MessageResponse.class))
			{
				throw new Exception(((MessageResponse)response).getMessage());
			}
			DownloadForReplicationResponse downloadResponse = (DownloadForReplicationResponse)response;

			// upload to server without file
			// TODO lab2 version not 0
			int version = 0;
			UploadRequest uploadRequest = new UploadRequest(downloadResponse.getFilename(), version, downloadResponse.getContent());
			Set<NetworkId> fileServerIds = getOnlineFileServerIds();
			fileServerIds.add(data.getNetworkId());
			sendUploadRequestToServers(uploadRequest, fileServerIds);
		}

	}

	public synchronized long getCredits(User user) throws Exception
	{
		User u = users.get(user.getUsername());
		if(u != null)
		{
			return u.getCredits();
		}
		else
		{
			throw new Exception("user not found");
		}

	}

	public synchronized Map<String, FileInfo> getFiles()
	{
		return files;
	}

	public synchronized Map<NetworkId, FileServerData> getFileServerData()
	{
		return fileServerData;
	}

	public synchronized FileServerData getFileServerWithLowestUsage()
	{
		Long minUsage = null;
		FileServerData minUsageFileServer = null;
		for(FileServerData data : fileServerData.values())
		{
			if(!data.isOnline())
			{
				continue;
			}
			if(minUsage == null)
			{
				minUsage = data.getUsage();
				minUsageFileServer = data;
			}
			else
			{
				if(data.getUsage() < minUsage)
				{
					minUsage = data.getUsage();
					minUsageFileServer = data;
				}
			}
		}
		return minUsageFileServer;
	}

	public synchronized long getFileSize(String filename) throws Exception
	{
		FileInfo fileInfo = files.get(filename);
		if(fileInfo != null)
		{
			return fileInfo.getSize();
		}
		else
		{
			// check sending empty file
			throw new FileNotFoundException("no file \"" + filename + "\" available");
		}
	}

	public synchronized Set<NetworkId> getOnlineFileServerIds()
	{
		Set<NetworkId> onlineFileServers = new HashSet<NetworkId>();
		for(FileServerData data : fileServerData.values())
		{
			if(data.isOnline())
			{
				onlineFileServers.add(data.getNetworkId());
			}

		}
		return onlineFileServers;
	}

	public synchronized Map<String, User> getUsers()
	{
		return users;
	}

	public synchronized Long increaseCredits(User user, long credits)
	{
		User u = users.get(user.getUsername());
		if(u != null)
		{
			u.setCredits(u.getCredits() + credits);
			return u.getCredits();
		}
		return null;
	}

	private synchronized void readUsers()
	{
		ResourceBundle resourceBundle = ResourceBundle.getBundle("user");
		if(resourceBundle != null)
		{
			try
			{
				Enumeration<String> e = resourceBundle.getKeys();
				while(e.hasMoreElements())
				{
					String s = e.nextElement();
					int pointIndex = s.indexOf(".");
					String username = s.substring(0, pointIndex);
					String property = s.substring(pointIndex + 1);
					User user = users.get(username);
					if(user == null)
					{
						user = new User();
					}
					if(property.equals("credits"))
					{
						user.setCredits(new Long(resourceBundle.getString(s)));
					}
					if(property.equals("password"))
					{
						user.setPassword(resourceBundle.getString(s));
					}
					user.setUsername(username);
					users.put(user.getUsername(), user);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("Properties file not found!");
		}
	}

	private synchronized Response sendListRequest(FileServerData data)
	{
		Response response = MyUtil.sendRequest(new ListRequest(), data.getNetworkId(), "error sending list request");
		if(response.getClass().equals(MessageResponse.class))
		{
			return response;
		}
		ListResponse listResponse = (ListResponse)response;
		Set<String> filesOnFileServer = listResponse.getFileNames();
		if(!filesOnFileServer.equals(files.keySet()))
		{
			try
			{
				// FALL 1 mehr files am fileserver d
				Set<String> additionalFilesToUpload = new HashSet<String>(filesOnFileServer);
				additionalFilesToUpload.removeAll(files.keySet());
				uploadNewFiles(additionalFilesToUpload, data);
				// FALL 2 weniger files am fileserver d
				Set<String> filesToDownload = new HashSet<String>(files.keySet());
				filesToDownload.remove(filesOnFileServer);
				downloadNewFiles(filesToDownload, data);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return new MessageResponse("success");
	}

	public synchronized MessageResponse sendUploadRequestToServers(UploadRequest request, Set<NetworkId> networkIds)
	{
		if(networkIds.size() == 0)
		{
			return new MessageResponse("no fileserver available");
		}
		for(NetworkId networkId : networkIds)
		{
			MessageResponse m = (MessageResponse)MyUtil.sendRequest(request, networkId, "error sending uploadRequest to " + networkId.getAddress() + ":" + networkId.getPort());
			if(!m.getMessage().contains("success"))
			{
				return new MessageResponse(networkId + ": " + m.getMessage());
			}
		}
		return new MessageResponse("success");
	}

	public synchronized void setOffline(NetworkId networkId)
	{
		FileServerData data = fileServerData.get(networkId);
		if(data != null)
		{
			data.setOnline(false);
		}
	}

	public synchronized void setOnline(NetworkId networkId)
	{
		FileServerData data = fileServerData.get(networkId);
		boolean sendListRequest = false;

		if(data != null)
		{
			if(!data.isOnline())
			{
				// when was offline
				sendListRequest = true;
			}
			data.setOnline(true);
		}
		else
		{
			data = new FileServerData(networkId);
			data.setOnline(true);
			fileServerData.put(data.getNetworkId(), data);
			sendListRequest = true;
		}
		if(sendListRequest)
		{
			sendListRequest(data);
		}
	}

	/**
	 * @param additionalFilesToUpload
	 *            files to upload
	 * @param data
	 *            fileserver with new files to upload on other servers
	 * @return
	 * @throws Exception
	 */
	private synchronized void uploadNewFiles(Set<String> additionalFilesToUpload, FileServerData data) throws Exception
	{
		for(String filename : additionalFilesToUpload)
		{
			// download from server with file
			DownloadForReplicationRequest request = new DownloadForReplicationRequest(filename);
			Response response = MyUtil.sendRequest(request, data.getNetworkId(), "download for replication failed");
			if(response.getClass().equals(MessageResponse.class))
			{
				throw new Exception(((MessageResponse)response).getMessage());
			}
			DownloadForReplicationResponse downloadResponse = (DownloadForReplicationResponse)response;

			// TODO lab2 version not 0
			int version = 0;
			addFile(new FileInfo(downloadResponse.getFilename(), downloadResponse.getContent().length, version));

			// upload to all other servers
			UploadRequest uploadRequest = new UploadRequest(downloadResponse.getFilename(), version, downloadResponse.getContent());
			Set<NetworkId> fileServersIds = getOnlineFileServerIds();
			fileServersIds.remove(data.getNetworkId()); // not sending to server which already have it
			sendUploadRequestToServers(uploadRequest, fileServersIds);
		}

	}
}
