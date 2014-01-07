package server;

import java.io.File;

public class FileContentVersion
{
	private final File file;
	private final byte[] content;
	private final int version;

	public FileContentVersion(File file, byte[] content, int version)
	{
		this.file = file;
		this.content = content;
		this.version = version;
	}

	public byte[] getContent()
	{
		return content;
	}

	public File getFile()
	{
		return file;
	}

	public int getVersion()
	{
		return version;
	}

}
