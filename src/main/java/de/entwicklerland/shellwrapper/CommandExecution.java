package de.entwicklerland.shellwrapper;

import java.util.List;

/**
 * An instance encapsulates the output
 * to STDIN and STDERR for an executed command.
 * STDIN and STDERR output are available as a collection
 * of output lines or a single string.
 * 
 * @author rjenster
 *
 */
public class CommandExecution {
	
	private String command;
	private List<String> outputLines;
	private List<String> errorLines;
	private String output;
	private String error;
	
	public CommandExecution(String command, List<String> output, List<String> error) {
		this.command = command;
		this.outputLines = output;
		this.errorLines = error;
	}
	
	public String getOutput() {
		return getOutput(ShellFactory.NEWLINE);
	}
	
	public String getOutput(String lineSeparator) {
		if (output == null) {
			output =  ShellFactory.concatLines(outputLines, lineSeparator);
		} 
		return output;
	}
	
	public String getError() {
		return getError(ShellFactory.NEWLINE);
	}
	
	public String getError(String lineSeparator) {
		if (error == null) {
			error =  ShellFactory.concatLines(errorLines, lineSeparator);
		} 
		return error;
	}

	public String getCommand() {
		return command;
	}

	public List<String> getOutputLines() {
		return outputLines;
	}

	public List<String> getErrorLines() {
		return errorLines;
	}		
	
}