package proxy;

import java.io.Serializable;

public class FileInfo implements Serializable
{
	private static final long serialVersionUID = 2237473122054003017L;
	private String filename;
	private long size;
	private int version;
	private int downloadCounter = 0;

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

	public synchronized int getDownloadCounter() {
		return downloadCounter;
	}

	@Override
	public String toString()
	{
		return version + " " + filename;
	}

	public synchronized void increaseDownloadCounter() {
		downloadCounter++;
	}

	public synchronized void setDownloadCounter(int downloadCounter) {
		this.downloadCounter = downloadCounter;
	}
}
