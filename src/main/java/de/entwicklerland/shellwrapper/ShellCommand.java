package de.entwicklerland.shellwrapper;

/**
 * Concrete shell commands that are supported
 * by  the {@link ShellFactory.Shell} implementation.
 * 
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