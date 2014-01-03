package cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Reads commands from an {@link InputStream}, executes them and writes the result to a {@link OutputStream}.
 */
public class ShellThread
{
	private Thread shellThread;
	private Shell shell;

	public ShellThread(Shell shell)
	{
		this.shell = shell;
	}

	public void start()
	{
		shellThread = new Thread(shell);
		shellThread.start();
	}

	public void stop()
	{
		try
		{
			shellThread.interrupt();
			shell.close();
			System.in.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}


}