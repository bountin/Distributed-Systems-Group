package example;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import cli.Command;
import cli.Shell;

/**
 * Contains a small showcase of how to use {@link Shell} and its commands.
 */
public class ShellExample {

	/**
	 * Contains some commands to be executed by a {@link Shell}.
	 */
	class ExampleCommands
	{
		/**
		 * Terminates the {@link Shell}.
		 * 
		 * Note that you do not have to deal with exceptions. If a command throws one, {@link Shell} catches it and prints it to the provided {@link OutputStream}. <b>However, you should handle them
		 * on your own whenever it is possible.</b>
		 * 
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		@Command
		public void exit() throws IOException
		{
			// Stop the Shell from listening for commands
			shell.close();

			/*
			 * If the Shell uses System.in, you have to close it manually (see Shell.close() for further information). The reason for that is, that reading from System.in is a blocking operation.
			 */
			System.in.close();
		}

		/**
		 * Returns the current date and time.
		 * 
		 * @return the date and time
		 */
		@Command
		public Date time()
		{
			return new Date();
		}
	}

	/**
	 * Contains additional commands for the example.
	 */
	class MathCommands {
		/**
		 * Adds two numbers and returns the result.
		 *
		 * Note that this command can be invoked with !add and not !sum because of the annotation.
		 * The ConversionService internally used by the Shell automatically converts the next two words after the
		 * command to Integers in order to invoke the command.
		 *
		 * @param a the first summand
		 * @param b the second summand
		 * @return the sum
		 */
		@Command("add")
		public long sum(int a, int b) {
			return a + b;
		}
	}

	private Thread shellThread;
	private Shell shell;

	public static void main(String... args) {
		new ShellExample().run();
	}

	private void run()
	{
		/*
		 * First, create a new Shell instance and provide InputStream as well as an OutputStream.
		 * If you want to test the application manually, simply use System.in and System.out.
		 * Otherwise, you can use TestInputStream and TestOutputStream from the template.
		 */
		shell = new Shell("example", System.out, System.in);

		/*
		 * Next, register all commands the Shell should support.
		 */
		// shell.register(new ExampleCommands());
		// shell.register(new MathCommands());
		ExampleCommands exampleCommands = new ExampleCommands();
		shell.register(exampleCommands);

		/*
		 * Finally, make the Shell process the commands read from the InputStream by invoking Shell.run(). Note that Shell implements the Runnable interface. Thus, you can run the Shell asynchronously
		 * by starting a new Thread:
		 * 
		 * 
		 * 
		 * In that case, do not forget to terminate the Thread ordinarily. Otherwise, the program will not exit.
		 */
		shellThread = new Thread(shell);
		shellThread.start();
	}
}
