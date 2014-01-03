package client;

import java.io.File;

import util.Config;
import util.MyUtil;

public class ClientConfig
{
	private File downloadDir;
	private String proxyHost;
	private Integer proxyTcpPort;

	public ClientConfig(Config config) throws Exception
	{
		downloadDir = MyUtil.getFile(config, "download.dir");
		proxyHost = MyUtil.getString(config, "proxy.host");
		proxyTcpPort = MyUtil.getPort(config, "proxy.tcp.port");
	}

	public File getDownloadDir()
	{
		return downloadDir;
	}

	public String getProxyHost()
	{
		return proxyHost;
	}

	public Integer getProxyTcpPort()
	{
		return proxyTcpPort;
	}

	public void setDownloadDir(File downloadDir)
	{
		this.downloadDir = downloadDir;
	}

	public void setProxyHost(String proxyHost)
	{
		this.proxyHost = proxyHost;
	}

	public void setProxyTcpPort(Integer proxyTcpPort)
	{
		this.proxyTcpPort = proxyTcpPort;
	}

}
