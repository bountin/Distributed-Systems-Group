package client;

import util.Config;
import util.MyUtil;
import util.UnvalidConfigException;

import java.io.File;

public class ManagementConfig {

	private String bindingName;
	private String host;
	private Integer port;
	private File keyDir;

	public ManagementConfig(Config config) throws UnvalidConfigException {
		bindingName = MyUtil.getString(config, "binding.name");
		host = MyUtil.getString(config, "proxy.host");
		port = MyUtil.getPort(config, "proxy.rmi.port");
		keyDir = MyUtil.getDirectory(config, "keys.dir");
	}

	public String getBindingName() {
		return bindingName;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public File getKeyDir() {
		return keyDir;
	}

	public String getUrl() {
		return String.format("//%s:%d/%s", getHost(), getPort(), getBindingName());
	}
}
