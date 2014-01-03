package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import util.Config;
import util.MyUtil;
import util.UnvalidConfigException;
import cli.Shell;
import cli.ShellThread;
import cli.TestOutputStream;

public class FileServer extends FileServerCommands implements Runnable
{
	private Timer timer = new Timer();
	private ServerSocket fileServerSocket;
	private ShellThread shellThread;
	private static int instanceCount = 1;
	private NetworkId networkId;
	private FileServerConfig fileServerConfig;
	private FileServerManager fileServerManager;
	private boolean closed;
	private List<FileServerThread> fileServerThreads = new ArrayList<FileServerThread>();

	public static void main(String[] args)
	{
		try
		{
			new util.ComponentFactory().startFileServer(new Config("fs" + instanceCount++), new Shell("fs1", new TestOutputStream(System.out), System.in));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public FileServer(Config config, Shell shell) throws IOException
	{
		try
		{
			this.fileServerConfig = new FileServerConfig(config);
		}
		catch(UnvalidConfigException ue)
		{
			shell.writeLine(ue.getMessage());
			return;
		}
		catch(Exception e)
		{
			shell.writeLine("only parameters allowed:");
			shell.writeLine("fileserver.dir: the directory that contains all the files clients can download.");
			shell.writeLine("tcp.port: the port to be used for instantiating a ServerSocket (handling the TCP requests from the Proxy).");
			shell.writeLine("proxy.host: the host name (or an IP address) where the Proxy is running.");
			shell.writeLine("proxy.udp.port: the UDP port where the Proxy is listening for fileserver datagrams.");
			shell.writeLine("fileserver.alive: the period in ms the fileserver needs to send an isAlive datagram to the Proxy.");
			return;
		}
		fileServerManager = new FileServerManager(fileServerConfig);

		try
		{
			fileServerSocket = new ServerSocket(fileServerConfig.getTcpPort());
			networkId = new NetworkId(fileServerSocket.getInetAddress(), fileServerSocket.getLocalPort());

			Thread thread = new Thread(this);
			thread.start();
		}
		catch(Exception e)
		{
			if(e.getMessage() != null && e.getMessage().equals("Address already in use: JVM_Bind"))
			{
				shell.writeLine("server already started");
				shutdown();
				return;
			}
			else
			{
				shell.writeLine(String.format("Fileserver could not listen on port: %d.", fileServerConfig.getTcpPort()));
				shell.writeLine(e.getMessage());
			}
		}

		try
		{
			NetworkId proxyUdpId = new NetworkId(InetAddress.getByName(fileServerConfig.getProxyHost()), fileServerConfig.getProxyUdpPort());
			timer.schedule(new IsAliveSender(networkId, proxyUdpId), 0, fileServerConfig.getPeriod());
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}

		shell.register(this);
		shellThread = new ShellThread(shell);
		shellThread.start();
	}

	@Override
	public void run()
	{
		try
		{
			while(!closed)
			{
				try
				{
					FileServerThread fileServerThread = new FileServerThread(fileServerManager, fileServerSocket.accept(), "fileserverHandlerThread");
					fileServerThread.start();
					fileServerThreads.add(fileServerThread);
				}
				catch(Exception e)
				{
					MyUtil.printStackTrace(e);
				}
			}
		}
		catch(Exception e)
		{
			if(e.getMessage().equals("socket closed"))
			{}
			else
			{
				e.printStackTrace();
			}
		}
		finally
		{
			if(!closed)
			{
				shutdown();
			}
		}
	}

	@Override
	public synchronized void shutdown()
	{
		closed = true;
		try
		{
			for(FileServerThread thread : fileServerThreads)
			{
				thread.shutdown();
			}
			timer.cancel();

			if(fileServerSocket != null)
			{
				fileServerSocket.close();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		if(shellThread != null)
		{
			shellThread.stop();
		}
	}

}
