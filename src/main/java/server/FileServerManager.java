package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import message.Response;
import message.request.*;
import message.response.DownloadFileResponse;
import message.response.DownloadForReplicationResponse;
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

	private FileInfo createFileInfo(File file)
	{
		return new FileInfo(file.getName(), file.length(), 0);
	}

	@Override
	public synchronized Response download(DownloadFileRequest request) throws IOException
	{
		DownloadTicket ticket = request.getTicket();
		File file = new File(fileServerConfig.getDir(), ticket.getFilename());
		if(!file.exists())
		{
			return new MessageResponse("file does not exist");
		}

		// TODO lab2 version not 0
		int version = 0;
		boolean verified = ChecksumUtils.verifyChecksum(ticket.getUsername(), file, version, ticket.getChecksum());
		if(!verified)
		{
			return new MessageResponse("unvalid download ticket ");
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
			return new MessageResponse("error reading file on fileserver: " + e.getMessage());
		}
		return new DownloadFileResponse(ticket, content);
	}

	public synchronized Response downloadForReplication(DownloadForReplicationRequest request) throws IOException
	{
		File file = new File(fileServerConfig.getDir(), request.getFilename());
		if(!file.exists())
		{
			return new MessageResponse("file does not exist");
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
			return new MessageResponse("error reading file on fileserver: " + e.getMessage());
		}
		return new DownloadForReplicationResponse(request.getFilename(), content);
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
			files.put(file.getName(), createFileInfo(file));
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

			FileInfo fileInfo = createFileInfo(uploadFile);
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
		FileInfo fileInfo = files.get(request.getFilename());
		if(fileInfo == null)
		{
			return new MessageResponse("file not found on server");
		}
		return new VersionResponse(fileInfo.getFilename(), fileInfo.getVersion());
	}
}
