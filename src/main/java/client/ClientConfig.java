package client;

import java.io.File;
import java.security.PublicKey;

import util.Config;
import util.MyUtil;

public class ClientConfig
{
	private File downloadDir;
	private String proxyHost;
	private Integer proxyTcpPort;
	private File privateKeyDir;
	private PublicKey publicProxyKey;

	public ClientConfig(Config config) throws Exception
	{
		downloadDir = MyUtil.getFile(config, "download.dir");
		proxyHost = MyUtil.getString(config, "proxy.host");
		proxyTcpPort = MyUtil.getPort(config, "proxy.tcp.port");
		privateKeyDir = MyUtil.getDirectory(config, "keys.dir");
		publicProxyKey = MyUtil.getPublicKey(config, "proxy.key");
	}

	public File getDownloadDir()
	{
		return downloadDir;
	}

	public File getPrivateKeyDir()
	{
		return privateKeyDir;
	}

	public String getProxyHost()
	{
		return proxyHost;
	}

	public Integer getProxyTcpPort()
	{
		return proxyTcpPort;
	}

	public PublicKey getPublicProxyKey()
	{
		return publicProxyKey;
	}

}
