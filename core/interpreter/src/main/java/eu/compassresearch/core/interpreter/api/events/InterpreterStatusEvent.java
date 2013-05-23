package eu.compassresearch.core.interpreter.api.events;

import eu.compassresearch.core.interpreter.api.CmlInterpreter;
import eu.compassresearch.core.interpreter.api.CmlInterpreterState;
import eu.compassresearch.core.interpreter.utility.events.Event;

/**
 * This represents a change of CmlInterpreterState in a specific CmlInterpreter instance.
 * This event is fired whenever a CmlInterpreter changes its state.
 * @author akm
 *
 */
public class InterpreterStatusEvent extends Event<CmlInterpreter> {

	private final CmlInterpreterState status;

	public InterpreterStatusEvent(CmlInterpreter interpreter,CmlInterpreterState status)
	{
		super(interpreter);
		this.status = status;
	}
	
	public CmlInterpreterState getStatus() {
		return status;
	}
}
