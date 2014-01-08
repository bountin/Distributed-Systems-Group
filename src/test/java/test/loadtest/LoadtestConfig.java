package test.loadtest;

import java.io.PrintStream;

import util.Config;
import util.MyUtil;
import util.UnvalidConfigException;

public class LoadtestConfig
{
	private int numberClients;
	private int uploadsPerMin;
	private int downloadsPerMin;
	private long fileSizeKB;
	private float overwriteRatio;

	public static void printUsage(PrintStream out)
	{
		out.println("only parameters allowed:");
		out.println("clients: Number of concurrent download clients");
		out.println("uploadsPerMin: Number of initiated uploads per client per minute");
		out.println("downloadsPerMin: Number of initiated downloads per client per minute");
		out.println("fileSizeKB: Size of files available for download, in KiloBytes (simply generate test files with random content)");
		out.println("overwriteRatio: average ratio of uploads which overwrite an existing file");
	}

	public LoadtestConfig(Config config) throws UnvalidConfigException
	{
		this.numberClients = config.getInt("clients");
		this.uploadsPerMin = config.getInt("uploadsPerMin");
		this.downloadsPerMin = config.getInt("downloadsPerMin");
		this.fileSizeKB = MyUtil.getLong(config, "fileSizeKB");
		this.overwriteRatio = MyUtil.getFloat(config, "overwriteRatio");
	}

	public int getDownloadsPerMin()
	{
		return downloadsPerMin;
	}

	public long getFileSizeKB()
	{
		return fileSizeKB;
	}

	public int getNumberClients()
	{
		return numberClients;
	}

	public float getOverwriteRatio()
	{
		return overwriteRatio;
	}

	public int getUploadsPerMin()
	{
		return uploadsPerMin;
	}

	public void setDownloadsPerMin(int downloadsPerMin)
	{
		this.downloadsPerMin = downloadsPerMin;
	}

	public void setFileSizeKB(long fileSizeKB)
	{
		this.fileSizeKB = fileSizeKB;
	}

	public void setNumberClients(int numberClients)
	{
		this.numberClients = numberClients;
	}

	public void setOverwriteRatio(float overwriteRatio)
	{
		this.overwriteRatio = overwriteRatio;
	}

	public void setUploadsPerMin(int uploadsPerMin)
	{
		this.uploadsPerMin = uploadsPerMin;
	}

}
