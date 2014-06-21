/**
 * Proof Obligation Generator Analysis
 *
 * Description: 
 * 
 * This analysis extends the QuestionAnswerAdaptor to generate
 * POs from the AST generated by the CML parser
 *
 */

package eu.compassresearch.core.analysis.pog.visitors;

// Java libraries 
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.ACaseAlternative;
import org.overture.ast.expressions.PExp;
import org.overture.ast.expressions.PModifier;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.modules.PExport;
import org.overture.ast.modules.PExports;
import org.overture.ast.modules.PImports;
import org.overture.ast.modules.PModules;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.ASetBind;
import org.overture.ast.patterns.ASetMultipleBind;
import org.overture.ast.patterns.ATypeBind;
import org.overture.ast.patterns.ATypeMultipleBind;
import org.overture.ast.patterns.PBind;
import org.overture.ast.patterns.PPair;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.patterns.PPatternBind;
import org.overture.ast.statements.AMapSeqStateDesignator;
import org.overture.ast.statements.ATixeStmtAlternative;
import org.overture.ast.statements.PCase;
import org.overture.ast.statements.PClause;
import org.overture.ast.statements.PObjectDesignator;
import org.overture.ast.statements.PStateDesignator;
import org.overture.ast.statements.PStm;
import org.overture.ast.types.PAccessSpecifier;
import org.overture.ast.types.PField;
import org.overture.ast.types.PType;
import org.overture.pog.contexts.POCaseContext;
import org.overture.pog.contexts.POContextStack;
import org.overture.pog.contexts.PONotCaseContext;
import org.overture.pog.obligation.SeqApplyObligation;
import org.overture.pog.pub.IPOContextStack;
import org.overture.pog.pub.IProofObligationList;

import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.declarations.PSingleDeclaration;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.ast.program.AFileSource;
import eu.compassresearch.ast.program.AInputStreamSource;
import eu.compassresearch.ast.program.PSource;
import eu.compassresearch.core.analysis.pog.obligations.CmlProofObligationList;
import eu.compassresearch.core.analysis.pog.utility.PogPubUtil;

public class ProofObligationGenerator extends
		QuestionAnswerCMLAdaptor<IPOContextStack, CmlProofObligationList>
{
	/**
	 * Main generator class for the POG. <b> Do not.</b> apply this class directly to an ast as it will not order or
	 * number the POs correctly. <br>
	 * Use {@link PogPubUtil} methods instead.
	 */

	private final static String ANALYSIS_NAME = "Proof Obligation Generator";

	private CmlPogAssistantFactory assistantFactory;

	// ---------------------------------------------
	// -- Proof Obligation Generator State
	// ---------------------------------------------

	// subvisitors
	private POGExpressionVisitor expressionVisitor;
	private POGStatementVisitor statementVisitor;
	private POGProcessVisitor processVisitor;
	private POGDeclAndDefVisitor declAndDefVisitor;
	private POGActionVisitor actionVisitor;

	private void initialize()
	{
		assistantFactory = new CmlPogAssistantFactory();
		expressionVisitor = new POGExpressionVisitor(this);
		statementVisitor = new POGStatementVisitor(this);
		processVisitor = new POGProcessVisitor(this);
		declAndDefVisitor = new POGDeclAndDefVisitor(this,assistantFactory);
		actionVisitor = new POGActionVisitor(this);
		
	}

	// ---------------------------------------------
	// -- Dispatch to sub-visitors
	// ---------------------------------------------

	// Duplicated main overture handlers. Necessary for now since we don't want
	// to
	// switch visitor context at the root level
	public ProofObligationGenerator()
	{
		this.initialize();
	}

	@Override
	public CmlProofObligationList defaultPDefinition(PDefinition node,
			IPOContextStack question) throws AnalysisException
	{
		return node.apply(this.declAndDefVisitor, question);
	}

	@Override
	public CmlProofObligationList defaultPSingleDeclaration(
			PSingleDeclaration node, IPOContextStack question)
			throws AnalysisException
	{
		return node.apply(this.declAndDefVisitor, question);
	}

	@Override
	public CmlProofObligationList caseAModuleModules(AModuleModules node,
			IPOContextStack question) throws AnalysisException
	{
		IProofObligationList ovtpos = assistantFactory.createPDefinitionAssistant().getProofObligations(node.getDefs(), this.declAndDefVisitor, question);
		CmlProofObligationList cmlpos = new CmlProofObligationList();
		cmlpos.addAll(ovtpos);

		return cmlpos;
	}

	@Override
	public CmlProofObligationList defaultPProcess(PProcess node,
			IPOContextStack question) throws AnalysisException
	{
		return node.apply(this.processVisitor, question);
	}

	@Override
	public CmlProofObligationList defaultPAction(PAction node,
			IPOContextStack question) throws AnalysisException
	{
		return node.apply(this.actionVisitor, question);
	}

	@Override
	public CmlProofObligationList defaultPStm(PStm node,
			IPOContextStack question) throws AnalysisException
	{
		return node.apply(this.statementVisitor, question);
	}

	@Override
	public CmlProofObligationList defaultPExp(PExp node,
			IPOContextStack question) throws AnalysisException
	{
		return node.apply(this.expressionVisitor, question);
	}

	@Override
	public CmlProofObligationList caseASetBind(ASetBind node,
			IPOContextStack question) throws AnalysisException
	{
		return node.getSet().apply(this.expressionVisitor, question);
	}

	@Override
	public CmlProofObligationList caseASetMultipleBind(ASetMultipleBind node,
			IPOContextStack question) throws AnalysisException
	{
		return node.getSet().apply(this.expressionVisitor, question);
	}

	@Override
	public CmlProofObligationList caseACaseAlternative(ACaseAlternative node,
			IPOContextStack question) throws AnalysisException
	{

		CmlProofObligationList obligations = new CmlProofObligationList();

		question.push(new POCaseContext(node.getPattern(), node.getType(), node.getCexp(),assistantFactory));
		obligations.addAll(node.getResult().apply(this.expressionVisitor, question));
		question.pop();
		question.push(new PONotCaseContext(node.getPattern(), node.getType(), node.getCexp(),assistantFactory));

		return obligations;
	}

	@Override
	public CmlProofObligationList caseAMapSeqStateDesignator(
			AMapSeqStateDesignator node, IPOContextStack question)
			throws AnalysisException
	{

		CmlProofObligationList list = new CmlProofObligationList();

		if (node.getSeqType() != null)
		{
			list.add(new SeqApplyObligation(node.getMapseq(), node.getExp(), question,assistantFactory));
		}

		// Maps are OK, as you can create new map domain entries

		return list;
	}

	@Override
	public CmlProofObligationList caseATixeStmtAlternative(
			ATixeStmtAlternative node, IPOContextStack question)
			throws AnalysisException
	{

		CmlProofObligationList list = new CmlProofObligationList();

		if (node.getPatternBind().getPattern() != null)
		{
			// Nothing to do
		} else if (node.getPatternBind().getBind() instanceof ATypeBind)
		{
			// Nothing to do
		} else if (node.getPatternBind().getBind() instanceof ASetBind)
		{
			ASetBind bind = (ASetBind) node.getPatternBind().getBind();
			list.addAll(bind.getSet().apply(this.expressionVisitor, question));
		}

		list.addAll(node.getStatement().apply(this.statementVisitor, question));
		return list;

	}

	// Return empty lists for a bunch of stuff...

	@Override
	public CmlProofObligationList defaultPModifier(PModifier node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPType(PType node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPField(PField node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPAccessSpecifier(
			PAccessSpecifier node, IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPPattern(PPattern node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPPair(PPair node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPBind(PBind node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList caseATypeMultipleBind(ATypeMultipleBind node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPPatternBind(PPatternBind node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPModules(PModules node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPImports(PImports node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPExports(PExports node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPExport(PExport node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPStateDesignator(
			PStateDesignator node, IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPObjectDesignator(
			PObjectDesignator node, IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPClause(PClause node,
			IPOContextStack question)
	{

		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList defaultPCase(PCase node,
			IPOContextStack question)
	{
		return new CmlProofObligationList();
	}

	// ---------------------------------------------
	// -- Public API to CML POG
	// ---------------------------------------------
	// Taken from Type Checker code
	// ---------------------------------------------
	/**
	 * This method is invoked by the command line tool when pretty printing the analysis name.
	 * 
	 * @return Pretty short name for this analysis.
	 */
	public String getAnalysisName()
	{
		return ANALYSIS_NAME;
	}

	/**
	 * Run the proof obligation generator. The POs are placed in the return value but we may eventually want to switch
	 * them over to the registry
	 * 
	 * @param sources
	 *            The list of definition to generate obligations for
	 * @return - Returns CMLProofObligation list. This may need to change.
	 */
	public CmlProofObligationList generatePOs(List<PDefinition> sources)
			throws AnalysisException
	{
		this.initialize();
		CmlProofObligationList obligations = new CmlProofObligationList();
		IPOContextStack ctxt = new POContextStack();

		// for each CML paragraph
		for (PDefinition paragraph : sources)
		{
			try
			{

				// process paragraph:
				obligations.addAll(paragraph.apply(this, ctxt));
				// obligations.addAll(paragraph.apply(overturePog, ctxt));

			} catch (AnalysisException ae)
			{
				// unexpected pog crash
				throw ae;
			}
		}

		obligations.renumber();
		return obligations;
	}

	// ---------------------------------------------
	// Static stuff for running the POG from Eclipse
	// ---------------------------------------------
	// Taken from Type Checker code
	// ---------------------------------------------

	// setting the file on AFileSource allows the POG to interact with it
	// TODO this method is a duplicate( from VanillaTypeChecker). Should be
	// placed in a common utils lib
	private static PSource prepareSource(File f)
	{
		if (f == null)
		{
			AInputStreamSource iss = new AInputStreamSource();
			iss.setStream(System.in);
			iss.setOrigin("stdin");
			return iss;
		} else
		{
			AFileSource fs = new AFileSource();
			fs.setName(f.getName());
			fs.setFile(f);
			return fs;
		}
	}

	/**
	 * This method runs the PO generator on a given file. The method invokes methods to generate POs.
	 * 
	 * @param f
	 *            - The file to generate POs
	 */
	// TODO this method is a duplicate( from VanillaTypeChecker). Should be
	// placed in a common utils lib
	private static void runOnFile(File f) throws IOException
	{
		// set file name
		PSource source = prepareSource(f);

		// generate POs
		ProofObligationGenerator cmlPOG = new ProofObligationGenerator();
		try
		{
			cmlPOG.generatePOs(source.getParagraphs());
		} catch (AnalysisException e)
		{
			System.out.println("The Symphony Proof Obligation Generator failed on this cml-source. Please submit it for investigation to richard.payne@ncl.ac.uk.\n");
			e.printStackTrace();
		}

		// Report success
		System.out.println("Proof Obligation Generation is complete for the given CML Program");
	}

	/**
	 * Main method for class. Current test class takes a set of cml examples and generates POs for each
	 */
	// TODO the body of this method is a duplicate (from VanillaTypeChecker)
	public static void main(String[] args) throws IOException
	{
		File cml_examples = new File("../../docs/cml-examples");
		int failures = 0;
		int successes = 0;
		// runOnFile(null);

		if (cml_examples.isDirectory())
		{
			for (File example : cml_examples.listFiles())
			{
				System.out.print("Generating Proof Obligations for example: "
						+ example.getName() + " \t\t...: ");
				System.out.flush();
				try
				{
					runOnFile(example);
					System.out.println("done");
					successes++;
				} catch (Exception e)
				{
					System.out.println("exception");
					failures++;
				}
			}
		}

		System.out.println(successes + " was successful, " + failures
				+ " was failures.");
	}

	@Override
	public CmlProofObligationList createNewReturnValue(INode node,
			IPOContextStack question)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CmlProofObligationList createNewReturnValue(Object node,
			IPOContextStack question)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
