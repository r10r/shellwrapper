package de.entwicklerland.shellwrapper;

import org.junit.Before;

public class ShTest extends AbstractShellTest {

	@Before
	public void createConsole() {
		setShell(ShellFactory.createInstance(ShellCommand.SH));
	}
}
