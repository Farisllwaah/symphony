package eu.compassresearch.core.interpreter.values;

import java.util.LinkedList;
import java.util.List;

import org.overture.interpreter.values.Value;

import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.core.interpreter.runtime.CMLContext;
import eu.compassresearch.core.interpreter.scheduler.CMLProcess;

public class ProcessValue extends Value {

	private CMLContext currentContext = null;
	private List<ACommunicationAction> offeredEvents = new LinkedList<ACommunicationAction>();
	private CMLProcess process = null;
	
	private boolean isReduced = false;
	
	private PAction reducedAction = null;
	//public  NameValuePairMap members = new NameValuePairMap();	
		
	public ProcessValue()
	{}
	
	public ProcessValue(List<ACommunicationAction> availableEvents, CMLContext currentContext)
	{
		this.offeredEvents = availableEvents;
		this.currentContext = currentContext;
	}
	
	public ProcessValue(ACommunicationAction availableEvent, CMLContext currentContext)
	{
		this.offeredEvents = new LinkedList<ACommunicationAction>();
		this.offeredEvents.add(availableEvent);
		this.currentContext = currentContext;
		
	}
	
	public ProcessValue(PAction action)
	{
		this.isReduced = true;
		this.reducedAction = action;
		if(this.reducedAction != null)
			this.offeredEvents.add((ACommunicationAction)action);
	}
		
	public ProcessValue(CMLProcess process, CMLContext currentContext)
	{
		this.process = process;
		this.currentContext = currentContext;	
	}
	
	public PAction getReducedAction() {
		return reducedAction;
	}

	public void setReducedAction(PAction reducedAction) {
		this.reducedAction = reducedAction;
	}

	public boolean isReduced() {
		return isReduced;
	}

	public void setReduced(boolean isReduced) {
		this.isReduced = isReduced;
	}
	
	public boolean isSkip()
	{
		return this.isReduced && this.reducedAction == null;
	}
	
	public CMLContext getCurrentContext() {
		return currentContext;
	}
	
	public List<ACommunicationAction> getOfferedEvents() {
		
		if (this.process != null)
			return this.process.WaitForEventOffer();
		else
			return offeredEvents;
	}

	public void setOfferedEvents(List<ACommunicationAction> offeredEvents) {
		this.offeredEvents = offeredEvents;
	}
			
	public CMLProcess getProcess()
	{
		return this.process;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "processValue";
	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		
		return 0;
	}

	@Override
	public String kind() {
		
		return toString();
	}

	@Override
	public Object clone() {
		
		return new ProcessValue();
	}

}
