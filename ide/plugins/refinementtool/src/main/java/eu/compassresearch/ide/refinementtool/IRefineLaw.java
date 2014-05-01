package eu.compassresearch.ide.refinementtool;

import java.util.List;
import java.util.Map;

import org.overture.ast.node.INode;

public interface IRefineLaw {

	public String getName();
	public boolean isApplicable(INode node);
	public Refinement apply(Map<String, String> metas, INode node);
	public List<String> getMetaNames();
}
