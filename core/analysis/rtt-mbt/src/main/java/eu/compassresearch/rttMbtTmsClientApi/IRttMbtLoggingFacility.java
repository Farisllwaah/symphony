package eu.compassresearch.rttMbtTmsClientApi;

public interface IRttMbtLoggingFacility {
	public void addLogMessage(String consoleName, String msg);
	public void addErrorMessage(String consoleName, String msg);
}
