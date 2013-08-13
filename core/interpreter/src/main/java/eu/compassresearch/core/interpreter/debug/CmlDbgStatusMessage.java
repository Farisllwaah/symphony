package eu.compassresearch.core.interpreter.debug;

import eu.compassresearch.core.interpreter.api.CmlInterpreterState;
import eu.compassresearch.core.interpreter.api.InterpreterStatus;
import eu.compassresearch.core.interpreter.utility.messaging.Message;
import eu.compassresearch.core.interpreter.utility.messaging.MessageType;


public class CmlDbgStatusMessage extends Message {

	private InterpreterStatus interpreterStatus;
	
	//dummy for serialization
	private CmlDbgStatusMessage(){}
	
	public CmlDbgStatusMessage(InterpreterStatus interpreterStatus)
	{
		this.interpreterStatus = interpreterStatus;
	}
	
	public CmlDbgStatusMessage(CmlInterpreterState state)
	{
		this.interpreterStatus = new InterpreterStatus(state);
	}
	
	public CmlInterpreterState getStatus() {
		if(this.interpreterStatus != null)
			return this.interpreterStatus.getInterpreterState();
		else 
			return CmlInterpreterState.TERMINATED;
	}
	
	public InterpreterStatus getInterpreterStatus() {
		return interpreterStatus;
	}

	@Override
	public String toString() {
		return CmlDbgStatusMessage.class.getSimpleName() + System.lineSeparator() +
			   this.interpreterStatus;
				
	}

	@Override
	public MessageType getType() {
		return MessageType.STATUS;
	}

	@Override
	public String getKey() {
		return getStatus().toString();
	}
}
