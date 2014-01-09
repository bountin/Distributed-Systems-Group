package proxy;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

import model.DirectoryKeyHolder;
import model.KeyHolder;
import util.Config;
import util.EncryptionUtil;
import util.MyUtil;

public class ProxyConfig
{
	private Integer tcpPort;
	private Integer udpPort;
	private Long timeout;
	private Long checkPeriod;
	private final PublicKey publicKey;
	private PrivateKey privateKey;
	private KeyHolder userPublicKeys;
	private String hmacKeyPath;

	public ProxyConfig(Config config, String password) throws Exception
	{
		this.tcpPort = MyUtil.getPort(config, "tcp.port");
		this.udpPort = MyUtil.getPort(config, "udp.port");
		this.timeout = MyUtil.getMilliseconds(config, "fileserver.timeout");
		this.checkPeriod = MyUtil.getMilliseconds(config, "fileserver.checkPeriod");
		this.privateKey = MyUtil.getPrivateKey(config, "key", password);
		this.userPublicKeys = new DirectoryKeyHolder(MyUtil.getDirectory(config, "keys.dir"));
		this.hmacKeyPath = MyUtil.getString(config, "hmac.key");
		String privateKey = MyUtil.getString(config, "key");
		String publicKeyPath = privateKey.replace(".pem", ".pub.pem");
		this.publicKey = EncryptionUtil.getPublicKeyFromFile(new File(publicKeyPath));
	}

	public ProxyConfig(Integer tcpPort, Integer udpPort, Long timeout, Long checkPeriod, PublicKey publicKey, PrivateKey privateKey, KeyHolder userPublicKeys, String hmacKeyPath)
	{
		super();
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.timeout = timeout;
		this.checkPeriod = checkPeriod;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.userPublicKeys = userPublicKeys;
		this.hmacKeyPath = hmacKeyPath;
	}

	public Long getCheckPeriod()
	{
		return checkPeriod;
	}

	public String getHmacKeyPath()
	{
		return hmacKeyPath;
	}

	public PrivateKey getPrivateKey()
	{
		return privateKey;
	}

	public PublicKey getPublicKey()
	{
		return publicKey;
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

	public KeyHolder getUserPublicKeys()
	{
		return userPublicKeys;
	}
}
