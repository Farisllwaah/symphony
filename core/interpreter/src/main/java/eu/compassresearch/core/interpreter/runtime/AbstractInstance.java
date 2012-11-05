package eu.compassresearch.core.interpreter.runtime;

import java.util.LinkedList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.node.INode;

import eu.compassresearch.core.interpreter.cml.CMLAlphabet;
import eu.compassresearch.core.interpreter.cml.CMLBehaviourSignal;
import eu.compassresearch.core.interpreter.cml.CMLProcess;
import eu.compassresearch.core.interpreter.cml.CMLSupervisorEnvironment;
import eu.compassresearch.core.interpreter.cml.ProcessState;
import eu.compassresearch.core.interpreter.eval.AbstractEvaluator;

public abstract class AbstractInstance<T extends INode> extends AbstractEvaluator<T>
		implements CMLProcess {
	
	protected ProcessState state;
	protected List<CMLProcess> children = new LinkedList<CMLProcess>();
	protected CMLProcess parent;
	protected CMLSupervisorEnvironment env;

	public AbstractInstance(CMLProcess parent)
	{
		state = ProcessState.INITIALIZED;
		this.parent = parent;
	}
		
	
	@Override
	public CMLSupervisorEnvironment supervisor() {
		return env;
	}

	@Override
	public CMLProcess parent() {
		return parent;
	}

	@Override
	public List<CMLProcess> children() {
		return children;
	}

	@Override
	public boolean hasChildren() {
		return children.size() > 0;
	}

	@Override
	public boolean started() {
		return getState() == ProcessState.RUNNING || 
				getState() == ProcessState.RUNNABLE ||
				getState() == ProcessState.WAIT;
	}

	@Override public boolean waiting() {
		return getState() == ProcessState.WAIT;
	}
	
	
	@Override
	public boolean running() {
		return getState() == ProcessState.RUNNING;
	}

	@Override
	public boolean finished() {
		return getState() == ProcessState.FINISHED;
	}

	@Override
	public boolean deadlocked() {
		return false;
	}

}