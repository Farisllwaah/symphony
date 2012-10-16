package eu.compassresearch.core.interpreter.scheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.core.interpreter.runtime.ChannelEvent;

public class SynchronousParallelismProcess implements CMLProcess
{
	private CMLProcess leftProcess; 
	private CMLProcess rightProcess;
	private Set<String> channelSet;
		
	public SynchronousParallelismProcess(CMLProcess leftProcess, CMLProcess rightProcess, Set<String> channelSet)
	{
		this.leftProcess = leftProcess;
		this.rightProcess = rightProcess;
		this.channelSet = channelSet;
	}
	
	//private Map<String,>
		
	@Override
	synchronized public List<ACommunicationAction> WaitForEventOffer() {
				
		List<ACommunicationAction> leftActions = leftProcess.WaitForEventOffer();
		List<ACommunicationAction> rigthActions = rightProcess.WaitForEventOffer();
		
		List<ACommunicationAction> offeredEvents = new LinkedList<ACommunicationAction>();
		
		for(ACommunicationAction a : leftActions)
		{
			String ev = a.getIdentifier().getName();
			//if the event is not in the chanset, no sync is necessary
			if(!this.channelSet.contains(ev))
				offeredEvents.add(a);
			//if the event is in the chanset, sync is necessary and the event needs to be offered by the other process
			else{
				
				
				
			}
		}
				
		return offeredEvents;
	}

	@Override
	public void start() {
		
		this.leftProcess.start();
		this.rightProcess.start();
	}

	@Override
	synchronized public boolean isSkip() {
		return this.leftProcess.isSkip() && this.rightProcess.isSkip();
	}

	@Override
	synchronized public void eventOccured(ChannelEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getChannelSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemainingInterpretationState(boolean expand) {
		// TODO Auto-generated method stub
		return null;
	}
}