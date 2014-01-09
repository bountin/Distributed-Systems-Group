package test.loadtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.TimerTask;

import client.IClientCli;

public class UploadSender extends TimerTask
{
	private IClientCli client;
	private File upload;
	private PrintStream out;
	private boolean overwrite;

	public UploadSender(IClientCli client, File uploadOverwriteFile, PrintStream out, boolean overwrite)
	{
		super();
		this.client = client;
		this.upload = uploadOverwriteFile;
		this.out = out;
		this.overwrite = overwrite;
	}

	private void copyFile(File from, File to)
	{
		try
		{
			InputStream in = new FileInputStream(from);
			OutputStream out = new FileOutputStream(to);

			byte[] buf = new byte[1024];
			int len;
			while((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
		catch(FileNotFoundException ex)
		{
			System.out.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void run()
	{
		try
		{
			if(overwrite)
			{
				File uploadFile = File.createTempFile("bla", "blub", upload.getParentFile());
				copyFile(upload, uploadFile);

				synchronized(client)
				{
					try
					{
						out.println(client.upload(uploadFile.getName()));
					}
					catch(IOException e)
					{
						out.print(e);
					}
				}
				uploadFile.delete();
			}
			else
			{
				synchronized(client)
				{
					try
					{
						out.println(client.upload(upload.getName()));
					}
					catch(IOException e)
					{
						out.print(e);
					}
				}
			}
		}
		catch(IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
