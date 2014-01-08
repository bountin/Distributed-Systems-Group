package client;

import model.DirectoryKeyHolder;
import model.KeyHolder;
import util.Config;
import util.MyUtil;
import util.UnvalidConfigException;

public class ManagementConfig
{

	private String bindingName;
	private String host;
	private Integer port;
	private KeyHolder keyDir;

	public ManagementConfig(Config config) throws UnvalidConfigException
	{
		bindingName = MyUtil.getString(config, "binding.name");
		host = MyUtil.getString(config, "proxy.host");
		port = MyUtil.getPort(config, "proxy.rmi.port");
		keyDir = new DirectoryKeyHolder(MyUtil.getDirectory(config, "keys.dir"));
	}

	public ManagementConfig(String bindingName, String host, Integer port, KeyHolder keyDir)
	{
		super();
		this.bindingName = bindingName;
		this.host = host;
		this.port = port;
		this.keyDir = keyDir;
	}

	public String getBindingName()
	{
		return bindingName;
	}

	public String getHost()
	{
		return host;
	}

	public KeyHolder getKeyDir()
	{
		return keyDir;
	}

	public Integer getPort()
	{
		return port;
	}

	public String getUrl()
	{
		return String.format("//%s:%d/%s", getHost(), getPort(), getBindingName());
	}
}
