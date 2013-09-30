package eu.compassresearch.core.interpreter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.overture.ast.analysis.AnalysisException;

import eu.compassresearch.ast.program.AFileSource;
import eu.compassresearch.ast.program.PSource;
import eu.compassresearch.core.interpreter.api.CmlInterpreter;
import eu.compassresearch.core.interpreter.api.CmlInterpreterException;
import eu.compassresearch.core.interpreter.api.RandomSelectionStrategy;
import eu.compassresearch.core.interpreter.api.behaviour.CmlBehaviour;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransition;
import eu.compassresearch.core.typechecker.VanillaFactory;
import eu.compassresearch.core.typechecker.api.CmlTypeChecker;
import eu.compassresearch.core.typechecker.api.TypeIssueHandler;

@RunWith(Parameterized.class)
public class InterpretAllCmlFilesTest {

	private String filePath;

	public InterpretAllCmlFilesTest(String filePath) {
		CmlRuntime.logger().setLevel(Level.OFF);
		this.filePath = filePath;
	}

	// @Parameters
	// public static Collection params() {
	// return new LinkedList<String>();
	// }

	// private static String watchedLog;

	@Rule
	public TestWatcher watchman = new TestWatcher() {

		@Override
		protected void failed(Throwable e, Description d) {
			// watchedLog+= d + "\n";

			System.out.println("Test failed in : " + d.getMethodName() + " : "
					+ filePath);
			System.out.println(e);
		}
		
		@Override
		protected void starting(Description description) {

			System.out.println("Test started : " + description.getMethodName() + " : "
					+ filePath);
			super.starting(description);
		}

		@Override
		protected void succeeded(Description d) {

			System.out.println(d.getMethodName() + " : '" + filePath
					+ "' completed succesfully");
		}
	};

	@Before
	public void setUp() {
		
	}

	@Test
	public void testParseCmlFile() throws IOException, AnalysisException,
			CmlInterpreterException {

		File f = new File(filePath);
		AFileSource ast = new AFileSource();
		ast.setName(f.getName());
		ast.setFile(f);

		String resultPath = filePath.split("[.]")[0] + ".result";

		//ExpectedTestResult testResult =(new File(resultPath).exists()? ExpectedTestResult.parseTestResultFile(resultPath):null);
		ExpectedTestResult testResult = ExpectedTestResult.parseTestResultFile(resultPath);
		//if(testResult == null)
		//	Assert.fail("The testResult is not formatted correctly");
		
		assertTrue(CmlParserUtil.parseSource(ast));

		// Type check
		TypeIssueHandler tcIssue = VanillaFactory.newCollectingIssueHandle();
		CmlTypeChecker cmlTC = VanillaFactory.newTypeChecker(
				Arrays.asList(new PSource[] { ast }), tcIssue);

		boolean isTypechecked = cmlTC.typeCheck();
		
		if(!isTypechecked)
			System.out.println(tcIssue.getTypeErrors());
			
		
		assertTrue(isTypechecked);

		CmlInterpreter interpreter = VanillaInterpreterFactory.newInterpreter(ast);

		Exception exception = null;
		try{
			interpreter.initialize();
			interpreter.execute(new RandomSelectionStrategy());
		}
		catch(Exception ex)
		{
			exception = ex;
		}

		checkResult(testResult, interpreter, exception);
	}
	
	private void checkResult(ExpectedTestResult testResult, CmlInterpreter interpreter, Exception exception) {

		CmlBehaviour topProcess = interpreter.getTopLevelProcess();
		
		//Exceptions check
		//testResult.throwsException() => exception != null
		assertTrue("The test was expected to throw an exception but did not!",!testResult.throwsException() || exception != null);
		//!testResult.throwsException() => exception == null
		assertTrue("The test threw an unexpected exception : " + exception,testResult.throwsException() || exception == null);

		//events
		String eventTrace = traceToString(topProcess.getTraceModel().getEventTrace());
		Pattern trace = testResult.getExpectedEventTracePattern();
		Matcher matcher = trace.matcher(eventTrace);
		assertTrue(testResult.getExpectedEventTracePattern() + " != " + eventTrace,matcher.matches());
		
		//TimedTrace
		if(testResult.hasTimedTrace())
		{
			//Convert the trace into a list of strings to compare it with the expected
			String timedTrace = traceToString(topProcess.getTraceModel().getObservableTrace());
			
			matcher = testResult.getExpectedTimedTracePattern().matcher(timedTrace);
			assertTrue(testResult.getExpectedTimedTracePattern() + " != " + timedTrace,matcher.matches());
		}
		
		//Interpreter state
		Assert.assertEquals(testResult.getInterpreterState(), interpreter.getStatus());
	}
	
	private String traceToString(List<CmlTransition> trace)
	{
		StringBuilder result = new StringBuilder();
		
		for(int  i = 0 ; i < trace.size();i++)
		{
			CmlTransition e  = trace.get(i);
			if(i > 0)
				result.append(",");
				
			result.append(e.toString());
		}

		return result.toString();
	}

	@Parameters
	public static Collection<Object[]> getCmlfilePaths() {

		List<Object[]> paths = findAllCmlFiles("src/test/resources");
		
		//List<Object[]> paths = findAllCmlFiles("src/test/resources/action/parallel-composition");
		
				//findAllCmlFiles("src/test/resources/action/");
		//paths.addAll(findAllCmlFiles("src/test/resources/process/"));
		//paths.addAll(findAllCmlFiles("src/test/resources/examples/"));
		//paths.addAll(findAllCmlFiles("src/test/resources/classes/"));
		//paths.addAll(findAllCmlFiles("src/test/resources/action/replicated/"));
		
		return paths;
	}
	
	private static List<Object[]> findAllCmlFiles(String folderPath)
	{
		List<Object[]> paths = new Vector<Object[]>();
		File folder = new File(folderPath);
		
		paths.addAll(addFilesInFolder(folder));
		
		for(File subfolder  : findSubfolders(folder))
			paths.addAll(addFilesInFolder(subfolder));
		
		return paths;
	}
	
	private static List<File> findSubfolders(File folder)
	{
		List<File> subfolders = new LinkedList<File>();
				
		subfolders.addAll(Arrays.asList(folder.listFiles(new FilenameFilter() {
			  @Override
			  public boolean accept(File dir, String name) {
			    return new File(dir, name).isDirectory();
			  }
			})));
		
		List<File> subsubfolders = new LinkedList<File>();
		for(File sub : subfolders)
			subsubfolders.addAll(findSubfolders(sub));
		
		subfolders.addAll(subsubfolders);
		
		return subfolders;
	}
	
	private static List<Object[]> addFilesInFolder(File folder)
	{
		
		//Make filter to only get the files that ends with '.cml'
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".cml");
			}
		};
		
		//Add the folders to search in
		String[] children = folder.list(filter);
		
		List<Object[]> paths = new Vector<Object[]>();
		
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				paths.add(new Object[] { folder.getPath() + "/" + children[i] });
			}
		}
		return paths;
	}
}
