package org.overture.interpreter.values;

import java.util.Map.Entry;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.lex.Dialect;
import org.overture.ast.types.PType;
import org.overture.config.Settings;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.scheduler.SharedStateListner;

import eu.compassresearch.core.interpreter.runtime.DelayedWriteContext;

/**
 * A wrapper for updatable values delaying updates to the original value. A transaction is made when a update is
 * performed and not committed before {@link DelayedWriteContext#writeChanges()}
 * 
 * @author kel
 */
public class DelayedUpdatableWrapper extends UpdatableValue
{
	/**
	 * serial
	 */
	private static final long serialVersionUID = 1L;
	final Value original;

	ILexLocation newLoc = null;
	Context newContext = null;

	/**
	 * Value maps obtained through {@link DelayedUpdatableWrapper#mapValue(Context)}
	 */
	ValueMap obtainedMaps = null;

	protected DelayedUpdatableWrapper(Value value, ValueListenerList listeners,
			PType type)
	{
		super(value, listeners, type);
		this.original = value;
	}

	public DelayedUpdatableWrapper(UpdatableValue val)
	{
		this(val, val.listeners, val.restrictedTo);
	}

	protected DelayedUpdatableWrapper(Value value, ValueListenerList listeners,
			PType type, Value original, ILexLocation newLoc, Context newContext)
	{
		super(value, listeners, type);
		this.original = original;
		this.newLoc = newLoc;
		this.newContext = newContext;
	}

	@Override
	public void set(ILexLocation location, Value newval, Context ctxt)
			throws AnalysisException
	{
		this.newLoc = location;
		this.newContext = ctxt;
		// Anything with structure added to an UpdateableValue has to be
		// updatable, otherwise you can "freeze" part of the substructure
		// such that it can't be changed. And we have to set the listeners
		// to be "our" listeners, regardless of any it had before.

		synchronized (this)
		{
			value = newval.getUpdatable(listeners);
			value = ((UpdatableValue) value).value; // To avoid nested updatables

			if (restrictedTo != null)
			{
				value = value.convertTo(restrictedTo, ctxt);
			}
		}

		// Experimental hood added for DESTECS
		if (Settings.dialect == Dialect.VDM_RT)
		{
			SharedStateListner.variableChanged(this, location);
		}

		// The listeners are outside the sync because they have to lock
		// the object they notify, which can be holding a lock on this one.

		if (listeners != null)
		{
			listeners.changedValue(location, value, ctxt);
		}

		/* Useful for debugging, but we don't want to ship with this on. */
		// System.err.println("Setting value(" + toShortString(10) + ") to "
		// + newval);
	}

	/**
	 * Write transactional value to the original value
	 * 
	 * @throws ValueException
	 * @throws AnalysisException
	 */
	public void set() throws ValueException, AnalysisException
	{
		original.set(newLoc, value, newContext);
	}

	@Override
	public synchronized ValueMap mapValue(Context ctxt) throws ValueException
	{
		ValueMap m = null;
		if (obtainedMaps == null)
		{
			m = super.mapValue(ctxt);
		} else
		{
			return obtainedMaps;
		}
		if (m != null)
		{
			ValueMap mWrapper = new ValueMap();

			for (Entry<Value, Value> entry : m.entrySet())
			{
				if (entry.getValue() instanceof UpdatableValue)
				{
					mWrapper.put(entry.getKey(), new DelayedUpdatableWrapper((UpdatableValue) entry.getValue()));
				} else
				{
					mWrapper.put(entry.getKey(), entry.getValue());
				}
			}
			obtainedMaps = mWrapper;
			this.value = new MapValue(obtainedMaps);
			return obtainedMaps;
		}

		return m;
	}

	@Override
	public synchronized Object clone()
	{
		return new DelayedUpdatableWrapper((Value) value.clone(), listeners, restrictedTo, original, newLoc, newContext);
	}

}
