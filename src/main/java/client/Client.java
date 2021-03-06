package client;

import java.io.File;
import java.net.Socket;
import java.rmi.Naming;

import model.IRmiServerData;
import util.ComponentFactory;
import util.Config;
import util.MyUtil;
import util.UnvalidConfigException;
import cli.Shell;
import cli.ShellThread;

public class Client extends ClientCommands implements Runnable
{
	private ShellThread shellThread;
	private Shell shell;

	public static void main(String[] args) throws Exception
	{
		new ComponentFactory().startClient(new Config("client"), new Config("mc"), new Shell("client", System.out, System.in));
	}

	public Client(ClientConfig config, ManagementConfig managementConfig, Shell shell)
	{
		this.shell = shell;
		this.clientConfig = config;
		this.manageConfig = managementConfig;
		Thread thread = new Thread(this);
		thread.start();
	}

	public Client(Config config, Config mc, Shell shell)
	{
		this.shell = shell;
		try
		{
			this.clientConfig = new ClientConfig(config);
			this.manageConfig = new ManagementConfig(mc);
		}
		catch(UnvalidConfigException ue)
		{
			MyUtil.writeToShell(shell, ue.getMessage());
			return;
		}
		catch(Exception e)
		{
			MyUtil.writeToShell(shell, "only parameters allowed:");
			MyUtil.writeToShell(shell, "download.dir: the directory to put downloaded files.");
			MyUtil.writeToShell(shell, "proxy.host: the host name (or an IP address) where the Proxy is running.");
			MyUtil.writeToShell(shell, "proxy.tcp.port: the TCP port where the server is listening for client connections.");
			MyUtil.writeToShell(shell, "keys.dir: directory where to look for the user's private key (named <username>.pem).");
			MyUtil.writeToShell(shell, "proxy.key: file from where to read Proxy's public key.");

			// TODO MC missing
		}
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public File getDownloadDir()
	{
		return clientConfig.getDownloadDir();
	}

	@Override
	public void run()
	{
		try
		{
			// in run method, otherwise getInputStream is blocking
			proxySocket = new Socket(clientConfig.getProxyHost(), clientConfig.getProxyTcpPort());

			rmiData = (IRmiServerData)Naming.lookup(manageConfig.getUrl());

			shell.register(this);
			shellThread = new ShellThread(shell);
			shellThread.start();
		}
		catch(Exception e)
		{
			if(e.getMessage() != null && e.getMessage().equals("Connection refused: connect"))
			{
				MyUtil.writeToShell(shell, "proxy not available");
			}
			e.printStackTrace();
			shutdown();
		}

	}

	@Override
	public void shutdown()
	{
		try
		{
			if(aesChannel != null)
			{
				aesChannel.close();
			}
			if(proxySocket != null)
			{
				proxySocket.close();
			}
			if(shellThread != null)
			{
				shellThread.stop();
			}
		}
		catch(Exception e)
		{
			MyUtil.printStackTrace(e);
		}
	}

}
