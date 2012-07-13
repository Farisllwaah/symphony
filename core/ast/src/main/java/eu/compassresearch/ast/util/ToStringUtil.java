package eu.compassresearch.ast.util;

import java.util.List;
import java.util.Vector;

import eu.compassresearch.ast.definitions.AEqualsDefinition;
import eu.compassresearch.ast.definitions.AExplicitFunctionFunctionDefinition;
import eu.compassresearch.ast.definitions.AExplicitOperationOperationDefinition;
import eu.compassresearch.ast.definitions.AImplicitFunctionFunctionDefinition;
import eu.compassresearch.ast.definitions.AImplicitOperationOperationDefinition;
import eu.compassresearch.ast.definitions.AImportedDefinition;
import eu.compassresearch.ast.definitions.AInheritedDefinition;
import eu.compassresearch.ast.definitions.AMultiBindListDefinition;
import eu.compassresearch.ast.definitions.ARenamedDefinition;
import eu.compassresearch.ast.definitions.AValueDefinition;
import eu.compassresearch.ast.definitions.PDefinition;
import eu.compassresearch.ast.definitions.SClassDefinition;
import eu.compassresearch.ast.lex.LexLocation;
import eu.compassresearch.ast.lex.LexNameList;
import eu.compassresearch.ast.lex.LexNameToken;
import eu.compassresearch.ast.node.NodeList;
import eu.compassresearch.ast.patterns.APatternListTypePair;
import eu.compassresearch.ast.patterns.PPattern;
import eu.compassresearch.ast.statements.ABlockSimpleBlockStm;
import eu.compassresearch.ast.statements.ACaseAlternativeStm;
import eu.compassresearch.ast.statements.ACasesStm;
import eu.compassresearch.ast.statements.AElseIfStm;
import eu.compassresearch.ast.statements.AIfStm;
import eu.compassresearch.ast.statements.ANonDeterministicSimpleBlockStm;
import eu.compassresearch.ast.statements.PStm;
import eu.compassresearch.ast.statements.SSimpleBlockStm;
import eu.compassresearch.ast.typechecker.NameScope;

public class ToStringUtil
{
	public static String getExplicitFunctionString(AExplicitFunctionFunctionDefinition d)
	{
		StringBuilder params = new StringBuilder();

		for (List<PPattern> plist : d.getParamPatternList())
		{
			params.append("(" + Utils.listToString(plist) + ")");
		}

		String accessStr = d.getAccess().toString();
		if (d.getNameScope() == NameScope.LOCAL)
			accessStr = "";

		return accessStr
				+ d.getName().name
				+ (d.getTypeParams().isEmpty() ? ": " : "["
						+ getTypeListString(d.getTypeParams()) + "]: ")
				+ d.getType()
				+ "\n\t"
				+ d.getName().name
				+ params
				+ " ==\n"
				+ d.getBody()
				+ (d.getPrecondition() == null ? "" : "\n\tpre "
						+ d.getPrecondition())
				+ (d.getPostcondition() == null ? "" : "\n\tpost "
						+ d.getPostcondition());
	}

	public static String getImplicitFunctionString(AImplicitFunctionFunctionDefinition d)
	{
	    /** @FIXME @TODO Below body is commented out */
		return d.getAccess()
				+ " "
				+ d.getName().name
				+ (d.getTypeParams().isEmpty() ? "" : "["
						+ getTypeListString(d.getTypeParams()) + "]")
				+ Utils.listToString("(", getString(d.getParamPatterns()), ", ", ")")
				+ d.getResult()
		    + "" //(d.getBody() == null ? "" : " ==\n\t" + d.getBody())
				+ (d.getPrecondition() == null ? "" : "\n\tpre "
						+ d.getPrecondition())
				+ (d.getPostcondition() == null ? "" : "\n\tpost "
						+ d.getPostcondition());
	}

	private static List<String> getString(List<APatternListTypePair> node)
	{
		List<String> list = new Vector<String>();
		for (APatternListTypePair pl : node)
		{
			list.add("(" + getStringPattern(pl.getPatterns()) + ":"
					+ pl.getType() + ")");
		}
		return list;
	}

	private static String getStringPattern(List<PPattern> patterns)
	{
		return Utils.listToString(patterns);
	}

	private static String getTypeListString(List<LexNameToken> typeParams)
	{
		return "(" + Utils.listToString(typeParams) + ")";
	}

	public static String getExplicitOperationString(
			AExplicitOperationOperationDefinition d)
	{
		return d.getName()
				+ " "
				+ d.getType()
				+ "\n\t"
				+ d.getName()
				+ "("
				+ Utils.listToString(d.getParameterPatterns())
				+ ")"
				+ (d.getBody() == null ? "" : " ==\n" + d.getBody())
				+ (d.getPrecondition() == null ? "" : "\n\tpre "
						+ d.getPrecondition())
				+ (d.getPostcondition() == null ? "" : "\n\tpost "
						+ d.getPostcondition());
	}

	public static String getImplicitOperationString(
			AImplicitOperationOperationDefinition d)
	{
		return d.getName()
				+ Utils.listToString("(", d.getParameterPatterns(), ", ", ")")
				+ (d.getResult() == null ? "" : " " + d.getResult())
				+ (d.getExternals().isEmpty() ? "" : "\n\text "
						+ d.getExternals())
				+ (d.getPrecondition() == null ? "" : "\n\tpre "
						+ d.getPrecondition())
				+ (d.getPostcondition() == null ? "" : "\n\tpost "
						+ d.getPostcondition())
				+ (d.getErrors().isEmpty() ? "" : "\n\terrs " + d.getErrors());
	}

	public static String getDefinitionListString(
			NodeList<PDefinition> _definitions)
	{
		StringBuilder sb = new StringBuilder();

		for (PDefinition d : _definitions)
		{
			if (d.getAccess() != null)
			{
				sb.append(d.getAccess());
				sb.append(" ");
			}
			sb.append(d.kindPDefinition() + " " + getVariableNames(d) + ":"
					+ d.getType());
			sb.append("\n");
		}

		return sb.toString();
	}

	private static LexNameList getVariableNames(List<? extends PDefinition> list)
	{
		LexNameList variableNames = new LexNameList();

		for (PDefinition dd : list)
		{
			variableNames.addAll(getVariableNames(dd));
		}

		return variableNames;
	}

	private static LexNameList getVariableNames(PDefinition d)
	{
		switch (d.kindPDefinition())
		{

			case CLASS:
				if (d instanceof SClassDefinition)
				{
					return getVariableNames(((SClassDefinition) d).getDefinitions());
				}
				assert false : "Error in class getVariableNames";
				break;

			case EQUALS:
				if (d instanceof AEqualsDefinition)
				{
					return ((AEqualsDefinition) d).getDefs() == null ? new LexNameList()
							: getVariableNames(((AEqualsDefinition) d).getDefs());
				}
				assert false : "Error in equals getVariableNames";
				break;

			case EXTERNAL:
				// return state.getVariableNames();
				// TODO
				return new LexNameList(new LexNameToken("Not implemented", "Not implemented", new LexLocation()));

			case IMPORTED:
				if (d instanceof AImportedDefinition)
				{
					return getVariableNames(((AImportedDefinition) d).getDef());
				}
				assert false : "Error in imported getVariableNames";
				break;
			case INHERITED:
				if (d instanceof AInheritedDefinition)
				{
					LexNameList names = new LexNameList();
					// checkSuperDefinition();//TODO
					AInheritedDefinition t = (AInheritedDefinition) d;
					for (LexNameToken vn : getVariableNames(t.getSuperdef()))
					{
						names.add(vn.getModifiedName(t.getName().module));
					}

					return names;
				}
				assert false : "Error in inherited getVariableNames";
				break;
				
			case MULTIBINDLIST:
				if (d instanceof AMultiBindListDefinition)
				{
					return ((AMultiBindListDefinition) d).getDefs() == null ? new LexNameList()
							: getVariableNames(((AMultiBindListDefinition) d).getDefs());
				}
				break;
				/*			case MUTEXSYNC:
			case NAMEDTRACE:
			case PERSYNC:
			return new LexNameList();*/
			case RENAMED:
				if (d instanceof ARenamedDefinition)
				{
					LexNameList both = new LexNameList(d.getName());
					both.add(((ARenamedDefinition) d).getDef().getName());
					return both;
				}
				assert false : "Error in renamed getVariableNames";

			case STATE:
				// return statedefs.getVariableNames();
				// TODO
				return new LexNameList(new LexNameToken("Not implemented", "Not implemented", new LexLocation()));
				// if (d instanceof AThreadDefinition)
				// {
				// 	if (((AThreadDefinition) d).getOperationDef() != null)// Differnt from VDMJ
				// 	{
				// 		return new LexNameList(((AThreadDefinition) d).getOperationDef().getName());
				// 	} else
				// 	{
				// 		return null;
				// 	}
				// }
				// assert false : "Error in thread getVariableNames";
				// break;
			case TYPE:
				return new LexNameList(d.getName());
			case UNTYPED:
				assert false : "Can't get variables of untyped definition?";
				return null;

			case VALUE:
				if (d instanceof AValueDefinition)
				{
					// return ((AValueDefinition) d).getPattern()
					// TODO
					return new LexNameList(new LexNameToken("Not implemented", "Not implemented", new LexLocation()));
				}
				// return pattern.getVariableNames();
				break;

			default:
				return new LexNameList(d.getName());

		}
		return null;
	}

	public static String getCasesString(ACasesStm stm)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("cases " + stm.getExp() + " :\n");

		for (ACaseAlternativeStm csa : stm.getCases())
		{
			sb.append("  ");
			sb.append(csa.toString());
		}

		if (stm.getOthers() != null)
		{
			sb.append("  others -> ");
			sb.append(stm.getOthers().toString());
		}

		sb.append("esac");
		return sb.toString();
	}

	public static String getIfString(AIfStm node)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("if " + node.getIfExp() + "\nthen\n" + node.getThenStm());

		for (AElseIfStm s : node.getElseIf())
		{
			sb.append(s.toString());
		}

		if (node.getElseStm() != null)
		{
			sb.append("else\n");
			sb.append(node.getElseStm().toString());
		}

		return sb.toString();
	}

	public static String getSimpleBlockString(SSimpleBlockStm node)
	{
		StringBuilder sb = new StringBuilder();
		String sep = "";

		for (PStm s : node.getStatements())
		{
			sb.append(sep);
			sb.append(s.toString());
			sep = ";\n";
		}

		sb.append("\n");
		return sb.toString();
	}

	public static String getBlockSimpleBlockString(ABlockSimpleBlockStm node)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("(\n");

		for (PDefinition d : node.getAssignmentDefs())
		{
			sb.append(d);
			sb.append("\n");
		}

		sb.append("\n");
		sb.append(getSimpleBlockString(node));
		sb.append(")");
		return sb.toString();
	}

	public static String getNonDeterministicSimpleBlockString(
			ANonDeterministicSimpleBlockStm node)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("||(\n");
		sb.append(getSimpleBlockString(node));
		sb.append(")");
		return sb.toString();
	}
}
