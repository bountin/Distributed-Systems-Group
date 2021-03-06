package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import message.MessageResponseException;
import message.Response;
import message.request.DownloadFileRequest;
import message.request.DownloadForReplicationRequest;
import message.request.InfoRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.DownloadFileResponse;
import message.response.DownloadForReplicationResponse;
import message.response.FileInfoListResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.DownloadTicket;
import proxy.FileInfo;
import util.ChecksumUtils;

public class FileServerManager implements IFileServer
{
	private FileServerConfig fileServerConfig;
	private Map<String, FileInfo> files = Collections.synchronizedMap(new HashMap<String, FileInfo>());

	public FileServerManager(FileServerConfig fileServerConfig)
	{
		this.fileServerConfig = fileServerConfig;
		readFiles();
	}

	@Override
	public synchronized Response download(DownloadFileRequest request) throws IOException
	{
		DownloadTicket ticket = request.getTicket();

		FileContentVersion file;
		try
		{
			file = download(ticket.getFilename());
		}
		catch(MessageResponseException e)
		{
			return new MessageResponse(e.getMessage());
		}
		boolean verified = ChecksumUtils.verifyChecksum(ticket.getUsername(), file.getFile(), file.getVersion(), ticket.getChecksum());
		if(!verified)
		{
			return new MessageResponse("unvalid download ticket ");
		}
		return new DownloadFileResponse(ticket, file.getContent());
	}

	private synchronized FileContentVersion download(String filename) throws MessageResponseException
	{
		File file = new File(fileServerConfig.getDir(), filename);
		if(!file.exists())
		{
			throw new MessageResponseException("file does not exist");
		}

		int version = getVersion(filename);

		byte[] content = new byte[new Long(file.length()).intValue()];
		try
		{
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(content);
			fileInputStream.close();
		}
		catch(Exception e)
		{
			throw new MessageResponseException("error reading file on fileserver: " + e.getMessage());
		}
		return new FileContentVersion(file, content, version);

	}

	public synchronized Response downloadForReplication(DownloadForReplicationRequest request) throws IOException
	{
		FileContentVersion file;
		try
		{
			file = download(request.getFilename());
		}
		catch(MessageResponseException e)
		{
			return new MessageResponse(e.getMessage());
		}
		return new DownloadForReplicationResponse(request.getFilename(), file.getContent(), file.getVersion());
	}

	public synchronized Response fileInfoList()
	{
		return new FileInfoListResponse(files.values());
	}

	public synchronized FileServerConfig getFileServerConfig()
	{
		return fileServerConfig;
	}

	private synchronized int getVersion(String filename) throws MessageResponseException
	{
		FileInfo fileInfo = files.get(filename);
		if(fileInfo == null)
		{
			throw new MessageResponseException("file not found on server");
		}
		return fileInfo.getVersion();
	}

	@Override
	public synchronized Response info(InfoRequest request) throws IOException
	{
		FileInfo fileInfo = files.get(request.getFilename());
		if(fileInfo == null)
		{
			return new MessageResponse("file not found on server");
		}
		return new InfoResponse(fileInfo.getFilename(), fileInfo.getSize());
	}

	@Override
	public synchronized Response list() throws IOException
	{
		return new ListResponse(files.keySet());
	}

	private void readFiles()
	{
		for(File file : fileServerConfig.getDir().listFiles())
		{
			files.put(file.getName(), new FileInfo(file.getName(), file.length(), 0));
		}
	}

	@Override
	public synchronized MessageResponse upload(UploadRequest request) throws IOException
	{
		try
		{
			File uploadFile = new File(fileServerConfig.getDir(), request.getFilename());
			FileOutputStream fos = new FileOutputStream(uploadFile);
			fos.write(request.getContent());
			fos.close();

			FileInfo fileInfo = new FileInfo(uploadFile.getName(), uploadFile.length(), request.getVersion());
			files.put(fileInfo.getFilename(), fileInfo);
		}
		catch(Exception e)
		{
			return new MessageResponse("Error writing file on upload: " + e.getMessage());
		}
		return new MessageResponse("success");
	}

	@Override
	public synchronized Response version(VersionRequest request) throws IOException
	{
		String filename = request.getFilename();
		int version;
		try
		{
			version = getVersion(filename);
		}
		catch(MessageResponseException e)
		{
			return new MessageResponse("file not found on server");
		}
		return new VersionResponse(filename, version);
	}

}
