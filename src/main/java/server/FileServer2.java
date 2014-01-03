package server;

import util.Config;
import cli.Shell;
import cli.TestOutputStream;

public class FileServer2
{
	public static void main(String[] args)
	{
		try
		{
			new util.ComponentFactory().startFileServer(new Config("fs2"), new Shell("fs2", new TestOutputStream(System.out), System.in));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
