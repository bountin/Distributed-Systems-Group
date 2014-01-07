package proxy;

import server.FileServerData;

public class VersionFileServerData
{
	private FileServerData fileServerData;
	private int version;

	public VersionFileServerData(FileServerData fileServerData, int version)
	{
		this.fileServerData = fileServerData;
		this.version = version;
	}

	public FileServerData getFileServerData()
	{
		return fileServerData;
	}

	public int getVersion()
	{
		return version;
	}
}
