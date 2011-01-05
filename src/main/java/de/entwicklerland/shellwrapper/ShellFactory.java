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
 * Factory to create shell instances for 
 * executing commands and capturing the command output.
 * The output of a command is returned encapsulated in 
 * an ExecutedCommand Object.
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
		private Process console;
		private BufferedWriter stdin;
		private BufferedReader stdout;
		private BufferedReader stderr;
		private boolean hasExited = false;
		
		
		// marker indicating the end of the command output
		private final String EOO = "END-"+System.currentTimeMillis();
		
		/**
		 * Creates a new shell.Remember to call {@link #exit()} on the shell instance once the
		 * shell is not used anymore.
		 * 
		 * @throws IllegalStateException which wraps an IOException when the execution of the shell command fails
		 */
		public Shell(String command) {
			try {
				console = Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			
			stdin  = new BufferedWriter(new OutputStreamWriter(console.getOutputStream()));
			stdout = new BufferedReader(new InputStreamReader(console.getInputStream()));
			stderr = new BufferedReader(new InputStreamReader(console.getErrorStream()));
		}
	
		public List<CommandExecution> run(String... cmds) {
			List<CommandExecution> output = new ArrayList<CommandExecution>(cmds.length);
			
			for(int i=0; i <cmds.length; i++) {
				output.add(run(cmds[i]));
			}
			
			return output;
		}
		
		public CommandExecution runPipedEcho(String message, String recipient) {
			return runPiped("echo " + "\'" + message + "\'", recipient);
		}
		
		public CommandExecution runPiped(String sendCmd, String recipient) {
			return run(NEWLINE, sendCmd + " | " + recipient);
		}
		
		public CommandExecution run(String cmd) {
			return run(NEWLINE, cmd);
		}
		
		/**
		 * 
		 * @param cmd
		 * @return
		 * @throws IllegalStateException if console process has exited or if it's not possible 
		 * to write the command to the console or read the results
		 */
		public CommandExecution run(String terminator, String cmd) {
			if (hasExited()) {
				throw new IllegalStateException("Console process has exited allready!");
			} else {
				try {
					stdin.write(cmd);
					stdin.write(terminator);
					writeMarker();
					stdin.flush();
				
					return new CommandExecution(cmd, getOutput(stdout), getOutput(stderr));
				
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}
	
		public void exit() {
			/*
			 *  ensure all streams are closed properly,
			 *  which is not properly by calling console.destroy()
			 */
			
			for(Closeable stream : new Closeable[]{stdout, stdin, stderr}) {
				try {
					stream.close();
				} catch (IOException e) {
					// just ignore it;
				}
				
			}
			console.destroy();
			hasExited = true;
		}
	
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
	 * Concatenates the given lines using the given separator.
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