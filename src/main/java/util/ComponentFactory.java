package util;

import proxy.IProxyCli;
import proxy.Proxy;
import server.FileServer;
import server.IFileServerCli;
import test.loadtest.Loadtest;
import cli.Shell;
import client.Client;
import client.ClientConfig;
import client.IClientCli;
import client.ManagementConfig;

/**
 * Provides methods for starting an arbitrary amount of various components.
 */
public class ComponentFactory
{
	public IClientCli startClient(ClientConfig config, ManagementConfig mc, Shell shell) throws Exception
	{
		return new Client(config, mc, shell);
	}

	/**
	 * Creates and starts a new client instance using the provided {@link Config} and {@link Shell}.
	 * 
	 * 
	 * @param config
	 *            the configuration containing parameters such as connection info
	 * @param mc
	 * @param shell
	 *            the {@code Shell} used for processing commands @return the created component after starting it successfully
	 * @throws Exception
	 *             if an exception occurs
	 */
	public IClientCli startClient(Config config, Config mc, Shell shell) throws Exception
	{
		return new Client(config, mc, shell);
	}

	/**
	 * Creates and starts a new file server instance using the provided {@link Config} and {@link Shell}.
	 * 
	 * @param config
	 *            the configuration containing parameters such as connection info
	 * @param shell
	 *            the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception
	 *             if an exception occurs
	 */
	public IFileServerCli startFileServer(Config config, Shell shell) throws Exception
	{
		return new FileServer(config, shell);
	}

	public Loadtest startLoadTest(Config config) throws Exception
	{
		return new Loadtest(config);
	}

	/**
	 * Creates and starts a new proxy instance using the provided {@link Config} and {@link Shell}.
	 * 
	 * 
	 * @param config
	 *            the configuration containing parameters such as connection info
	 * @param mc
	 * @param shell
	 *            the {@code Shell} used for processing commands @return the created component after starting it successfully
	 * @throws Exception
	 *             if an exception occurs
	 */
	public IProxyCli startProxy(Config config, Config mc, Shell shell) throws Exception
	{
		return new Proxy(config, mc, shell, "12345");
	}
}
