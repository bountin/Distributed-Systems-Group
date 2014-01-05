package server;

import java.io.File;

import util.Config;
import util.MyUtil;

public class FileServerConfig
{
	private File dir;
	private Integer tcpPort;
	private String proxyHost;
	private Integer proxyUdpPort;
	private long period;
	private String hmacKeyPath;

	public FileServerConfig(Config config) throws Exception
	{
		dir = MyUtil.getFile(config, "fileserver.dir");
		tcpPort = MyUtil.getPort(config, "tcp.port");
		proxyHost = MyUtil.getString(config, "proxy.host");
		proxyUdpPort = MyUtil.getPort(config, "proxy.udp.port");
		period = MyUtil.getMilliseconds(config, "fileserver.alive");
		hmacKeyPath = MyUtil.getString(config, "hmac.key");
	}

	public File getDir()
	{
		return dir;
	}

	public long getPeriod()
	{
		return period;
	}

	public String getProxyHost()
	{
		return proxyHost;
	}

	public Integer getProxyUdpPort()
	{
		return proxyUdpPort;
	}

	public Integer getTcpPort()
	{
		return tcpPort;
	}

	public void setDir(File dir)
	{
		this.dir = dir;
	}

	public String getHmacKeyPath() { return hmacKeyPath; }

	public void setPeriod(long period)
	{
		this.period = period;
	}

	public void setProxyHost(String proxyHost)
	{
		this.proxyHost = proxyHost;
	}

	public void setProxyUdpPort(Integer proxyUdpPort)
	{
		this.proxyUdpPort = proxyUdpPort;
	}

	public void setTcpPort(Integer tcpPort)
	{
		this.tcpPort = tcpPort;
	}

}
