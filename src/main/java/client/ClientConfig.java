package client;

import java.io.*;
import java.security.PublicKey;

import org.bouncycastle.openssl.PEMWriter;
import util.Config;
import util.MyUtil;
import util.UnvalidConfigException;

public class ClientConfig
{
	private File downloadDir;
	private String proxyHost;
	private Integer proxyTcpPort;
	private File privateKeyDir;
	private PublicKey publicProxyKey;
	private File publicProxyKeyPath;

	public ClientConfig(Config config) throws Exception
	{
		downloadDir = MyUtil.getFile(config, "download.dir");
		proxyHost = MyUtil.getString(config, "proxy.host");
		proxyTcpPort = MyUtil.getPort(config, "proxy.tcp.port");
		privateKeyDir = MyUtil.getDirectory(config, "keys.dir");

		publicProxyKeyPath = new File(MyUtil.getString(config,"proxy.key"));
		try {
			publicProxyKey = MyUtil.getPublicKey(config, "proxy.key");
		} catch (UnvalidConfigException ignored) {}
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
		synchronized (this) {
			return publicProxyKey;
		}
	}

	public void setPublicProxyKey(PublicKey publicProxyKey) throws IOException {
		synchronized (this) {
			this.publicProxyKey = publicProxyKey;
			FileWriter fw = new FileWriter(this.publicProxyKeyPath);

			PEMWriter writer = new PEMWriter(fw);
			writer.writeObject(publicProxyKey);
			writer.close();
		}
	}
}
