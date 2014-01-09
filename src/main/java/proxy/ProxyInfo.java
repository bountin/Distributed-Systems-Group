package proxy;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import message.MessageResponseException;
import message.Response;
import message.request.DownloadForReplicationRequest;
import message.request.FileInfoListRequest;
import message.request.HMACRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.DownloadForReplicationResponse;
import message.response.FileInfoListResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.IRmiClientData;
import model.KeyHolder;
import objects.User;
import server.FileServerData;
import server.NetworkId;
import util.MyUtil;

public class ProxyInfo
{
	private Map<String, User> users = Collections.synchronizedMap(new HashMap<String, User>());
	private Map<NetworkId, FileServerData> fileServerData = Collections.synchronizedMap(new HashMap<NetworkId, FileServerData>());
	private Map<String, FileInfo> files = Collections.synchronizedMap(new HashMap<String, FileInfo>());
	private Set<IRmiClientData> downloadSubscriptions = Collections.synchronizedSet(new HashSet<IRmiClientData>());
	private ReplicationManager replicationInfo;

	private static ProxyInfo instance;
	private String hmacKeyPath;
	private KeyHolder userKeyHolder;

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

	public synchronized void addFile(FileInfo filenInfo)
	{
		files.put(filenInfo.getFilename(), filenInfo);
	}

	public synchronized void addSubscription(IRmiClientData data) {
		downloadSubscriptions.add(data);
	}

	public void pruneSubscriptions(User user) {
		Set<IRmiClientData> toRemove = new HashSet<IRmiClientData>();

		for(IRmiClientData subscription: downloadSubscriptions) {
			try {
				if (subscription.getUser().equals(user.getUsername())) {
					toRemove.add(subscription);
				}
			} catch (RemoteException e) {
				toRemove.add(subscription);
			}
		}

		for (IRmiClientData subscription: toRemove) {
			downloadSubscriptions.remove(subscription);
			subscription.unregister();
		}
	}

	public synchronized void increaseFileDownloadCounter(String filename) {
		files.get(filename).increaseDownloadCounter();
		int count = files.get(filename).getDownloadCounter();

		Set<IRmiClientData> toRemove = new HashSet<IRmiClientData>();

		for(IRmiClientData subscription: downloadSubscriptions) {
			try {
				if (subscription.test(filename, count)) {
					subscription.notifyDownloadSubscription();
					toRemove.add(subscription);
				}
			} catch (RemoteException e) {
				toRemove.add(subscription);
			}
		}

		for (IRmiClientData subscription: toRemove) {
			downloadSubscriptions.remove(subscription);
			subscription.unregister();
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

	public void decreaseUsage(NetworkId networkId, long filesize)
	{
		FileServerData f = fileServerData.get(networkId);
		if(f != null)
		{
			f.setUsage(f.getUsage() - filesize);
		}

	}

	private synchronized void downloadFilesToStartUpServer(Set<String> filesToDownload, FileServerData data) throws Exception
	{
		// TODO version from readquroum upload to writequorum or only new server?
		for(String filename : filesToDownload)
		{
			// download from server with lowest usage
			HMACRequest<DownloadForReplicationRequest> request = new HMACRequest<DownloadForReplicationRequest>(new DownloadForReplicationRequest(filename), hmacKeyPath);
			// minUsageFileServer with newest version of file
			FileServerData minUsageFileServer = replicationInfo.getLowestReadQuorumWithHighestVersion(filename).getFileServerData();

			Response response = MyUtil.sendRequest(request, minUsageFileServer.getNetworkId(), getHmacKeyPath());
			if(response instanceof MessageResponse)
			{
				throw new Exception("error downloading file for replication\nfile: " + filename + "\nfileserver: " + data + "\ncause: " + ((MessageResponse)response).getMessage());
			}
			DownloadForReplicationResponse downloadResponse = (DownloadForReplicationResponse)response;

			// upload to server without file
			UploadRequest uploadRequest = new UploadRequest(downloadResponse.getFilename(), downloadResponse.getVersion(), downloadResponse.getContent());
			HMACRequest<UploadRequest> hmacUploadRequest = new HMACRequest<UploadRequest>(uploadRequest, hmacKeyPath);
			List<FileServerData> fileServerData = getWriteQuorumServers();
			// fileServerData.add(data);
			sendUploadRequestToServers(hmacUploadRequest, fileServerData);
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

	public String getHmacKeyPath()
	{
		return hmacKeyPath;
	}

	public Map<String, FileInfo> getMergedListRequest(List<FileServerData> newReadQuorumServers)
	{
		Map<String, FileInfo> mergedFileInfos = new HashMap<String, FileInfo>();
		for(FileServerData fileServerData : newReadQuorumServers)
		{
			try
			{
				HMACRequest<FileInfoListRequest> request = new HMACRequest<FileInfoListRequest>(new FileInfoListRequest(), hmacKeyPath);
				Response response = MyUtil.sendRequest(request, fileServerData.getNetworkId(), getHmacKeyPath());

				if(response instanceof MessageResponse)
				{
					continue;
				}
				FileInfoListResponse fileInfoListResponse = (FileInfoListResponse)response;
				for(FileInfo fileInfo : fileInfoListResponse.getFileInfos())
				{
					if(isFileInfoNewerThanInMap(fileInfo, mergedFileInfos))
					{
						mergedFileInfos.put(fileInfo.getFilename(), fileInfo);
					}
				}
			}
			catch(Exception e)
			{
				// do nothing
				e.printStackTrace();
			}
		}
		return mergedFileInfos;
	}

	public int getNextFileVersion(String filename) throws Exception
	{
		List<FileServerData> fileServers = getReplicationInfo().getReadQuorumServers();
		int version = -1;
		for(FileServerData fileServerData : fileServers)
		{
			HMACRequest<VersionRequest> versionRequest = new HMACRequest<VersionRequest>(new VersionRequest(filename), hmacKeyPath);
			Response response = MyUtil.sendRequest(versionRequest, fileServerData.getNetworkId(), getHmacKeyPath());
			if(response instanceof MessageResponse)
			{
				if(((MessageResponse)response).getMessage().equals("file not found on server"))
				{
					continue;
				}
				throw new Exception(((MessageResponse)response).getMessage());
			}
			VersionResponse versionResponse = (VersionResponse)response;

			version = Math.max(version, versionResponse.getVersion());
		}
		return version + 1;
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

	public List<FileServerData> getReadQuorumServers()
	{
		return getReplicationInfo().getReadQuorumServers();
	}

	public ReplicationManager getReplicationInfo()
	{
		if(replicationInfo == null)
		{
			replicationInfo = new ReplicationManager(this);
		}
		return replicationInfo;
	}

	public synchronized Map<String, User> getUsers()
	{
		return users;
	}

	public List<FileServerData> getWriteQuorumServers()
	{
		return getReplicationInfo().getWriteQuorumServers();
	}

	public synchronized void increaseCredits(User user, long credits)
	{
		User u = users.get(user.getUsername());
		if(u != null)
		{
			u.setCredits(u.getCredits() + credits);
		}
	}

	public void increaseUsage(FileServerData fileServer, long filesize)
	{
		FileServerData f = fileServerData.get(fileServer.getNetworkId());
		if(f != null)
		{
			f.setUsage(f.getUsage() + filesize);
		}
	}

	public boolean isFileInfoNewerThanInMap(FileInfo info, Map<String, FileInfo> filesAlreadyOnOtherServers)
	{
		FileInfo f = filesAlreadyOnOtherServers.get(info.getFilename());
		// file exists on other servers
		if(f != null)
		{
			// only upload newer version than on other servers
			if(info.getVersion() > f.getVersion())
			{
				return true;
			}
			return false;
		}
		else
		{
			return true;
		}
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

	public synchronized MessageResponse sendUploadRequestToServers(HMACRequest<UploadRequest> request, List<FileServerData> fileServers)
	{
		if(fileServers.size() == 0)
		{
			return new MessageResponse("no fileserver available");
		}

		for(FileServerData data : fileServers)
		{
			try
			{
				MyUtil.sendRequest(request, data.getNetworkId(), getHmacKeyPath());
			}
			catch(Exception e)
			{
				return new MessageResponse("error sending uploadRequest to " + data.getNetworkId().getAddress() + ":" + data.getNetworkId().getPort() + "\ncause: " + e.getMessage());
			}
		}
		return new MessageResponse("success");
	}

	public void setHmacKeyPath(String hmacKeyPath)
	{
		this.hmacKeyPath = hmacKeyPath;
	}

	public synchronized void setOffline(NetworkId networkId)
	{
		FileServerData data = fileServerData.get(networkId);
		if(data != null)
		{
			data.setOnline(false);
			getReplicationInfo().initializeNumbers();
		}
	}

	public synchronized void setOnline(NetworkId networkId)
	{
		FileServerData data = fileServerData.get(networkId);
		boolean wasOfflineBefore = false;

		if(data != null)
		{
			if(!data.isOnline())
			{
				// when was offline
				wasOfflineBefore = true;
			}
			data.setOnline(true);
		}
		else
		{
			data = new FileServerData(networkId);
			data.setOnline(true);
			fileServerData.put(data.getNetworkId(), data);
			wasOfflineBefore = true;
		}
		if(wasOfflineBefore)
		{
			getReplicationInfo().initializeNumbers();
			synchronizeFilesOnFileServer(data);
		}
	}

	private synchronized Response synchronizeFilesOnFileServer(FileServerData data)
	{
		try
		{
			Response response = MyUtil.sendRequest(new HMACRequest<ListRequest>(new ListRequest(), hmacKeyPath), data.getNetworkId(), getHmacKeyPath());
			if(response instanceof MessageResponse)
			{
				throw new Exception(((MessageResponse)response).getMessage());
			}

			ListResponse listResponse = (ListResponse)response;
			Set<String> filesOnFileServer = listResponse.getFileNames();

			// CASE 1 upload files that not exist on any other fileserver
			// no need to check versions because file versions on data always 0 at beginning
			// when any version exists on another fileserver, version always >= 0
			// upload only on read quorumServers
			Set<String> filesToUpload = new HashSet<String>(filesOnFileServer);
			filesToUpload.removeAll(files.keySet());
			uploadFilesFromServer(filesToUpload, data, getReadQuorumServers());
			// CASE 2 files to download on fileserver data
			// fileserver only has to download files when in readQuorum
			if(getReplicationInfo().isInReadQuorum(data))
			{
				Set<String> filesToDownload = new HashSet<String>(files.keySet());
				filesToDownload.remove(filesOnFileServer);
				downloadFilesToStartUpServer(filesToDownload, data);
			}
		}
		catch(MessageResponseException e)
		{
			return new MessageResponse(e.getMessage());
		}
		catch(Exception e)
		{
			return new MessageResponse("error synchronizing files\ncause: " + e.getMessage());
		}

		return new MessageResponse("success");
	}

	/**
	 * @param filesToUpload
	 * @param fromServer
	 *            fileserver with new files to upload on other servers
	 * @return
	 * @throws Exception
	 */
	public synchronized void uploadFilesFromServer(Set<String> filesToUpload, FileServerData fromServer, List<FileServerData> toServers) throws Exception
	{
		for(String filename : filesToUpload)
		{
			// download from server with file
			HMACRequest<DownloadForReplicationRequest> request = new HMACRequest<DownloadForReplicationRequest>(new DownloadForReplicationRequest(filename), hmacKeyPath);
			Response response = MyUtil.sendRequest(request, fromServer.getNetworkId(), getHmacKeyPath());
			DownloadForReplicationResponse downloadResponse = (DownloadForReplicationResponse)response;

			addFile(new FileInfo(downloadResponse.getFilename(), downloadResponse.getContent().length, downloadResponse.getVersion()));

			// upload to all other servers
			UploadRequest uploadRequest = new UploadRequest(downloadResponse.getFilename(), downloadResponse.getVersion(), downloadResponse.getContent());
			HMACRequest<UploadRequest> hmacUploadRequest = new HMACRequest<UploadRequest>(uploadRequest, hmacKeyPath);
			toServers.remove(fromServer); // not sending to server from which downloaded
			sendUploadRequestToServers(hmacUploadRequest, toServers);
		}

	}

	public void setUserKeyHolder(KeyHolder userKeyHolder) {
		this.userKeyHolder = userKeyHolder;
	}

	public KeyHolder getUserKeyHolder() {
		return userKeyHolder;
	}
}
