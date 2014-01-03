package server;

import util.Config;
import cli.Shell;
import cli.TestOutputStream;

public class FileServer1
{
	public static void main(String[] args)
	{
		try
		{
			new util.ComponentFactory().startFileServer(new Config("fs1"), new Shell("fs1", new TestOutputStream(System.out), System.in));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
