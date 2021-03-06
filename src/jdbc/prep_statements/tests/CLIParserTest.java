/**
 * 
 */
package jdbc.prep_statements.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import jdbc.prep_statements.CommandLineParser;
import joptsimple.OptionException;

/**
 * Unit tests for methods of CommandLineParser class
 * @author pkomon, lmayer
 * @version 20160221.1
 */
public class CLIParserTest {

	/*didn't know how to test for console output; looked it up on stackoverflow*/
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	/*basically everything is just printed to this streams instead of stdout;
	 *  therefore it can be tested easily*/
	
	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	private CommandLineParser parser;
	
	/**
	 * Called before each test, used to redirect all content, written to stdout to another
	 * output stream (it sets System.out to another output stream then stdout)
	 */
	@Before
	public void setUp(){
		System.setOut(new PrintStream(outContent));
		parser = new CommandLineParser();
	}
	
	/**
	 * Called after each test, resets System.out to stdout
	 */
	@After
	public void cleanUp(){
		System.setOut(null);
		parser = null;
	}

	/**
	 * Test method for {@link jdbc.prep_statements.CommandLineParser#parse(java.lang.String[])}.
	 * Should throw an exception because not all require were arguments were suppled
	 */
	@Test(expected=OptionException.class)
	public void testParseArgsMissing() {
		String[] args = {"--host", "host_name"}; // arguments missing -> should lead to error
		parser.parse(args);
	}
	
	/**
	 * Test method for {@link jdbc.prep_statements.CommandLineParser#parse(java.lang.String[])}.
	 * If all arguments were supplied, no exception should be thrown
	 */
	@Test
	public void testParseArgsSupplied() {
		String[] args1 = {"--host", "host_name", "--dbname", "name", "--username", "user", "--password", "pw"}; // arguments here ->  no error
		parser.parse(args1);
		String[] args2 = {"-h", "host_name", "-db", "name", "-u", "user", "-pw", "pw"}; // synonym forms
		parser.parse(args2);
	}
	
	/**
	 * Test method for {@link jdbc.prep_statements.CommandLineParser#parse(java.lang.String[])}.
	 * If all "help" option was supplied, no exception should be thrown
	 */
	@Test
	public void testParseHelp() {
		exit.expectSystemExitWithStatus(0);
		String[] args = {"--help"}; // help option here -> no error
		parser.parse(args);
	}

	/**
	 * Test method for {@link jdbc.prep_statements.CommandLineParser#printHelp()}.
	 * Should print the descriptions of the options and arguments
	 */
	@Test
	public void testPrintHelp() {
		parser.printHelp();
		String output = outContent.toString();
		Assert.assertTrue(output.contains("--host")); //didn't test ALL of the content, that will be printed out...
		Assert.assertTrue(output.contains("server to connect to"));
		Assert.assertTrue(output.contains("<database_name>"));
		Assert.assertTrue(output.contains("Description"));
		Assert.assertTrue(output.contains("--username <username>"));
		Assert.assertTrue(output.contains("prints this"));
		Assert.assertTrue(output.contains("-p"));
		Assert.assertTrue(output.contains("--port"));
		Assert.assertTrue(output.contains("default: 5432")); //...but that should be enough
	}

	/**
	 * Test method for {@link jdbc.prep_statements.CommandLineParser#getArgumentOf(java.lang.String)}.
	 * Gets the values of the required arguments, tests if optional arguments defaults to certain value
	 */
	@Test
	public void testGetArgumentOf() {
		String hostname = "some_host", dbname = "some_db", username = "some_user", password = "super_secret", port = "5432";
		String[] args1 = {"--host", hostname, "--dbname", dbname, "--username", username, "--password", password};
		parser.parse(args1);
		Assert.assertEquals(hostname, parser.getArgumentOf("host"));
		Assert.assertEquals(dbname, parser.getArgumentOf("dbname"));
		Assert.assertEquals(username, parser.getArgumentOf("username"));
		Assert.assertEquals(password, parser.getArgumentOf("password"));
		Assert.assertEquals(port, parser.getArgumentOf("port"));
		String[] args2 = {"--host", hostname, "--db", dbname, "--username", username, "--password", password, "--port", "1111"};
		parser.parse(args2);
		Assert.assertEquals("1111", parser.getArgumentOf("port"));
		Assert.assertEquals("some_db", parser.getArgumentOf("databasename"));
	}
	
	/**
	 * Test method for {@link jdbc.prep_statements.CommandLineParser#hasOption(java.lang.String)}.
	 * Checks if option were present when parsing or not
	 */
	@Test
	public void testHasOption() {
		String[] args = {"--host", "some_host", "--dbname", "some_db", "--username", "some_user", "--password", "some_pw", "-v"};
		parser.parse(args);
		Assert.assertTrue(parser.hasOption("host"));
		Assert.assertTrue(parser.hasOption("v"));
		Assert.assertFalse(parser.hasOption("port"));
	}

}
