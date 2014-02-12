/**
 * 
 */
package eu.compassresearch.ide.faulttolerance.jobs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import eu.compassresearch.ide.faulttolerance.Activator;

/**
 * @author Andr&eacute; Didier (<a href=
 *         "mailto:alrd@cin.ufpe.br?Subject=Package eu.compassresearch.ide.faulttolerance.jobs, class FaultToleranceVerificationStatus"
 *         >alrd@cin.ufpe.br</a>)
 * 
 */
public class FaultToleranceVerificationStatus extends Status {

	private final FaultToleranceVerificationResults results;

	public FaultToleranceVerificationStatus(
			FaultToleranceVerificationResults results) {
		super(getSeverity(results), Activator.ID, getMessage(results), results
				.getFirstException());
		this.results = results;
	}

	private static String getMessage(FaultToleranceVerificationResults results) {
		if (results.hasException()) {
			return results.getExceptionsLocalizedMessage();
		}
		return null;
	}

	private static int getSeverity(FaultToleranceVerificationResults results) {
		return results.hasException() ? IStatus.ERROR : IStatus.OK;
	}

	public FaultToleranceVerificationResults getResults() {
		return results;
	}
}