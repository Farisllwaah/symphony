package eu.compassresearch.core.interpreter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexLocation;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.node.INode;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.ObjectValue;
import org.overture.interpreter.values.RecordValue;
import org.overture.interpreter.values.Value;

import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.core.interpreter.api.CmlBehaviorFactory;
import eu.compassresearch.core.interpreter.api.CmlBehaviour;
import eu.compassresearch.core.interpreter.api.CmlBehaviour.BehaviourName;
import eu.compassresearch.core.interpreter.api.CmlTrace;
import eu.compassresearch.core.interpreter.api.TransitionEvent;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransitionSet;
import eu.compassresearch.core.interpreter.api.transitions.TauTransition;
import eu.compassresearch.core.interpreter.api.transitions.TimedTransition;
import eu.compassresearch.core.interpreter.utility.Pair;

/**
 * This class
 * 
 * @author akm
 */
public abstract class AbstractInspectionVisitor extends
		QuestionAnswerCMLAdaptor<Context, Inspection>
{

	/**
	 * The behavior object that owns this visitor
	 */
	protected final CmlBehaviour owner;
	/**
	 * The parent visitor
	 */
	protected final QuestionAnswerCMLAdaptor<Context, Inspection> parentVisitor;

	protected CmlBehaviorFactory cmlBehaviorFactory;
	/**
	 * Interface that gives access to methods that access protected parts of a CmlBehaviour
	 */
	protected final VisitorAccess visitorAccess;
	/**
	 * Evaluator for expressions
	 */
	protected final QuestionAnswerCMLAdaptor<Context, Value> cmlExpressionVisitor = new CmlExpressionVisitor();

	/**
	 * Used for making random but deterministic decisions
	 */
	protected final Random rnd = new Random(CmlRuntime.randomSeed);

	/**
	 * @param ownerProcess
	 *            The behavior object that owns this visitor
	 * @param visitorAccess
	 *            Gives access to methods that access protected parts of a CmlBehaviour
	 * @param cmlBehaviorFactory 
	 * @param parentVisitor 
	 */
	public AbstractInspectionVisitor(CmlBehaviour ownerProcess,
			VisitorAccess visitorAccess, CmlBehaviorFactory cmlBehaviorFactory,
			QuestionAnswerCMLAdaptor<Context, Inspection> parentVisitor)
	{
		this.owner = ownerProcess;
		this.visitorAccess = visitorAccess;
		this.parentVisitor = parentVisitor;
		this.cmlBehaviorFactory = cmlBehaviorFactory;
	}

	/**
	 * Common Helpers
	 */

	protected CmlTransitionSet createTauTransitionWithTime(INode dstNode,
			String transitionText)
	{
		return new CmlTransitionSet(new TimedTransition(owner), new TauTransition(owner, dstNode, transitionText));
	}

	protected CmlTransitionSet createTauTransitionWithTime(INode dstNode)
	{
		return createTauTransitionWithTime(dstNode, null);
	}

	protected CmlTransitionSet createTauTransitionWithoutTime(INode dstNode)
	{
		return createTauTransitionWithoutTime(dstNode, null);
	}

	protected CmlTransitionSet createTauTransitionWithoutTime(INode dstNode,
			String transitionText)
	{
		return new CmlTransitionSet(new TauTransition(owner, dstNode, transitionText));
	}

	protected Inspection newInspection(CmlTransitionSet transitions,
			CmlCalculationStep step)
	{
		return new Inspection(new CmlTrace(owner.getTraceModel()), transitions, step);
	}

	protected void newTransitionEvent(TransitionEvent event)
	{
		visitorAccess.newTransitionEvent(event);
	}

	protected void clearLeftChild()
	{
		this.visitorAccess.setLeftChild(null);
	}

	protected void setLeftChild(INode node, BehaviourName name, Context question)
			throws AnalysisException
	{
		this.visitorAccess.setLeftChild(this.cmlBehaviorFactory.newCmlBehaviour(node, this.visitorAccess.getChildContexts(question).first, name, owner));
	}

	protected void setLeftChild(INode node, Context question)
			throws AnalysisException
	{
		this.visitorAccess.setLeftChild(this.cmlBehaviorFactory.newCmlBehaviour(node, this.visitorAccess.getChildContexts(question).first, owner));
	}

	protected void setLeftChild(CmlBehaviour child)
	{
		this.visitorAccess.setLeftChild(child);
	}

	protected void clearRightChild()
	{
		this.visitorAccess.setRightChild(null);
	}

	protected void setRightChild(CmlBehaviour child)
	{
		this.visitorAccess.setRightChild(child);
	}

	protected void setRightChild(INode node, BehaviourName name,
			Context question) throws AnalysisException
	{
		this.visitorAccess.setRightChild(this.cmlBehaviorFactory.newCmlBehaviour(node, this.visitorAccess.getChildContexts(question).second, name, owner));
	}

	protected void setRightChild(INode node, Context question)
			throws AnalysisException
	{
		this.visitorAccess.setRightChild(this.cmlBehaviorFactory.newCmlBehaviour(node, this.visitorAccess.getChildContexts(question).second, owner));
	}

	protected Pair<Context, Context> getChildContexts(Context context)
	{
		return this.visitorAccess.getChildContexts(context);
	}

	protected BehaviourName name()
	{
		return this.owner.getName();
	}

	protected List<CmlBehaviour> children()
	{
		return owner.children();
	}

	protected Pair<INode, Context> replaceWithChild(CmlBehaviour child)
	{
		this.visitorAccess.setLeftChild(child.getLeftChild());
		this.visitorAccess.setRightChild(child.getRightChild());
		return child.getNextState();
	}

	protected Value lookupName(ILexNameToken name, Context question)
			throws ValueException
	{
		if (name.getModule().equals(""))
		{
			return question.lookup(name);
		} else
		{
			String[] tokens = name.getModule().split("\\.");
			List<String> ids = new LinkedList<String>(Arrays.asList(tokens));
			ids.add(name.getName());
			Value val = question.lookup(new LexNameToken("", ids.get(0), name.getLocation()));
			return lookupName(val, ids.subList(1, ids.size()), question);
		}
	}

	protected Value lookupName(Value val, List<String> ids, Context question)
			throws ValueException
	{
		Value retVal = null;

		if (ids.size() == 0)
		{
			retVal = val;
		} else
		{

			String nextId = ids.get(0);
			// Value nextVal = question.check(new LexNameToken("",nextId,new LexLocation()));
			if (val.deref() instanceof RecordValue)
			{
				RecordValue recordVal = val.recordValue(question);
				Value fieldValue = recordVal.fieldmap.get(nextId);
				retVal = lookupName(fieldValue, ids.subList(1, ids.size()), question);
				;
			} else if (val.deref() instanceof ObjectValue)
			{
				ObjectValue objectVal = val.objectValue(question);
				retVal = lookupName(objectVal.get(new LexNameToken("", nextId, new LexLocation()), false), ids.subList(1, ids.size()), question);
			}
		}

		return retVal;
	}

	@Override
	public Inspection createNewReturnValue(INode node, Context question)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Inspection createNewReturnValue(Object node, Context question)
	{
		// TODO Auto-generated method stub
		return null;
	}

}