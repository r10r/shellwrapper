package de.entwicklerland.shellwrapper;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import de.entwicklerland.shellwrapper.ShellFactory.Shell;

public abstract class AbstractShellTest {
	
	private Shell shell = null;
	
	public void setShell(Shell shell) {
		this.shell = shell;
	}
	
	@After
	public void terminateConsole() {
		shell.exit();
	}
	
	@Test
	public void testSimpleCommand() {
		assertEchoOutput(shell, "FOO");
		assertEchoOutput(shell, "BAR");
	}

	
	@Test(expected = IllegalStateException.class)
	public void testTermination() {
		assertEchoOutput(shell, "FOO");
		shell.exit();
		assertEchoOutput(shell, "BAR");
		
	}
	
	@Test
	public void testErrorOutput() {
		String command = "nosuchcommand";
		
		CommandResult executedCommand = shell.execute("nosuchcommand");
		
		assertEquals("executed command should match", command, executedCommand.getCommand());
		assertEquals("one error line should be returned", 1, executedCommand.getErrorLines().size());
		assertTrue("stdout should be empty", executedCommand.getOutputLines().isEmpty());
	}
	
	@Test
	public void testMultiLineOutput() {
		String command = "echo FOO; echo BAR";
		String expectedReturn = "FOO\nBAR\n";
		CommandResult executedCommand = shell.execute(command);
		
		assertEquals("executed command should match", command, executedCommand.getCommand());
		assertEquals("returned lines count should match", 2, executedCommand.getOutputLines().size());
		assertEquals("return value should match", expectedReturn, executedCommand.getOutput());
		assertTrue("stderr should be empty", executedCommand.getErrorLines().isEmpty());
	}
	
	private void assertEchoOutput(Shell shell, String value) {
		String command = "echo " + value;
		CommandResult executedCommand = shell.execute(command);
		
		assertEquals("executed command should match", command, executedCommand.getCommand());
		assertEquals("one line should be returned", 1, executedCommand.getOutputLines().size());
		assertEquals("returned value should match", value, executedCommand.getOutputLines().get(0));
		assertTrue("stderr should be empty", executedCommand.getErrorLines().isEmpty());
	}
	
	@Test
	public void testMultipleCommands() {
		String command = "echo FOO; echo BAR";
		String expectedReturn = "FOO\nBAR\n";
		CommandResult executedCommand = shell.execute(command);
		
		assertEquals("executed command should match", command, executedCommand.getCommand());
		assertEquals("returned lines count should match", 2, executedCommand.getOutputLines().size());
		assertEquals("return value should match", expectedReturn, executedCommand.getOutput());
		assertTrue("stderr should be empty", executedCommand.getErrorLines().isEmpty());
		
		String command1 = "echo BAR; echo FOO";
		String expectedReturn1 = "BAR\nFOO\n";
		CommandResult executedCommand1 = shell.execute(command1);
		
		assertEquals("executed command should match", command1, executedCommand1.getCommand());
		assertEquals("returned lines count should match", 2, executedCommand1.getOutputLines().size());
		assertEquals("return value should match", expectedReturn1, executedCommand1.getOutput());
		assertTrue("stderr should be empty", executedCommand1.getErrorLines().isEmpty());
	}
	
}
