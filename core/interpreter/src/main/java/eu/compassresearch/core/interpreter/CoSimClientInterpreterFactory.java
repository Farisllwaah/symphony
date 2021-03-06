package eu.compassresearch.core.interpreter;

import java.util.List;

import org.overture.ast.definitions.PDefinition;

import eu.compassresearch.core.interpreter.api.CmlBehaviorFactory;
import eu.compassresearch.core.interpreter.api.CmlInterpreter;
import eu.compassresearch.core.interpreter.api.CmlInterpreterException;
import eu.compassresearch.core.interpreter.cosim.CoSimulationClient;

/**
 * Factory to be used for co-simulation when running as a client
 * 
 * @author kel
 */
public final class CoSimClientInterpreterFactory extends VanillaInterpreterFactory
{

	private CoSimulationClient client;

	public CoSimClientInterpreterFactory(CoSimulationClient client)
	{
		this.client = client;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * eu.compassresearch.core.interpreter.InterpreterFactory#setDefaultCmlBehaviourFactory(eu.compassresearch.core.
	 * interpreter.api.behaviour.CmlBehaviorFactory)
	 */
	@Override
	public void setDefaultCmlBehaviourFactory(CmlBehaviorFactory factory)
	{
		cmlBehaviorFactory = factory;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.compassresearch.core.interpreter.InterpreterFactory#newInterpreter(java.util.List)
	 */
	@Override
	public CmlInterpreter newInterpreter(List<PDefinition> definitions)
			throws CmlInterpreterException
	{
		CoSimClientInterpreter interpreter = new CoSimClientInterpreter(definitions, newDefaultConfig(), client);
		CmlContextFactory.configureDBGPReader(interpreter);
		return interpreter;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.compassresearch.core.interpreter.InterpreterFactory#newInterpreter(java.util.List,
	 * eu.compassresearch.core.interpreter.Config)
	 */
	@Override
	public CmlInterpreter newInterpreter(List<PDefinition> definitions,
			Config config) throws CmlInterpreterException
	{
		CoSimClientInterpreter interpreter = new CoSimClientInterpreter(definitions, config, client);
		CmlContextFactory.configureDBGPReader(interpreter);
		return interpreter;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.compassresearch.core.interpreter.InterpreterFactory#newDefaultConfig()
	 */
	@Override
	public Config newDefaultConfig()
	{
		return new Config(false, cmlBehaviorFactory);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.compassresearch.core.interpreter.InterpreterFactory#newDefaultConfig(boolean)
	 */
	@Override
	public Config newDefaultConfig(boolean filterTockEvents)
	{
		return new Config(filterTockEvents, cmlBehaviorFactory);
	}
}
