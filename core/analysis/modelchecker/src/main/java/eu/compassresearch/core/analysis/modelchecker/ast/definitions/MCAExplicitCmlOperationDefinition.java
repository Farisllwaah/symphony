package eu.compassresearch.core.analysis.modelchecker.ast.definitions;

import java.util.LinkedList;

import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.statements.AAssignmentStm;

import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.core.analysis.modelchecker.ast.MCNode;
import eu.compassresearch.core.analysis.modelchecker.ast.actions.MCASingleGeneralAssignmentStatementAction;
import eu.compassresearch.core.analysis.modelchecker.ast.actions.MCAStmAction;
import eu.compassresearch.core.analysis.modelchecker.ast.actions.MCPAction;
import eu.compassresearch.core.analysis.modelchecker.ast.auxiliary.Binding;
import eu.compassresearch.core.analysis.modelchecker.ast.auxiliary.ExpressionEvaluator;
import eu.compassresearch.core.analysis.modelchecker.ast.auxiliary.MCOperationCall;
import eu.compassresearch.core.analysis.modelchecker.ast.auxiliary.MCPreOpTrue;
import eu.compassresearch.core.analysis.modelchecker.ast.auxiliary.MCStringType;
import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCAVariableExp;
import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCPCMLExp;
import eu.compassresearch.core.analysis.modelchecker.ast.pattern.MCPCMLPattern;
import eu.compassresearch.core.analysis.modelchecker.ast.statements.MCAActionStm;
import eu.compassresearch.core.analysis.modelchecker.ast.statements.MCAAssignmentStm;
import eu.compassresearch.core.analysis.modelchecker.ast.statements.MCAIdentifierStateDesignator;
import eu.compassresearch.core.analysis.modelchecker.ast.statements.MCAUnresolvedStateDesignator;
import eu.compassresearch.core.analysis.modelchecker.ast.statements.MCPCMLStm;
import eu.compassresearch.core.analysis.modelchecker.ast.statements.MCPStateDesignator;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCANamedInvariantType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCPCMLType;
import eu.compassresearch.core.analysis.modelchecker.visitors.NewCMLModelcheckerContext;

public class MCAExplicitCmlOperationDefinition implements
		MCSCmlOperationDefinition {

	private String name;
	private MCPCMLStm body;
	private MCPCMLExp precondition;
	private MCPCMLExp postcondition;
	private MCAExplicitFunctionDefinition predef;
	private MCAExplicitFunctionDefinition postdef;
	private LinkedList<MCPCMLPattern> paramPatterns = new LinkedList<MCPCMLPattern>();
	private MCAStateDefinition state;
	private MCPCMLType actualResult;
	private MCPAction parentAction;
	
	public MCAExplicitCmlOperationDefinition(String name, MCPCMLStm body,
			MCPCMLExp precondition, MCPCMLExp postcondition,
			MCAExplicitFunctionDefinition predef,
			MCAExplicitFunctionDefinition postdef,
			LinkedList<MCPCMLPattern> paramPatterns,
			MCAStateDefinition state, MCPCMLType actualResult) {
		
		this.name = name;
		this.body = body;
		this.precondition = precondition;
		this.postcondition = postcondition;
		this.predef = predef;
		this.postdef = postdef;
		this.paramPatterns = paramPatterns;
		this.state = state;
		this.actualResult = actualResult;
		this.parentAction = new MCOperationCall(this.name, null, paramPatterns);
	}

	

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof MCAExplicitCmlOperationDefinition){
			result = this.name.equals(((MCAExplicitCmlOperationDefinition) obj).getName())
					&& this.paramPatterns.equals(((MCAExplicitCmlOperationDefinition) obj).getParamPatterns());
		}
		return result;
	}



	@Override
	public String toFormula(String option) {
		NewCMLModelcheckerContext context = NewCMLModelcheckerContext.getInstance();
		StringBuilder result = new StringBuilder();
		
		result.append("  operationDef(");
		result.append("\"" + this.name + "\"");
		result.append(",");
		
		ExpressionEvaluator evaluator = ExpressionEvaluator.getInstance();
		MCPCMLType argsType = null;
		MCPCMLType paramType = evaluator.instantiateMCTypeFromPatterns(this.paramPatterns);
		result.append(paramType.toFormula(MCNode.NAMED));
		result.append(",");
		result.append("st");
		result.append(",");
		result.append("st_");
		result.append(") :- ");
		result.append("State(st,");
		//the parent action of an operation must be an operation call in the specification
		//its translation must be generic so many calls can reuse the body of an operation
		//changing only its parameters
		//create a operation call and use generic translation
		//result.append(this.parentAction.toFormula(option));
		MCOperationCall opCall = new MCOperationCall(this.name, null, paramPatterns);
		result.append(opCall.toFormula(NAMED));
		
		result.append(")");
		result.append(",");
		result.append("st = ");
		result.append(context.maximalBinding.toFormula(MCNode.NAMED));
		result.append(",");
		result.append("st_ = ");
		Binding maximalCopy = context.maximalBinding.copy();
		MCPCMLStm body = this.body;
		MCAAssignmentStm assignmentBody = null;
		
		if(body instanceof MCAAssignmentStm){
			assignmentBody = (MCAAssignmentStm) body;
		}else if(body instanceof MCAActionStm){
			MCPAction innerAction = ((MCAActionStm) body).getAction();
			if(innerAction instanceof MCAStmAction){
				MCPCMLStm stm = ((MCAStmAction) innerAction).getStatement();
				if(stm instanceof MCAAssignmentStm){
					assignmentBody = (MCAAssignmentStm) stm;
				}
			}
		}
		
		
		
		String newValueVarName = "";
		
		if(assignmentBody != null){
			MCPStateDesignator stateDesignator = assignmentBody.getTarget();
			if(stateDesignator instanceof MCAUnresolvedStateDesignator){
				MCPCMLExp path = ((MCAUnresolvedStateDesignator) stateDesignator).getPath();
				if(path instanceof MCAVariableExp){
					String varName = path.toFormula(MCNode.NAMED);
					newValueVarName = varName + "_";
					MCPCMLExp newVarValue = new MCAVariableExp(newValueVarName);
					maximalCopy.updateBinding(varName,newVarValue);
					result.append(maximalCopy.toFormula(MCNode.DEFAULT)); 
				}
			} else if (stateDesignator instanceof MCAIdentifierStateDesignator){
				String varName = ((MCAIdentifierStateDesignator) stateDesignator).getName();
				newValueVarName = varName + "_";
				MCPCMLExp newVarValue = new MCAVariableExp(newValueVarName);
				maximalCopy.updateBinding(varName,newVarValue);
				result.append(maximalCopy.toFormula(MCNode.DEFAULT));
			}
		}
		
		//THE EXPRESSION OF THE ASSIGNMENT
		if(assignmentBody.getExpression() != null){
			result.append(", ");
			result.append(newValueVarName + " = " + assignmentBody.getExpression().toFormula(option)); //expression assignment
		}
		result.append(".");
		result.append("\n");
		
		if(this.precondition != null){
			//convert precondition
		}else{
			//we assume that all preconditions are true for the moment
			MCPreOpTrue preOp = new MCPreOpTrue(name, paramPatterns);
			result.append(preOp.toFormula(option));
		}
		result.append("\n");
		return result.toString();
	}


	public MCPCMLExp getPrecondition() {
		return precondition;
	}


	public void setPrecondition(MCPCMLExp precondition) {
		this.precondition = precondition;
	}


	public MCPCMLExp getPostcondition() {
		return postcondition;
	}


	public void setPostcondition(MCPCMLExp postcondition) {
		this.postcondition = postcondition;
	}


	public MCAExplicitFunctionDefinition getPredef() {
		return predef;
	}


	public void setPredef(MCAExplicitFunctionDefinition predef) {
		this.predef = predef;
	}


	public MCAExplicitFunctionDefinition getPostdef() {
		return postdef;
	}


	public void setPostdef(MCAExplicitFunctionDefinition postdef) {
		this.postdef = postdef;
	}


	
	public LinkedList<MCPCMLPattern> getParamPatterns() {
		return paramPatterns;
	}


	public void setParamPatterns(LinkedList<MCPCMLPattern> paramPatterns) {
		this.paramPatterns = paramPatterns;
	}


	public MCAStateDefinition getState() {
		return state;
	}


	public void setState(MCAStateDefinition state) {
		this.state = state;
	}


	public MCPCMLType getActualResult() {
		return actualResult;
	}


	public void setActualResult(MCPCMLType actualResult) {
		this.actualResult = actualResult;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	

	public MCPCMLStm getBody() {
		return body;
	}


	public void setBody(MCPCMLStm body) {
		this.body = body;
	}


	public MCPAction getParentAction() {
		return parentAction;
	}


	public void setParentAction(MCPAction parentAction) {
		this.parentAction = parentAction;
	}

	

}
