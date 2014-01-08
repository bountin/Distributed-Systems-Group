package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;

import model.DirectoryKeyHolder;
import model.KeyHolder;

import org.bouncycastle.openssl.PEMWriter;

import util.Config;
import util.MyUtil;
import util.UnvalidConfigException;

public class ClientConfig
{
	private File downloadDir;
	private String proxyHost;
	private Integer proxyTcpPort;
	private KeyHolder privateKeyDir;
	private PublicKey publicProxyKey;
	private File publicProxyKeyPath;

	public ClientConfig(Config config) throws Exception
	{
		downloadDir = MyUtil.getFile(config, "download.dir");
		proxyHost = MyUtil.getString(config, "proxy.host");
		proxyTcpPort = MyUtil.getPort(config, "proxy.tcp.port");
		privateKeyDir = new DirectoryKeyHolder(MyUtil.getDirectory(config, "keys.dir"));
		publicProxyKeyPath = new File(MyUtil.getString(config, "proxy.key"));
		try
		{
			publicProxyKey = MyUtil.getPublicKey(config, "proxy.key");
		}
		catch(UnvalidConfigException ignored)
		{}
	}

	public ClientConfig(File downloadDir, String proxyHost, Integer proxyTcpPort, KeyHolder privateKeyDir, PublicKey publicProxyKey)
	{
		this.downloadDir = downloadDir;
		this.proxyHost = proxyHost;
		this.proxyTcpPort = proxyTcpPort;
		this.privateKeyDir = privateKeyDir;
		this.publicProxyKey = publicProxyKey;
	}

	public File getDownloadDir()
	{
		return downloadDir;
	}

	public KeyHolder getPrivateKeyDir()
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
		synchronized(this)
		{
			return publicProxyKey;
		}
	}

	public void setPrivateKeyDir(KeyHolder privateKeyDir)
	{
		this.privateKeyDir = privateKeyDir;
	}

	public void setPublicProxyKey(PublicKey publicProxyKey) throws IOException
	{
		synchronized(this)
		{
			this.publicProxyKey = publicProxyKey;
			FileWriter fw = new FileWriter(this.publicProxyKeyPath);

			PEMWriter writer = new PEMWriter(fw);
			writer.writeObject(publicProxyKey);
			writer.close();
		}
	}

}
