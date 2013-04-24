package eu.compassresearch.core.interpreter.cml;

import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.node.INode;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ValueException;

import eu.compassresearch.core.interpreter.events.CmlProcessStateObserver;
import eu.compassresearch.core.interpreter.events.CmlProcessTraceObserver;
import eu.compassresearch.core.interpreter.events.EventSource;
import eu.compassresearch.core.interpreter.util.Pair;

/**
 * This interfaces specifies a specific process behavior.
 * E.g: 
 * 	prefix : a -> P
 * 
 * 	CmlBehaviour.inspect() = {a}
 * 	CmlBehaviour.execute() :  trace: a
 * 	CmlBehaviour.inspect() = alpha(P)
 *   
 * @author akm
 *
 */
public interface CmlBehaviour //extends Transactable //, CmlBehaviour 
{
	/**
	 * Executes the behaviour of this process
	 * @return
	 */
	public void execute(CmlSupervisorEnvironment supervisor) throws AnalysisException;
	
	/**
	 * Returns the immediate alphabet of the process, meaning the next possible cml event including tau
	 * @return The immediate alphabet of the process
	 */
	public CmlAlphabet inspect();
	
	/**
	 * 
	 * @return The current supervisor of this process
	 */
	public CmlSupervisorEnvironment supervisor();
	
	//public Reason abortReason();
	public void setAbort(Reason reason);
	
	/**
	 * Returns the current execution state of the process
	 * @return The current context
	 */
	public Pair<INode,Context> getExecutionState();
	public void replaceState(Context context) throws ValueException;
	//public Pair<INode,Context> getPostCondition();
	//public setPostCondition(Pair<INode,Context> postcondition);
	
	/**
	 * Name of the process
	 * @return The name of the process
	 */
	public LexNameToken name();
	
	/**
	 * This constructs a string representing the next execution step of this process
	 * @return
	 */
	public String nextStepToString();
	
	// Process Graph/Representation related methods
	/**
	 * The level of this object in the process network.
	 * @return return 0 if this is the root, 1 if this is a child of the root etc.
	 */
	public long level();
	public CmlBehaviour parent();
	public List<CmlBehaviour> children();
	public CmlBehaviour getLeftChild();
	public CmlBehaviour getRightChild();
//	public CMLAlphabet childInspectedAlphabet(CMLProcessNew child);
//	public void setChildInspectedAlphabet(CMLProcessNew child, CMLAlphabet alpha);
	//public boolean hasChild(CMLProcess child, boolean recursive);
	public boolean hasChildren();
	
	/**
	 * Process state methods 
	 */
	
	/**
	 * Determines whether the process is started
	 * @return true if the process has been started, meaning the start method has been invoked
	 * else false
	 */
	public boolean started();
	public boolean finished();
	
	/**
	 * Determines whether the process is in a waiting state.
	 * @return true if the process is either waiting for a child or an event to occur else false
	 */
	public boolean waiting();
	
	/**
	 * Determines whether this process is deadlocked
	 * @return true if the process is deadlocked else false
	 */
	public boolean deadlocked();
	
	/**
	 * @return The current state of the process
	 */
	public CmlProcessState getState();

	/**
	 * Register or unregister for the State Changed event
	 * @return The appropriate EventSource for event registration
	 */
	public EventSource<CmlProcessStateObserver> onStateChanged();
	
	/**
	 * Denotational Semantics Information
	 */
	public CmlTrace getTraceModel();
	//public CSPFailures failuresModel();
	//public CSPDivergencies divergenciesModel();
	//public CSPFailuresDivergencies failuresDivergenciesModel();
	
	/**
	 * Register or unregister for the State Changed event
	 * @return The appropriate EventSource for event registration
	 */
	public EventSource<CmlProcessTraceObserver> onTraceChanged();
	
}
