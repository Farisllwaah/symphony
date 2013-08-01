package eu.compassresearch.ide.core.resources;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.overture.ast.node.INode;
import org.overture.ide.core.resources.IVdmSourceUnit;

import eu.compassresearch.ast.program.PSource;

public interface ICmlModel extends IAdaptable
{
	// IVdmModel getVdmModel();

	// public abstract void setRootElementList(List<T> rootElementList);

	@Deprecated
	public abstract List<PSource> getAstSource();

	public abstract List<INode> getAst();

	public abstract Date getCheckedTime();

	/**
	 * Check if the model has been type checked without errors
	 * 
	 * @return true if no TC errors
	 */
	public abstract boolean isTypeCorrect();

	/**
	 * Checks if a the model has been type checked. This is not the same as it has no errors.
	 * 
	 * @return true if the model has been TC, this does not reveal if the model is TC ok for that see
	 *         <b>isTypeCorrect</b>
	 */
	public abstract boolean isTypeChecked();

	/***
	 * Updates the local list with a new Definition if it already exists the old one is replaced
	 * 
	 * @param module
	 *            the new definition
	 */
	// @SuppressWarnings("unchecked")
	// public abstract void update(List<T> modules);

	/***
	 * Check if any definition in the list has the file as source location
	 * 
	 * @param file
	 *            The file which should be tested against all definitions in the list
	 * @return true if the file has a definition in the list
	 */
	public abstract boolean hasFile(File file);

	public abstract boolean hasFile(IVdmSourceUnit file);

	public abstract boolean hasFile(ICmlSourceUnit file);

	// public abstract void setParseCorrect(String file, Boolean isParseCorrect);

	public abstract boolean isParseCorrect();

	public abstract boolean exists();

	// public abstract IVdmModel filter(IFile file);

	public abstract ICmlSourceUnit getSourceUnit(IFile file);

	public abstract void clean();

	public abstract List<ICmlSourceUnit> getSourceUnits();

	// public abstract VdmModelWorkingCopy getWorkingCopy();

	/**
	 * Refresh source unit (reparse, will require type check again then)
	 * 
	 * @param completeRefresh
	 *            true to reparse all source units. False only to reparse source unit with empty parse lists
	 * @param monitor
	 *            null or a progress monitor
	 */
	public void refresh(boolean completeRefresh, IProgressMonitor monitor);

	// public abstract void remove(IVdmSourceUnit iVdmSourceUnit);
	//
	// /**
	// * Returns if any working copies has been issued from the current model. If this returns true
	// * any changes to the model might be overridden when a working copy is committed.
	// * @return returns true if working copies has been issued
	// */
	// boolean hasWorkingCopies();

	/**
	 * Check if the internal state has a certain attribute name defined
	 * 
	 * @param attributeName
	 *            the name to check for
	 * @return true if the name exists
	 */
	public boolean hasAttribute(String attributeName);

	/**
	 * Gets an internal state object defined by the attribute name.<br/>
	 * The return type is defined by the class it is called with.
	 * <p/>
	 * Note that the intension is that PLUG_IN ids should be used as the attribyteName
	 * 
	 * @param attributeName
	 *            a plugin id
	 * @param returnClassType
	 *            the class type which should be returned
	 * @return if the attribute is defined then the state object is returned otherwise null
	 */
	public <K> K getAttribute(String attributeName, Class<K> returnClassType);

	/**
	 * Sets an internal state object using attribute name.<br/>
	 * If a state already exists with the attributename then it will be overwritten.
	 * <p/>
	 * Note that the intension is that PLUG_IN ids should be used as the attribyteName
	 * 
	 * @param attributeName the plugin id to store the state under
	 * @param value the state to be stored
	 */
	public <K> void setAttribute(String attributeName, K value);
}