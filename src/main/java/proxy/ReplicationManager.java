package proxy;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import message.MessageResponseException;
import message.Response;
import message.request.FileInfoListRequest;
import message.request.HMACRequest;
import message.request.VersionRequest;
import message.response.FileInfoListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import server.FileServerData;
import util.HMACException;
import util.MyUtil;

public class ReplicationManager
{
	private ProxyInfo proxyInfo;

	private int numberWriteQuorum;
	private int numberReadQuorum;
	private int numberFileServers;
	private List<FileServerData> readQuorumServers;

	public ReplicationManager(ProxyInfo proxyInfo)
	{
		this.proxyInfo = proxyInfo;

		initializeNumbers();
	}

	/**
	 * Seeks highest version of file with given filename and server with lowest usage which have this file version. Sets highestVersion and minUsageFileServer object.
	 * 
	 * @param filename
	 * @param highestVersion
	 *            gets returned
	 * @param minUsageFileServer
	 *            gets returned
	 * @return
	 * @return
	 * @throws Exception
	 */
	public synchronized VersionFileServerData getLowestReadQuorumWithHighestVersion(String filename) throws Exception
	{
		Integer highestVersion = null;
		FileServerData minUsageFileServer = null;

		List<FileServerData> readQuorumFileServers = getReadQuorumServers();
		// TODO check ob richtig formuliert
		if(readQuorumFileServers == null || readQuorumFileServers.size() == 0)
		{
			throw new MessageResponseException("no fileserver available");
		}

		for(FileServerData fileServerData : readQuorumFileServers)
		{
			HMACRequest<VersionRequest> versionRequest = null;
			try
			{
				versionRequest = new HMACRequest<VersionRequest>(new VersionRequest(filename), proxyInfo.getHmacKeyPath());
			}
			catch(HMACException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			VersionResponse versionResponse = null;
			try
			{
				Response response = MyUtil.sendRequest(versionRequest, fileServerData.getNetworkId(), proxyInfo.getHmacKeyPath());
				if(response instanceof MessageResponse)
				{
					String message = ((MessageResponse)response).getMessage();
					if(message.equals("file not found on server"))
					{
						continue;
					}
					throw new MessageResponseException(message);
				}
				versionResponse = (VersionResponse)response;
			}
			catch(Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(highestVersion == null)
			{
				highestVersion = versionResponse.getVersion();
				minUsageFileServer = fileServerData;
			}
			else
			{
				if(highestVersion < versionResponse.getVersion())
				{
					highestVersion = versionResponse.getVersion();
					minUsageFileServer = fileServerData;
				}
				// System.err.println(versionResponse + " " + fileServerData + " " + minUsageFileServer);
				if(highestVersion == versionResponse.getVersion() && fileServerData.getUsage() < minUsageFileServer.getUsage())
				{
					minUsageFileServer = fileServerData;
				}
			}
		}
		if(minUsageFileServer == null || highestVersion == null)
		{
			throw new FileNotFoundException("no version of file " + filename + " found");
		}
		return new VersionFileServerData(minUsageFileServer, highestVersion);
	}

	public synchronized List<FileServerData> getNServers(int number)
	{
		List<FileServerData> allFileServers = new ArrayList<FileServerData>(ProxyInfo.getInstance().getFileServerData().values());

		Collections.sort(allFileServers);

		return allFileServers.subList(0, number);
	}

	public synchronized int getReadQuorum()
	{
		return numberReadQuorum;
	}

	public synchronized List<FileServerData> getReadQuorumServers()
	{
		List<FileServerData> readQuorumServersBefore = readQuorumServers;
		readQuorumServers = getNServers(numberReadQuorum);

		if(readQuorumServersBefore != null)
		{
			// calculate servers which not any more are read quorum servers
			List<FileServerData> copyFromReadQuorumServer = new ArrayList<FileServerData>(readQuorumServersBefore);
			copyFromReadQuorumServer.removeAll(readQuorumServers);

			// files from this servers has to be uploaded on new read quorum servers
			for(FileServerData fileServerData : copyFromReadQuorumServer)
			{
				try
				{
					synchronizeFilesOnFileServer(fileServerData, readQuorumServersBefore, readQuorumServers);
				}
				catch(Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return readQuorumServers;
	}

	public synchronized int getWriteQuorum()
	{
		return numberWriteQuorum;
	}

	public synchronized List<FileServerData> getWriteQuorumServers()
	{
		return getNServers(numberWriteQuorum);
	}

	public synchronized void initializeNumbers()
	{
		numberFileServers = proxyInfo.getFileServerData().size();
		numberWriteQuorum = numberFileServers / 2 + 1;
		numberReadQuorum = numberFileServers - numberWriteQuorum + 1;
		// System.out.println(this);
	}

	public synchronized boolean isInReadQuorum(FileServerData data)
	{
		return getReadQuorumServers().contains(data);
	}

	private synchronized Response synchronizeFilesOnFileServer(FileServerData data, List<FileServerData> oldReadQuorumServers, List<FileServerData> newReadQuorumServers) throws Exception
	{
		HMACRequest<FileInfoListRequest> request = new HMACRequest<FileInfoListRequest>(new FileInfoListRequest(), proxyInfo.getHmacKeyPath());
		Response response = MyUtil.sendRequest(request, data.getNetworkId(), proxyInfo.getHmacKeyPath());
		if(response instanceof MessageResponse)
		{
			throw new Exception(((MessageResponse)response).getMessage());
		}
		FileInfoListResponse fileInfoListResponse = (FileInfoListResponse)response;
		List<FileInfo> filesOnFileServer = fileInfoListResponse.getFileInfos();
		Set<String> filesToUpload = new HashSet<String>();
		Map<String, FileInfo> filesAlreadyOnOtherServers = proxyInfo.getMergedListRequest(newReadQuorumServers);

		for(FileInfo fileInfo : filesOnFileServer)
		{
			if(proxyInfo.isFileInfoNewerThanInMap(fileInfo, filesAlreadyOnOtherServers))
			{
				filesToUpload.add(fileInfo.getFilename());
			}

		}
		proxyInfo.uploadFilesFromServer(filesToUpload, data, newReadQuorumServers);

		return new MessageResponse("success");
	}

	@Override
	public synchronized String toString()
	{
		return numberReadQuorum + "+" + numberWriteQuorum + ">" + numberFileServers + "\n" + numberWriteQuorum + ">" + numberFileServers + "/2";
	}
}
