package test.loadtest;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.String;
import java.lang.System;
import java.util.TimerTask;

import client.Client;

public class SubscriptionSender extends TimerTask
{
	private Client client;
	private PrintStream out;
	private final String filename;
	private final int downloadsPerSecond;

	private int count = 0;

	public SubscriptionSender(Client client, PrintStream out, String filename, int downloadsPerSecond)
	{
		super();
		this.client = client;
		this.out = out;
		this.filename = filename;
		this.downloadsPerSecond = downloadsPerSecond;
	}

	@Override
	public void run()
	{
		for (int i=0;i<downloadsPerSecond;i++) {
			client.subscribe(filename, ++count);
		}
	}
}
