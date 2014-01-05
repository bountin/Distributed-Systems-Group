package proxy;

import util.Config;
import util.MyUtil;

public class ProxyConfig
{
	private Integer tcpPort;
	private Integer udpPort;
	private Long timeout;
	private Long checkPeriod;
	private String hmacKeyPath;

	public ProxyConfig(Config config) throws Exception
	{
		this.tcpPort = MyUtil.getPort(config, "tcp.port");
		this.udpPort = MyUtil.getPort(config, "udp.port");
		this.timeout = MyUtil.getMilliseconds(config, "fileserver.timeout");
		this.checkPeriod = MyUtil.getMilliseconds(config, "fileserver.checkPeriod");
		this.hmacKeyPath = MyUtil.getString(config, "hmac.key");
	}

	public Long getCheckPeriod()
	{
		return checkPeriod;
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
