package eu.compassresearch.core.analysis.modelchecker.ast.types;

public class MCANamedInvariantType implements MCPCMLType {

	private String name;
	
	
	public MCANamedInvariantType(String name) {
		super();
		this.name = name;
	}

	@Override
	public String toFormula(String option) {
		return this.name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}
