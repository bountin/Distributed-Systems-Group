package proxy;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import util.ComponentFactory;
import util.Config;
import util.MyUtil;
import util.UnvalidConfigException;
import cli.Shell;
import cli.ShellThread;

public class Proxy extends ProxyCommands implements Runnable
{
	private ServerSocket proxySocket;
	private ShellThread shellThread;
	private List<ProxyThread> proxyThreads = new ArrayList<ProxyThread>();
	private boolean closed;
	private IsAliveHandler isAliveHandler;
	private ProxyConfig proxyConfig;

	public static void main(String[] args)
	{
		try
		{
			new ComponentFactory().startProxy(new Config("proxy"), new Shell("proxy", System.out, System.in));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Proxy(Config config, Shell shell) throws UnvalidConfigException, IOException
	{
		try
		{
			this.proxyConfig = new ProxyConfig(config);
		}
		catch(UnvalidConfigException ue)
		{
			shell.writeLine(ue.getMessage());
			return;
		}
		catch(Exception e)
		{
			shell.writeLine("only parameters allowed:");
			shell.writeLine("tcp.port: the port to be used for instantiating a java.net.ServerSocket (handling TCP connection requests from clients).");
			shell.writeLine("udp.port: the port to be used for instantiating a java.net.DatagramSocket (handling UDP requests from fileservers).");
			shell.writeLine("fileserver.timeout: the period in milliseconds each fileserver has to send an isAlive packet (only containing the fileserver's TCP port). If no such packet is received within this time, the fileserver is assumed to be offline and is no longer available for handling requests.");
			shell.writeLine("fileserver.checkPeriod: specifies the number of delay milliseconds to repeatedly test whether a fileserver has timed-out or not (see fileserver.timeout).");
			shell.writeLine("keys.dir: directory where to look for user's public keys (named <username>.pub.pem).");
			shell.writeLine("key: telling where to read the Proxy's private key.");
			shell.writeLine("hmac.key: The path to the secret key that is used to authenticate the proxy's communication with the file servers.");
			return;
		}
		try
		{
			ProxyInfo.getInstance().setHmacKeyPath(proxyConfig.getHmacKeyPath());

			proxySocket = new ServerSocket(proxyConfig.getTcpPort());

			Thread thread = new Thread(this);
			thread.start();

			shell.register(this);
			shellThread = new ShellThread(shell);
			shellThread.start();

			DatagramSocket datagramSocket = new DatagramSocket(proxyConfig.getUdpPort());

			isAliveHandler = new IsAliveHandler(datagramSocket, proxyConfig.getTimeout(), proxyConfig.getCheckPeriod());
			isAliveHandler.start();
		}
		catch(IOException e)
		{
			System.err.printf("Could not listen on port: %d.", proxyConfig.getTcpPort());
			System.err.println(e);
		}
	}

	@Override
	public void run()
	{
		try
		{
			while(!closed)
			{
				System.err.println("Listening ...");
				ProxyThread proxyThread = new ProxyThread(proxySocket.accept(), "proxyClientThread", proxyConfig);
				proxyThread.start();
				proxyThreads.add(proxyThread);
			}
		}
		catch(Exception e)
		{
			MyUtil.printStackTrace(e);
		}
		finally
		{
			if(!closed)
			{
				shutdown();
			}
		}
	}

	public synchronized void setClosed(boolean closed)
	{
		this.closed = closed;
	}

	@Override
	public synchronized void shutdown()
	{
		if(closed == true)
		{
			return;
		}
		setClosed(true);
		System.err.println("Stopping clients ...");
		for(ProxyThread thread : proxyThreads)
		{
			thread.shutdown();
		}
		shellThread.stop();
		isAliveHandler.shutdown();
		if(proxySocket != null)
		{
			try
			{
				System.err.println("Stopping Proxy ...");
				proxySocket.close();
			}
			catch(IOException e)
			{
				System.err.println("Error closing socket with port " + proxySocket.getLocalPort());
				e.printStackTrace();
			}
		}
		System.err.println("Proxy CLOSED");
	}

}
