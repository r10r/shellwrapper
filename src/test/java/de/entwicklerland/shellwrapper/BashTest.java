package de.entwicklerland.shellwrapper;

import org.junit.Before;

public class BashTest extends AbstractShellTest {

	@Before
	public void createShell() {
		setShell(ShellFactory.createInstance(ShellCommand.BASH));
	}
}
