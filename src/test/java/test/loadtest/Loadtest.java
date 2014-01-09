package test.loadtest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import model.KeyHolder;
import model.MapKeyHolder;
import objects.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import proxy.IProxyCli;
import proxy.ProxyConfig;
import proxy.ProxyInfo;
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
	private final String folder = System.getProperty("user.dir");
	// private final File downloadDir = new File(System.getProperty("java.io.tmpdir"), "download");
	private final File uploadDownloadDir = new File(folder, "download");
	private String uploadOverwriteFilename = "update.txt";
	private String uploadFilename = "update.txt";
	private String downloadFilename = "fsxf1.txt";
	private File uploadFile = new File(uploadDownloadDir, uploadFilename);
	private List<IClientCli> clients = new ArrayList<IClientCli>();
	private final String CLIENT = "alice";// clientname
	private final String host = "localhost";
	private final Integer tcpPort = 12290;
	private final Integer udpPort = 12291;
	private final int rmiPort = 12299;
	private final String bindingName = "managementservice";
	private List<Timer> timers = new ArrayList<Timer>();

	// periods
	private Integer uploadNonOverwriteSec;
	private Integer uploadOverwriteSec;
	private Integer downloadSec;
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
		deleteFiles();
		System.err.println("Loadtest END");
	}

	@Before
	public void before() throws Exception
	{
		createFiles();

		// KeyPair proxyKeyPair = EncryptionUtil.generateRSAKeyPair();
		// System.out.println(proxyKeyPair.getPrivate().getEncoded().length + " privateProxyString " + Arrays.toString(proxyKeyPair.getPrivate().getEncoded()));
		// System.out.println(proxyKeyPair.getPublic().getEncoded().length + " publicProxyString " + Arrays.toString(proxyKeyPair.getPublic().getEncoded()));
		PublicKey publicProxyKey = EncryptionUtil.getPublicKeyFromFile(new File("keys/proxy.pub.pem"));
		PrivateKey privateProxyKey = EncryptionUtil.getPrivateKeyFromFile(new File("keys/proxy.pem"), "12345");

		// same keyPair for all clients
		KeyHolder userKeyHolder = generateKeys(config.getNumberClients());
		initPeriods(config);

		proxy = componentFactory.startProxy(createProxyConfig(userKeyHolder, publicProxyKey, privateProxyKey), createManagementConfig(userKeyHolder), new Shell("proxy", new TestOutputStream(System.out), new TestInputStream()));

		server = componentFactory.startFileServer(new Config("fs1"), new Shell("fs1", new TestOutputStream(System.out), new TestInputStream()));

		Thread.sleep(1000);
		ProxyInfo.getInstance().setUsers(createUsers(config.getNumberClients()));

		synchronized(clients)
		{
			for(int i = 1; i <= config.getNumberClients(); i++)
			{
				ClientConfig clientConfig = createClientConfig(i, userKeyHolder, publicProxyKey);
				ManagementConfig managementConfig = createManagementConfig(userKeyHolder);
				IClientCli client = componentFactory.startClient(clientConfig, managementConfig, new Shell(CLIENT + i, System.out, new TestInputStream()));
				clients.add(client);
			}
		}
	}

	private ClientConfig createClientConfig(int i, KeyHolder userKeyHolder, PublicKey publicProxyKey) throws IOException
	{
		String proxyHost = host;
		return new ClientConfig(uploadDownloadDir, proxyHost, tcpPort, userKeyHolder, publicProxyKey);
	}

	private void createFiles() throws IOException
	{
		System.out.println(uploadDownloadDir.mkdir());

		generateFile(uploadFile, config.getFileSizeKB());
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

	private Map<String, User> createUsers(int numberClients)
	{
		Map<String, User> users = new HashMap<String, User>();
		for(int i = 1; i <= numberClients; i++)
		{
			users.put(CLIENT + i, new User(CLIENT + i, CLIENT + i, false, i * 1000));
		}
		return users;
	}

	private void deleteDir(File dir)
	{
		if(dir.isDirectory() && dir.list().length > 0)
		{
			for(File f : dir.listFiles())
			{
				f.delete();
			}
		}
		dir.delete();

	}

	private void deleteFiles()
	{
		deleteDir(uploadDownloadDir);
	}

	public void generateFile(File file, long fileSizeKB) throws IOException
	{
		RandomAccessFile f = new RandomAccessFile(file, "rw");
		f.setLength(fileSizeKB * 1024);
	}

	private KeyHolder generateKeys(int numberClients) throws Exception
	{
		Map<String, KeyPair> keys = new HashMap<String, KeyPair>();
		for(int i = 1; i <= numberClients; i++)
		{
			PublicKey publicKey = EncryptionUtil.getPublicKeyFromFile(new File("keys/alice.pub.pem"));
			PrivateKey privateKey = EncryptionUtil.getPrivateKeyFromFile(new File("keys/alice.pem"), "12345");

			KeyPair keyPair = new KeyPair(publicKey, privateKey);
			keys.put(CLIENT + i, keyPair);
			// System.out.println(keyPair.getPublic().getEncoded().length + " client" + i + " key: " + Arrays.toString(keyPair.getPublic().getEncoded()));
		}
		return new MapKeyHolder(keys);
	}

	private void initPeriods(LoadtestConfig config2)
	{
		if((1 - config.getOverwriteRatio()) > 0)
		{
			uploadNonOverwriteSec = (int)Math.ceil((60.0 / config.getUploadsPerMin()) / (1.0 - config.getOverwriteRatio()));
		}
		if(config.getOverwriteRatio() > 0)
		{
			uploadOverwriteSec = (int)Math.ceil((60.0 / config.getUploadsPerMin()) / config.getOverwriteRatio());
		}
		if(config.getDownloadsPerMin() > 0)
		{
			downloadSec = (int)Math.ceil(60.0 / config.getDownloadsPerMin());
		}
		System.out.println("uploadnono " + uploadNonOverwriteSec);
		System.out.println("uploadover " + uploadOverwriteSec);
		System.out.println("download " + downloadSec);
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

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.err.println("after");
			try
			{
				after();
			}
			catch(Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

				Timer timer = new Timer();
				if(uploadNonOverwriteSec != null)
				{
					timer.schedule(new UploadSender(clients.get(i), uploadFilename, System.out), 0, uploadNonOverwriteSec);
				}
				if(uploadOverwriteSec != null)
				{
					timer.schedule(new UploadSender(clients.get(i), uploadOverwriteFilename, System.out), 0, uploadOverwriteSec);
				}
				if(downloadSec != null)
				{
					timer.schedule(new DownloadSender(clients.get(i), downloadFilename, System.out), 0, downloadSec);
				}

				timers.add(timer);
			}
		}

	}
}
