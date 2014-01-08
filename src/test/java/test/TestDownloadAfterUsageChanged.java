package test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import proxy.IProxyCli;
import server.IFileServerCli;
import util.ComponentFactory;
import util.Config;
import util.Util;
import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;
import client.IClientCli;

public class TestDownloadAfterUsageChanged
{
	static ComponentFactory componentFactory = new ComponentFactory();
	IProxyCli proxy;
	List<IFileServerCli> servers = new ArrayList<IFileServerCli>();
	IClientCli client;

	@After
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
			for(IFileServerCli server : servers)
			{
				server.exit();
			}
		}
		catch(IOException e)
		{
			// This should not happen. In case it does, output the stack trace for easier trouble shooting.
			e.printStackTrace();
		}
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

	@Before
	public void before() throws Exception
	{
		proxy = componentFactory.startProxy(new Config("proxy"), new Config("mc"), new Shell("proxy", new TestOutputStream(System.out), new TestInputStream()));
		Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);

		for(int i = 1; i <= 5; i++)
		{
			servers.add(componentFactory.startFileServer(new Config("fs" + i), new Shell("fs" + i, new TestOutputStream(System.out), new TestInputStream())));
			Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);
		}

		client = componentFactory.startClient(new Config("client"), new Config("mc"), new Shell("client", new TestOutputStream(System.out), new TestInputStream()));
		Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);
	}

	@Test
	public void test() throws Exception
	{
		String actual = client.login("alice", "12345").toString();
		String expected = "success";
		assertTrue(String.format("Response must contain '%s' but was '%s'", expected, actual), actual.contains(expected));

		System.out.println(proxy.fileservers());

		actual = client.download("fs3.txt").toString();
		expected = "!data fileserver3 filecontent";
		assertTrue(String.format("Response must start with '%s' but was '%s'", expected, actual), actual.startsWith(expected));

		System.out.println(proxy.fileservers());

		actual = client.download("fs1.txt").toString();
		expected = "!data fileserver1 filecontent";
		assertTrue(String.format("Response must start with '%s' but was '%s'", expected, actual), actual.startsWith(expected));

		actual = client.logout().toString();
		expected = "Successfully logged out.";
		assertTrue(String.format("Response must contain '%s' but was '%s'", expected, actual), actual.contains(expected));
	}
}
