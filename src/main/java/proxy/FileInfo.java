package proxy;

public class FileInfo
{
	private String filename;
	private long size;
	private int version;

	public FileInfo(String filename, long size, int version)
	{
		this.filename = filename;
		this.size = size;
		this.version = version;
	}

	public String getFilename()
	{
		return filename;
	}

	public long getSize()
	{
		return size;
	}

	public int getVersion()
	{
		return version;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

}
