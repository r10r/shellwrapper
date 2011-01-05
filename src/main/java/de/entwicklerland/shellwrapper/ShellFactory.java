package de.entwicklerland.shellwrapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Factory to create shell instances for executing commands.
 * The output of a command is returned encapsulated in 
 * {@link CommandResult} instances.
 * 
 * @author rjenster
 *
 */
public class ShellFactory {

	public static final String NEWLINE = "\n";
	
	public static Shell createInstance(ShellCommand shellCommand) {
		return new Shell(shellCommand.command);
	}
	
	public static class Shell {
		private Process shellProcess;
		private BufferedWriter stdin;
		private BufferedReader stdout;
		private BufferedReader stderr;
		private boolean hasExited = false;
		
		
		// marker indicating the end of the command output
		private final String EOO = "END-" + System.currentTimeMillis();
		
		/**
		 * Creates a new shell. Remember to call {@link #exit()} on the shell instance once the
		 * shell is not used anymore.
		 * 
		 * @throws IllegalStateException which wraps an IOException if the execution of the shell command fails
		 */
		public Shell(String command) {
			try {
				shellProcess = Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			
			stdin  = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
			stdout = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
			stderr = new BufferedReader(new InputStreamReader(shellProcess.getErrorStream()));
		}
	
		/**
		 * Execute multiple commands in sequence.
		 * 
		 * @see #execute(String, String)
		 * @param commands the commands to execute
		 * @return a list of {@link CommandResult} instances for the executed commands
		 */
		public List<CommandResult> execute(String... commands) {
			List<CommandResult> output = new ArrayList<CommandResult>(commands.length);
			
			for(int i=0; i <commands.length; i++) {
				output.add(execute(commands[i]));
			}
			
			return output;
		}
		
		/**
		 * Execute a single command 
		 * 
		 * @see #execute(String, String)
		 * @param command
		 * @return a {@link CommandResult} instance for the executed command
		 */
		public CommandResult execute(String command) {
			return execute(NEWLINE, command);
		}

		/**
		 * Convenience method to send the given message to the given recieveCommand using echo and a pipe.
		 * E.g. calling
		 * <pre>
		 * 	executePipedEcho("Hello World", "wc -c")
		 * </pre>
		 * will result in
		 * <pre>
		 * 	echo "Hello World" | wc -c
		 * </pre>
		 * being executed in the shell
		 * 
		 * @see #execute(String, String)
		 * @param message the message to send to the receiveCommand
		 * @param recieveCommand the command to receive the message
		 * @return a {@link CommandResult} instance for the executed command
		 */
		public CommandResult executePipedEcho(String message, String recieveCommand) {
			return executePiped("echo " + "\'" + message + "\'", recieveCommand);
		}
		
		/**
		 * Convenience method to send the output of the sendCommand to the given recieveCommand using a pipe.
		 * E.g. calling
		 * <pre>
		 * 	executePiped("whoami", "wc -c")
		 * </pre>
		 * will result in
		 * <pre>
		 * 	whoami | wc -c
		 * </pre>
		 * being executed in the shell
		 * 
		 * @see #execute(String, String)
		 * @param sendCommand the command to generate the message
		 * @param recieveCommand the command to receive the message
		 * @return a {@link CommandResult} instance for the executed command
		 */
		public CommandResult executePiped(String sendCommand, String recieveCommand) {
			return execute(NEWLINE, sendCommand + " | " + recieveCommand);
		}
		
		
		/**
		 * Executes a single command in the shell.
		 * 
		 * @param terminator terminates and executes the command in the shell
		 * @param command the command to execute
		 * @return a {@link CommandResult} instance for the executed command
		 * @throws IllegalStateException if console process has exited or if it's not possible 
		 * to write the command to the console or read the results
		 */
		private CommandResult execute(String terminator, String command) {
			if (hasExited()) {
				throw new IllegalStateException("Console process has already exited!");
			} else {
				try {
					stdin.write(command);
					stdin.write(terminator);
					writeMarker();
					stdin.flush();
				
					return new CommandResult(command, getOutput(stdout), getOutput(stderr));
				
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}
	
		/**
		 * Closes all connected streams (STDOUT,STDERR and STDIN) and terminates the shell process.
		 */
		public void exit() {
			/*
			 *  ensure all streams are closed properly,
			 *  which is not properly done by calling console.destroy()
			 */
			
			for(Closeable stream : new Closeable[]{stdout, stdin, stderr}) {
				try {
					stream.close();
				} catch (IOException e) {
					// just ignore it;
				}
				
			}
			shellProcess.destroy();
			hasExited = true;
		}
	
		/**
		 * @return true if the shell process has exited.
		 */
		public boolean hasExited() {
			return hasExited;
		}
	
		/**
		 * Writes the marker to STDIN/STDERR that marks the end 
		 * of the command output.
		 * 
		 * @throws IOException
		 */
		private void writeMarker() throws IOException {
			// write EOC marker to stdout
			stdin.write("echo " + EOO);
			stdin.write(NEWLINE);
			// write EOC marker to stderr
			stdin.write("echo " + EOO + " >&2"); 
			stdin.write(NEWLINE);
		}
	
		/**
		 * Reads the output lines from the given reader up
		 * to the 'end of output' marker {@link #EOO}.
		 * 
		 * @param reader
		 * @return
		 * @throws IOException
		 */
		private List<String> getOutput(BufferedReader reader) throws IOException {
			
			List<String> lines = new LinkedList<String>();
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.equals(EOO)) {
					break;
				} else {
					lines.add(line);
				}
			}
			
			return lines;
		}
		
	}

	/**
	 * Helper method to concatenate the given lines using the given separator.
	 * 
	 * @param lines
	 * @param separator
	 * @return
	 */
	static String concatLines(List<String> lines, String separator) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append(separator);
		}
		return sb.toString();
	}
}