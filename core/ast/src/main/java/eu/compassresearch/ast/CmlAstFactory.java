package eu.compassresearch.ast;

import java.util.ArrayList;
import java.util.List;

import org.overture.ast.definitions.PDefinition;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexIdentifierToken;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.intf.lex.ILexNameToken;

import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.definitions.AActionClassDefinition;
import eu.compassresearch.ast.definitions.AActionDefinition;
import eu.compassresearch.ast.lex.LexNameToken;

public class CmlAstFactory extends AstFactory
{
	static int actionClassIndex = 0;

	public static AActionClassDefinition newAActionClassDefinition(
			ILexLocation loc, List<PDefinition> members)
	{
		ILexNameToken className = new LexNameToken("", "$actionClass"
				+ actionClassIndex++, loc);
		List<? extends ILexNameToken> superclasses = new ArrayList<ILexNameToken>();

		AActionClassDefinition result = new AActionClassDefinition();
		initClassDefinition(result, className, superclasses, members);
//		result.setActionDesinitions(actionDefs);

		return result;
	}
	
	public static AActionDefinition newAActionDefinition(ILexIdentifierToken identifier, PAction action)
	{
		 AActionDefinition adef = new AActionDefinition();
         adef.setName(new LexNameToken("", identifier));
         adef.setAction(action);
//         adef.setDeclarations(parametrisationList);
         return adef;
	}
}