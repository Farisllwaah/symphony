package eu.compassresearch.ide.refinementtool.laws;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;

import eu.compassresearch.core.analysis.pog.obligations.CmlProofObligation;
import eu.compassresearch.ide.refinementtool.IRefineLaw;
import eu.compassresearch.ide.refinementtool.Refinement;
import eu.compassresearch.ide.refinementtool.maude.MaudePrettyPrinter;
import eu.compassresearch.ide.refinementtool.maude.MaudeRefineInfo;
import eu.compassresearch.ide.refinementtool.maude.MaudeRefiner;

public class MaudeRefineLaw implements IRefineLaw {

	private MaudeRefineInfo minfo;
	private MaudeRefiner mref;
	
	public MaudeRefineLaw() {
		
	}
	
	public MaudeRefineLaw(MaudeRefineInfo minfo, MaudeRefiner mref) {
		this.minfo = minfo;
		this.mref = mref;
	}

	@Override
	public String getName() {
		return minfo.desc();
	}

	@Override
	public String getDetail() {
		return minfo.law();
	}

	@Override
	public boolean isApplicable(INode node) {
		return true;
	}

	@Override
	public Refinement apply(Map<String, String> metas, INode node, int offset) {
		MaudePrettyPrinter mpp = new MaudePrettyPrinter();
		
	    String cml = "Skip";
		
		try {
			cml = node.apply(mpp, 0);
		} catch (AnalysisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<CmlProofObligation> ps = new LinkedList<CmlProofObligation>();
		
		return new Refinement(mref.applyLaw(cml, minfo), ps);
	}

	@Override
	public List<String> getMetaNames() {
		return new LinkedList<String>();
	}

}
