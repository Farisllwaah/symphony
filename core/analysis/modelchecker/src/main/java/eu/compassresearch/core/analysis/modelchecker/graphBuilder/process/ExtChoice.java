package eu.compassresearch.core.analysis.modelchecker.graphBuilder.process;


public class ExtChoice extends Choice {
	
	
	public ExtChoice(Process firstProcess, Process secondProcess2) {
	   super(firstProcess, secondProcess2);
	}
	
	@Override
	public String toString() {
		return "(" + firstProcess.toString() + ")" + " [] " + "(" + secondProcess.toString() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof ExtChoice) {
			result = this.getFirstProcess().equals(((ExtChoice) obj).getFirstProcess()) 
					 && this.getSecondProcess().equals(((ExtChoice) obj).getSecondProcess());
		}
		return result;
	}
	
	@Override
	public boolean isDeadlock(){
		return false;
	}

}
