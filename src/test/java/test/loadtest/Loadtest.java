package test.loadtest;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.KeyHolder;
import model.MapKeyHolder;
import proxy.IProxyCli;
import server.IFileServerCli;
import util.ComponentFactory;
import util.Config;
import util.EncryptionUtil;
import util.UnvalidConfigException;
import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;
import client.ClientConfig;
import client.IClientCli;
import client.ManagementConfig;

public class Loadtest implements Runnable
{
	private LoadtestConfig config;
	private IProxyCli proxy;
	private IFileServerCli server;
	private List<IClientCli> clients = new ArrayList<IClientCli>();
	private final String CLIENT = "client";// clientname
	private final String host = "localhost";
	private final int beginnPort = 12290;
	private final int rmiPort = 12289;
	private final File downloadDir = new File(System.getProperty("java.io.tmpdir"), "download");
	private final String bindingName = "hubbabubba";

	static ComponentFactory componentFactory = new ComponentFactory();

	public static void main(String[] args)
	{
		try
		{
			new ComponentFactory().startLoadTest(new Config("loadtest"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Loadtest(Config config)
	{
		try
		{
			this.config = new LoadtestConfig(config);
		}
		catch(UnvalidConfigException ue)
		{
			System.err.println(ue.getMessage());
			return;
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			LoadtestConfig.printUsage(System.err);
			return;
		}
		Thread thread = new Thread(this);
		thread.start();
	}

	public void after() throws Exception
	{
		try
		{
			proxy.exit();
		}
		catch(Exception e)
		{
			// This should not happen. In case it does, output the stack trace for easier trouble shooting.
			e.printStackTrace();
		}
		try
		{
			server.exit();
		}
		catch(IOException e)
		{
			// This should not happen. In case it does, output the stack trace for easier trouble shooting.
			e.printStackTrace();
		}
		for(IClientCli client : clients)
		{
			try
			{
				client.exit();
			}
			catch(IOException e)
			{
				// This should not happen. In case it does, output the stack trace for easier trouble shooting.
				e.printStackTrace();
			}
		}
		System.err.println("Loadtest END");
	}

	public void before() throws Exception
	{
		KeyPair proxyKeyPair = EncryptionUtil.generateRSAKeyPair();

		// same keyPair for all clients
		KeyHolder userKeyHolder = generateKeys(config.getNumberClients());

		proxy = componentFactory.startProxy(new Config("proxy"), new Config("mc"), new Shell("proxy", new TestOutputStream(System.err), new TestInputStream()));

		server = componentFactory.startFileServer(new Config("fs1"), new Shell("fs1", new TestOutputStream(System.err), new TestInputStream()));

		for(int i = 1; i <= config.getNumberClients(); i++)
		{
			ClientConfig clientConfig = createClientConfig(i, userKeyHolder, proxyKeyPair.getPublic());
			ManagementConfig managementConfig = createManagementConfig(i, userKeyHolder);
			clients.add(componentFactory.startClient(clientConfig, managementConfig, new Shell(CLIENT + i, new TestOutputStream(System.err), new TestInputStream())));
		}
	}

	private ClientConfig createClientConfig(int i, KeyHolder userKeyHolder, PublicKey publicProxyKey)
	{
		System.err.println("downloaddir" + downloadDir);
		String proxyHost = "localhost";
		Integer proxyTcpPort = beginnPort + i;
		return new ClientConfig(downloadDir, proxyHost, proxyTcpPort, userKeyHolder, publicProxyKey);
	}

	private ManagementConfig createManagementConfig(int i, KeyHolder userKeyHolder)
	{
		return new ManagementConfig(bindingName, host, rmiPort, userKeyHolder);
	}

	private KeyHolder generateKeys(int numberClients) throws NoSuchAlgorithmException
	{
		Map<String, KeyPair> keys = new HashMap<String, KeyPair>();
		for(int i = 1; i <= numberClients; i++)
		{
			keys.put(CLIENT + i, EncryptionUtil.generateRSAKeyPair());
		}
		return new MapKeyHolder(keys);
	}

	@Override
	public void run()
	{
		try
		{
			System.err.println("before");
			before();
			System.err.println("test");
			test();
			System.err.println("after");
			after();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void test() throws Exception
	{

	}
}
