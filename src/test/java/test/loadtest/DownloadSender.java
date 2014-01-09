package test.loadtest;

import java.io.IOException;
import java.io.PrintStream;
import java.util.TimerTask;

import client.IClientCli;

public class DownloadSender extends TimerTask
{
	private IClientCli client;
	private String download;
	private PrintStream out;

	public DownloadSender(IClientCli client, String download, PrintStream out)
	{
		this.client = client;
		this.download = download;
		this.out = out;
	}

	@Override
	public void run()
	{
		synchronized(client)
		{
			try
			{
				out.println(client.download(download));
			}
			catch(IOException e)
			{
				out.print(e);
			}
		}
	}
}
