package eu.compassresearch.core.analysis.modelchecker.ast.auxiliary;

import java.util.LinkedList;

import eu.compassresearch.core.analysis.modelchecker.ast.MCNode;
import eu.compassresearch.core.analysis.modelchecker.ast.definitions.MCAChannelDefinition;
import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCPVarsetExpression;
import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCVoidValue;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCAChannelType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCANamedInvariantType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCAProductType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCPCMLType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCVoidType;
import eu.compassresearch.core.analysis.modelchecker.visitors.NewCMLModelcheckerContext;

public class MCLieInFact implements MCNode {
	
	private MCCommEv commEvent;
	private MCPVarsetExpression setExp;
	
	
	public MCLieInFact(MCCommEv commEvent, MCPVarsetExpression setExp) {
		this.commEvent = commEvent;
		this.setExp = setExp;
	}

	public void prepareLieInFact(){
		NewCMLModelcheckerContext context = NewCMLModelcheckerContext.getInstance();
		MCAChannelDefinition chanDef = context.getChannelDefinition(this.commEvent.getName());
		LinkedList<TypeValue> typeValues = getTypeValues();

		if(typeValues.size() == 0){ //it is (probably an infinite type and must be instantiated by formula)
			MCCommEv ev = new MCCommEv(this.commEvent.getName(), this.commEvent.getParameters(), new MCVoidType());
			if(chanDef != null){
				if(!chanDef.isTyped()){
					ev.setValue(new MCVoidType());
				}else{ //probably the type is infinite
					
				}
			}
			MCLieInFact realLieIn = new MCLieInFact(ev, setExp);
			context.realLieInFacts.add(realLieIn);
		} else{
			for (TypeValue typeValue : typeValues) {
				//we need to generate many commev with the communicated values
				MCCommEv ev = new MCCommEv(this.commEvent.getName(), this.commEvent.getParameters(), typeValue);
				MCLieInFact realLieIn = new MCLieInFact(ev, setExp);
				context.realLieInFacts.add(realLieIn);
			}
		}

	}

	@Override
	public String toFormula(String option) {
		
		StringBuilder lieIn = new StringBuilder();
		NewCMLModelcheckerContext context = NewCMLModelcheckerContext.getInstance();
		
		//MCAChannelDefinition chanDef = context.getChannelDefinition(this.commEvent.getName());
		//LinkedList<TypeValue> typeValues = getTypeValues();
		//if(typeValues.size() == 0){ //it is (probably an infinite type and must be instantiated by formula)
			lieIn.append("  lieIn(");
			//MCCommEv ev = new MCCommEv(this.commEvent.getName(), this.commEvent.getParameters(), new MCVoidType());
			//if(chanDef != null){
			//	if(!chanDef.isTyped()){
			//		ev.setValue(new MCVoidType());
			//	}else{ //probably the type is infinite
					
			//	}
			//}
			lieIn.append(this.commEvent.toFormula(option));
			lieIn.append(",");
			lieIn.append(setExp.toFormula(option));
			lieIn.append(")");
		//} else{
		//	for (TypeValue typeValue : typeValues) {
		//		lieIn.append("  lieIn(");
				//we need to generate many commev with the communicated values
		//		MCCommEv ev = new MCCommEv(this.commEvent.getName(), this.commEvent.getParameters(), typeValue);
		//		lieIn.append(ev.toFormula(option));
		//		lieIn.append(",");
		//		lieIn.append(setExp.toFormula(option));
		//		lieIn.append(")");
		//		lieIn.append("\n");
		//	}
		//}
		
		return lieIn.toString();
	}

	
	private LinkedList<TypeValue> getTypeValues(){
		LinkedList<TypeValue> result = new LinkedList<TypeValue>();

		NewCMLModelcheckerContext context = NewCMLModelcheckerContext.getInstance();
		MCAChannelDefinition chanDef = context.getChannelDefinition(this.commEvent.getName());
		if(chanDef != null){
			MCPCMLType chanType = chanDef.getType();
			if(chanType instanceof MCAChannelType){
				chanType = ((MCAChannelType) chanType).getType();
				
				//MCAIntNumericBasicType, MCANatNumericBasicType are infinite, so we let formula to instantiate them
				
				//we check if named types have been previously defined and get all possible values for them
				if(chanType instanceof MCANamedInvariantType || chanType instanceof MCAProductType){ 
					TypeManipulator typeManipulator = TypeManipulator.getInstance();
					result = typeManipulator.getValues(chanType);
				}
			}
		}
		
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof MCLieInFact){
			result = this.commEvent.equals(((MCLieInFact) obj).getCommEvent()) 
					&& this.setExp.equals(((MCLieInFact) obj).getSetExp());
		}
		return result;
	}

	public MCCommEv getCommEvent() {
		return commEvent;
	}


	public void setCommEvent(MCCommEv commEvent) {
		this.commEvent = commEvent;
	}


	public MCPVarsetExpression getSetExp() {
		return setExp;
	}


	public void setSetExp(MCPVarsetExpression setExp) {
		this.setExp = setExp;
	}


	

	
}
