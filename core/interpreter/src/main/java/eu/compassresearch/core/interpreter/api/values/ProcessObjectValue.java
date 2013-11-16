package eu.compassresearch.core.interpreter.api.values;

import java.util.LinkedList;

import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.types.AClassType;
import org.overture.interpreter.values.CPUValue;
import org.overture.interpreter.values.NameValuePairMap;
import org.overture.interpreter.values.ObjectValue;

import eu.compassresearch.ast.definitions.AProcessDefinition;

public class ProcessObjectValue extends ObjectValue
{

	private AProcessDefinition processDefinition = null;
	private PExp invariantExpression = null;

	public ProcessObjectValue(AProcessDefinition processDefinition,
			NameValuePairMap members, ObjectValue creator)
	{
		super(CmlToVdmConverter.createClassType(processDefinition), members, new LinkedList<ObjectValue>(), CPUValue.vCPU, creator);
		this.processDefinition = processDefinition;
	}

	public ProcessObjectValue(AProcessDefinition processDefinition,
			NameValuePairMap members, ObjectValue creator, PExp invExp)
	{
		this(processDefinition, members, creator);
		this.setInvariantExpression(invExp);
	}
	
	public ProcessObjectValue(SClassDefinition processDefinition,
			NameValuePairMap members, ObjectValue creator, PExp invExp)
	{
		super((AClassType)processDefinition.getType(), members, new LinkedList<ObjectValue>(), CPUValue.vCPU, creator);
		this.setInvariantExpression(invExp);
	}

	public ProcessObjectValue(AProcessDefinition processDefinition,
			ObjectValue creator)
	{
		super(CmlToVdmConverter.createClassType(processDefinition), new NameValuePairMap(), new LinkedList<ObjectValue>(), CPUValue.vCPU, creator);
		this.processDefinition = processDefinition;
	}

	public AProcessDefinition getProcessDefinition()
	{
		return processDefinition;
	}

	public PExp getInvariantExpression()
	{
		return invariantExpression;
	}

	public boolean hasInvariant()
	{
		return invariantExpression != null;
	}

	private void setInvariantExpression(PExp invariantExpression)
	{
		this.invariantExpression = invariantExpression;
	}

}
