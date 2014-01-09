package test.loadtest;

import java.io.IOException;
import java.io.PrintStream;
import java.util.TimerTask;

import client.IClientCli;

public class UploadSender extends TimerTask
{
	private IClientCli client;
	private String upload;
	private PrintStream out;

	public UploadSender(IClientCli client, String upload, PrintStream out)
	{
		super();
		this.client = client;
		this.upload = upload;
		this.out = out;
	}

	@Override
	public void run()
	{
		// synchronized(client)
		{
			try
			{
				out.println(client.upload(upload));
			}
			catch(IOException e)
			{
				out.print(e);
			}
		}
	}
}
