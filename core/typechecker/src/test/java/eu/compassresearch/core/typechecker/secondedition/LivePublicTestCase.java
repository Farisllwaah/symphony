package eu.compassresearch.core.typechecker.secondedition;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class LivePublicTestCase extends
		AbstractResultBasedCmlTypeCheckerTestCase
{
	private static String caseStudyDir = System.getProperty("CASESTUDIES")+"/../PublicLiveCMLCaseStudies/";
	static String[] specifications = {

	"/BitRegister", "/Dwarf","/AVDeviceDiscovery","/ChoiceSupportInDebugger","/RingBuffer" };

	public LivePublicTestCase(File file, List<File> files, String name,
			TestType type)
	{
		super(file, files, name, type);
	}

	@Parameters(name = "{2}")
	public static Collection<Object[]> getData()
	{
		if (caseStudyDir == null)
		{
			return new LinkedList<Object[]>();
		}
		Collection<Object[]> results = new Vector<Object[]>();

		for (String spec : specifications)
		{
			results.addAll(collectTestDataMultipleFiles(caseStudyDir + spec, TestType.POSITIVE, TestType.ANY.endswith));
		}

		for (Object[] test : results)
		{
			test[2] = test[2].toString().substring(caseStudyDir.length() + 1);
		}

		return results;

	}

	// @Before
	// public void setup()
	// {
	// Properties.recordTestResults = true;
	// }

	@Override
	protected File createResultFile(String filename)
	{
		return new File(filename + "/result.result");
	}

	@Override
	protected File getResultFile(String filename)
	{
		return new File(filename + "/result.result");
	}

}
