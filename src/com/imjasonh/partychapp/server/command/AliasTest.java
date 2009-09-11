package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Message;

public class AliasTest extends TestCase {
	AliasHandler handler = new AliasHandler();
	
	public void testAlias() {
		assertTrue(handler.matches(new Message("/alias nsanch")));
		assertTrue(handler.matches(new Message(" /alias with-a-space-in-front")));
		assertFalse(handler.matches(new Message("no you shouldn't handle this string with /alias")));
		assertTrue(handler.matches(new Message("/alias abcABC123-_*'")));
	}
}
