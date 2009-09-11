package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

public class CommandTest extends TestCase {
	public void testAlias() {
		assertTrue(Command.ALIAS.matches("/alias nsanch"));
		assertTrue(Command.ALIAS.matches(" /alias with-a-space-in-front"));
		assertFalse(
				Command.ALIAS.matches("no you shouldn't handle this string with /alias"));
		assertTrue(Command.ALIAS.matches("/alias abcABC123-_*'"));
	}
}
