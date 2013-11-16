package eu.compassresearch.core.interpreter;

import org.overture.ast.intf.lex.ILexIdentifierToken;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexLocation;
import org.overture.ast.node.INode;

import eu.compassresearch.ast.lex.CmlLexNameToken;
import eu.compassresearch.core.interpreter.utility.LocationExtractor;

class NamespaceUtility
{
	public static ILexNameToken createSimpleName(ILexIdentifierToken id)
	{
		return new CmlLexNameToken("", id.getName(), id.getLocation(), false, true);
	}

	public static ILexNameToken createChannelName(ILexIdentifierToken id)
	{
		return new CmlLexNameToken("|CHANNELS|", id.getName(), id.getLocation(), false, true);
	}

	public static ILexNameToken createChansetName(ILexIdentifierToken id)
	{
		return new CmlLexNameToken("|CHANSET|", id.getName(), id.getLocation(), false, true);
	}

	public static ILexNameToken getStartTimeName()
	{
		return new CmlLexNameToken("|TIMEOUT|", "STARTTIME", new LexLocation());
	}

	public static ILexNameToken getSeqForName()
	{
		return new CmlLexNameToken("|FORSEQ|", "v", new LexLocation());
	}

//	public static ILexNameToken getVarExpContextName()
//	{
//		return new CmlLexNameToken("|VARSET|", "namesetExpression", new LexLocation());
//	}

	public static ILexNameToken getReplicationNodeName(INode node)
	{
		// The name of the value holding the state of the remaining values of the replication
		ILexLocation loc = LocationExtractor.extractLocation(node);
		return new CmlLexNameToken("|REPLICATION|", loc.toShortString(), loc);
	}
	
//	public static ILexNameToken ReturnValueName()
//	{
//		return new CmlLexNameToken("|CALL|", "RETURN", new LexLocation());
//	}
}
