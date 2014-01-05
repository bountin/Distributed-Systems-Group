package proxy;

import java.io.File;
import java.security.PrivateKey;

import util.Config;
import util.MyUtil;

public class ProxyConfig
{
	private Integer tcpPort;
	private Integer udpPort;
	private Long timeout;
	private Long checkPeriod;
	private PrivateKey privateKey;
	private File publicKeyDir;
	private String hmacKeyPath;

	public ProxyConfig(Config config) throws Exception
	{
		this.tcpPort = MyUtil.getPort(config, "tcp.port");
		this.udpPort = MyUtil.getPort(config, "udp.port");
		this.timeout = MyUtil.getMilliseconds(config, "fileserver.timeout");
		this.checkPeriod = MyUtil.getMilliseconds(config, "fileserver.checkPeriod");
		this.privateKey = MyUtil.getPrivateKey(config, "key", "12345");
		this.publicKeyDir = MyUtil.getDirectory(config, "keys.dir");
		this.hmacKeyPath = MyUtil.getString(config, "hmac.key");
	}

	public Long getCheckPeriod()
	{
		return checkPeriod;
	}

	public PrivateKey getPrivateKey()
	{
		return privateKey;
	}

	public File getPublicKeyDir()
	{
		return publicKeyDir;
	}

	public Integer getTcpPort()
	{
		return tcpPort;
	}

	public Long getTimeout()
	{
		return timeout;
	}

	public Integer getUdpPort()
	{
		return udpPort;
	}

	public String getHmacKeyPath() { return hmacKeyPath; }
}
