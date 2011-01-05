package de.entwicklerland.shellwrapper;

/**
 * Collection of 
 * @author rjenster
 *
 */
public enum ShellCommand {
	BASH("bash -s"),
	SH("sh -s");
	
	public String command;
	
	private ShellCommand(String command) {
		this.command = command;
	}
}