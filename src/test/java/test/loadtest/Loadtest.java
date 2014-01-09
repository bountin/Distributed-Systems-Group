package test.loadtest;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import model.KeyHolder;
import model.MapKeyHolder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import proxy.IProxyCli;
import proxy.ProxyConfig;
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
	private String overWriteUploadFile;
	private String uploadFile = "fs1.txt";
	private List<IClientCli> clients = new ArrayList<IClientCli>();
	private final String CLIENT = "client";// clientname
	private final String host = "localhost";
	private final Integer tcpPort = 12290;
	private final Integer udpPort = 12291;
	private final int rmiPort = 12299;
	private final String folder = System.getProperty("user.dir");
	// private final File downloadDir = new File(System.getProperty("java.io.tmpdir"), "download");
	private final File downloadDir = new File(folder, "download");
	private final String bindingName = "managementservice";
	private List<Timer> timers = new ArrayList<Timer>();
	private int uploadNonOverwriteSec;
	private int uploadOverwriteSec;
	private final String HMAC_KEY_PATH = "keys/hmac.key";

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
		super();
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

	@After
	public void after() throws Exception
	{
		Thread.sleep(3000);
		for(Timer timer : timers)
		{
			timer.cancel();
		}
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

	@Before
	public void before() throws Exception
	{
		System.out.println(downloadDir.mkdir());
		KeyPair proxyKeyPair = EncryptionUtil.generateRSAKeyPair();

		System.out.println("privateProxyString " + Arrays.toString(proxyKeyPair.getPrivate().getEncoded()));
		System.out.println("publicProxyString " + Arrays.toString(proxyKeyPair.getPublic().getEncoded()));

		// same keyPair for all clients
		KeyHolder userKeyHolder = generateKeys(config.getNumberClients());
		uploadNonOverwriteSec = 30;// 60 / config.getUploadsPerMin() / (1.0 - config.getOverwriteRatio());
		System.out.println(uploadOverwriteSec);

		proxy = componentFactory.startProxy(createProxyConfig(userKeyHolder, proxyKeyPair.getPublic(), proxyKeyPair.getPrivate()), createManagementConfig(userKeyHolder), new Shell("proxy", new TestOutputStream(System.out), new TestInputStream()));

		server = componentFactory.startFileServer(new Config("fs1"), new Shell("fs1", new TestOutputStream(System.out), new TestInputStream()));

		synchronized(clients)
		{
			for(int i = 1; i <= config.getNumberClients(); i++)
			{
				ClientConfig clientConfig = createClientConfig(i, userKeyHolder, proxyKeyPair.getPublic());
				ManagementConfig managementConfig = createManagementConfig(userKeyHolder);
				IClientCli client = componentFactory.startClient(clientConfig, managementConfig, new Shell(CLIENT + i, new TestOutputStream(System.out), new TestInputStream()));
				clients.add(client);
			}
		}
	}

	private ClientConfig createClientConfig(int i, KeyHolder userKeyHolder, PublicKey publicProxyKey) throws IOException
	{
		File clientDirectory = new File(downloadDir, "client" + i);
		System.out.println(clientDirectory.mkdir());
		String proxyHost = host;
		return new ClientConfig(clientDirectory, proxyHost, tcpPort, userKeyHolder, publicProxyKey);
	}

	private ManagementConfig createManagementConfig(KeyHolder userKeyHolder)
	{
		return new ManagementConfig(bindingName, host, rmiPort, userKeyHolder);
	}

	private ProxyConfig createProxyConfig(KeyHolder userKeys, PublicKey proxyPublicKey, PrivateKey proxyPrivateKey)
	{
		String hmacKeyPath = HMAC_KEY_PATH;
		ProxyConfig proxyConfig = new ProxyConfig(tcpPort, udpPort, new Long(300), new Long(1000), proxyPublicKey, proxyPrivateKey, userKeys, hmacKeyPath);
		return proxyConfig;
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

	@Test
	public void test() throws Exception
	{
		Thread.sleep(1000);
		synchronized(clients)
		{
			for(int i = 0; i < config.getNumberClients(); i++)
			{
				clients.get(i).login(CLIENT + (i + 1), "");

				// Timer timer = new Timer();
				// timer.schedule(new DownloadSender(clients.get(i), uploadFile, System.out), 0, uploadNonOverwriteSec);
				// timers.add(timer);
			}
		}

	}
}
