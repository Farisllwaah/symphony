package eu.compassresearch.core.analysis.pog.obligations;

import java.util.Iterator;
import java.util.Vector;

import org.overture.ast.expressions.ABooleanConstExp;
import org.overture.ast.expressions.ACharLiteralExp;
import org.overture.ast.expressions.AGreaterEqualNumericBinaryExp;
import org.overture.ast.expressions.AGreaterNumericBinaryExp;
import org.overture.ast.expressions.AIsExp;
import org.overture.ast.expressions.ALenUnaryExp;
import org.overture.ast.expressions.ALessEqualNumericBinaryExp;
import org.overture.ast.expressions.AMapEnumMapExp;
import org.overture.ast.expressions.AMapletExp;
import org.overture.ast.expressions.AMkTypeExp;
import org.overture.ast.expressions.ANotEqualBinaryExp;
import org.overture.ast.expressions.ASeqEnumSeqExp;
import org.overture.ast.expressions.ASetEnumSetExp;
import org.overture.ast.expressions.ASetRangeSetExp;
import org.overture.ast.expressions.ASubseqExp;
import org.overture.ast.expressions.ATupleExp;
import org.overture.ast.expressions.AVariableExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.lex.LexKeywordToken;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.lex.VDMToken;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.ACharBasicType;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ANatNumericBasicType;
import org.overture.ast.types.ANatOneNumericBasicType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.AProductType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.ASeq1SeqType;
import org.overture.ast.types.ASetType;
import org.overture.ast.types.AUnionType;
import org.overture.ast.types.PType;
import org.overture.ast.types.SBasicType;
import org.overture.ast.types.SInvariantType;
import org.overture.ast.types.SMapType;
import org.overture.ast.types.SNumericBasicType;
import org.overture.ast.types.SSeqType;
import org.overture.ast.util.PTypeSet;
import org.overture.pog.pub.IPOContextStack;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;
import org.overture.typechecker.assistant.type.SNumericBasicTypeAssistantTC;

import eu.compassresearch.ast.definitions.AExplicitCmlOperationDefinition;

public class CmlSubTypeObligation extends CmlProofObligation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CmlSubTypeObligation(AExplicitCmlOperationDefinition def,
			PType actualResult, IPOContextStack ctxt) {
		
		super(def, CmlPOType.SUB_TYPE, ctxt);

		AVariableExp result = AstFactory.newAVariableExp(
				new LexNameToken(def.getName().getModule(), "RESULT", def.getLocation()));
		
		valuetree.setPredicate(ctxt.getPredWithContext(
				oneType(false, result, ((AOperationType) def.getType()).getResult().clone(), actualResult.clone())));
	
		
		
		}
	
	//FIXME Duplicated from Overture Subtype. Fix when we have subtype obligations for cml
	private PExp oneType(boolean rec, PExp exp, PType etype, PType atype)
	{
		if (atype != null && rec)
		{
			if (TypeComparator.isSubType(atype, etype))
			{
				return null;	// Means a sub-comparison is OK without PO checks
			}
		}

		PExp po = null;
		etype = PTypeAssistantTC.deBracket(etype);

		if (etype instanceof AUnionType)
		{
			AUnionType ut = (AUnionType) etype;
			PTypeSet possibles = new PTypeSet();

			for (PType pos : ut.getTypes())
			{
				if (atype == null || TypeComparator.compatible(pos, atype))
				{
					possibles.add(pos);
				}
			}

			po = null;
			
			for (PType poss : possibles)
			{
				PExp s = oneType(true, exp, poss, null);
				PExp e = addIs(exp, poss);

				if (s != null && !(s instanceof AIsExp))
				{
					e = makeAnd(e, s);
				}
				
				po = makeOr(po, e);
			}
		}
		else if (etype instanceof SInvariantType)
		{
			SInvariantType et = (SInvariantType) etype;
			po = null;

			if (et.getInvDef() != null)
			{
				AVariableExp root = getVarExp(et.getInvDef().getName());			

				// This needs to be put back if/when we change the inv_R signature to take
				// the record fields as arguments, rather than one R value.
				//
				// if (exp instanceof MkTypeExpression)
				// {
				//     MkTypeExpression mk = (MkTypeExpression)exp;
				//     sb.append(Utils.listToString(mk.args));
				// }
				// else
				// {
				//     ab.append(exp);
				// }
				
				po = getApplyExp(root, exp);
			}

			if (etype instanceof ANamedInvariantType)
			{
				ANamedInvariantType nt = (ANamedInvariantType) etype;

				if (atype instanceof ANamedInvariantType)
				{
					atype = ((ANamedInvariantType) atype).getType();
				}
				else
				{
					atype = null;
				}

				PExp s = oneType(true, exp, nt.getType(), atype);

				if (s != null)
				{
					po = makeAnd(po, s);
				}
			}
			else if (etype instanceof ARecordInvariantType)
			{
				if (exp instanceof AMkTypeExp)
				{
					ARecordInvariantType rt = (ARecordInvariantType) etype;
					AMkTypeExp mk = (AMkTypeExp) exp;

					if (rt.getFields().size() == mk.getArgs().size())
					{
						Iterator<AFieldField> fit = rt.getFields().iterator();
						Iterator<PType> ait = mk.getArgTypes().iterator();

						for (PExp e : mk.getArgs())
						{
							PExp s = oneType(true, e, fit.next().getType(), ait.next());

							if (s != null)
							{
								po = makeAnd(po, s);
							}
						}
					}
				}
				else
				{
					po = makeAnd(po, addIs(exp, etype));
				}
			}
			else
			{
				po = makeAnd(po, addIs(exp, etype));
			}
		}
		else if (etype instanceof SSeqType)
		{
			po = null;

			if (etype instanceof ASeq1SeqType)
			{
				ANotEqualBinaryExp ne = new ANotEqualBinaryExp();
				ne.setLeft(exp);
				ASeqEnumSeqExp empty = new ASeqEnumSeqExp();
				empty.setMembers(new Vector<PExp>());
				ne.setRight(empty);
			}

			if (exp instanceof ASeqEnumSeqExp)
			{
				SSeqType stype = (SSeqType) etype;
				ASeqEnumSeqExp seq = (ASeqEnumSeqExp) exp;
				Iterator<PType> it = seq.getTypes().iterator();

				for (PExp m : seq.getMembers())
				{
					PExp s = oneType(true, m, stype.getSeqof(), it.next());

					if (s != null)
					{
						po = makeAnd(po, s);
					}
				}
			}
			else if (exp instanceof ASubseqExp)
			{
				ASubseqExp subseq = (ASubseqExp) exp;
				PType itype = AstFactory.newANatOneNumericBasicType(exp.getLocation());
				PExp s = oneType(true, subseq.getFrom(), itype, subseq.getFtype());

				if (s != null)
				{
					po = makeAnd(po, s);
				}

				s = oneType(true, subseq.getTo(), itype, subseq.getTtype());

				if (s != null)
				{
					po = makeAnd(po, s);
				}

				ALessEqualNumericBinaryExp le = new ALessEqualNumericBinaryExp();
				le.setLeft(subseq.getTo());
				ALenUnaryExp len = new ALenUnaryExp();
				len.setExp(subseq.getSeq());
				le.setRight(len);
				po = makeAnd(po, le);

				po = makeAnd(po, addIs(exp, etype)); // Like set range does
			}
			else
			{
				po = addIs(exp, etype); // remove any "x <> []"
			}
		}
		else if (etype instanceof SMapType)
		{
			if (exp instanceof AMapEnumMapExp)
			{
				SMapType mtype = (SMapType) etype;
				AMapEnumMapExp seq = (AMapEnumMapExp) exp;
				Iterator<PType> dit = seq.getDomTypes().iterator();
				Iterator<PType> rit = seq.getRngTypes().iterator();
				po = null;

				for (AMapletExp m : seq.getMembers())
				{
					PExp s = oneType(true, m.getLeft(), mtype.getFrom(), dit.next());

					if (s != null)
					{
						po = makeAnd(po, s);
					}

					s = oneType(true, m.getRight(), mtype.getTo(), rit.next());

					if (s != null)
					{
						po = makeAnd(po, s);
					}
				}
			}
			else
			{
				po = addIs(exp, etype);
			}
		}
		else if (etype instanceof ASetType)
		{
			po = null;

			if (exp instanceof ASetEnumSetExp)
			{
				ASetType stype = (ASetType) etype;
				ASetEnumSetExp set = (ASetEnumSetExp) exp;
				Iterator<PType> it = set.getTypes().iterator();

				for (PExp m : set.getMembers())
				{
					PExp s = oneType(true, m, stype.getSetof(), it.next());

					if (s != null)
					{
						po = makeAnd(po, s);
					}
				}
			}
			else if (exp instanceof ASetRangeSetExp)
			{
				ASetType stype = (ASetType) etype;
				ASetRangeSetExp range = (ASetRangeSetExp) exp;
				PType itype = AstFactory.newAIntNumericBasicType(exp.getLocation());

				PExp s = oneType(true, range.getFirst(), itype,	range.getFtype());

				if (s != null)
				{
					po = makeAnd(po, s);
				}

				s = oneType(true, range.getFirst(), stype.getSetof(), range.getFtype());

				if (s != null)
				{
					po = makeAnd(po, s);
				}

				s = oneType(true, range.getLast(), itype, range.getLtype());

				if (s != null)
				{
					po = makeAnd(po, s);
				}

				s = oneType(true, range.getLast(), stype.getSetof(), range.getLtype());

				if (s != null)
				{
					po = makeAnd(po, s);
				}
			}

			po = makeAnd(po, addIs(exp, etype));
		}
		else if (etype instanceof AProductType)
		{
			if (exp instanceof ATupleExp)
			{
				AProductType pt = (AProductType) etype;
				ATupleExp te = (ATupleExp) exp;
				Iterator<PType> eit = pt.getTypes().iterator();
				Iterator<PType> ait = te.getTypes().iterator();
				po = null;

				for (PExp e : te.getArgs())
				{
					PExp s = oneType(true, e, eit.next(), ait.next());

					if (s != null)
					{
						po = makeAnd(po, s);
					}
				}
			}
			else
			{
				po = addIs(exp, etype);
			}
		}
		else if (etype instanceof SBasicType)
		{
			if (etype instanceof SNumericBasicType)
			{
				SNumericBasicType ent = (SNumericBasicType) etype;

				if (atype instanceof SNumericBasicType)
				{
					SNumericBasicType ant = (SNumericBasicType) atype;

					if (SNumericBasicTypeAssistantTC.getWeight(ant) > SNumericBasicTypeAssistantTC.getWeight(ent))
					{
						boolean isWhole = SNumericBasicTypeAssistantTC.getWeight(ant) < 3;
						
						if (isWhole && ent instanceof ANatOneNumericBasicType)
						{
							AGreaterNumericBinaryExp gt = new AGreaterNumericBinaryExp();
							gt.setLeft(exp);
							gt.setOp(new LexKeywordToken(VDMToken.GT, exp.getLocation()));
							gt.setRight(getIntLiteral(0));
							po = gt;
						}
						else if (isWhole && ent instanceof ANatNumericBasicType)
						{
							AGreaterEqualNumericBinaryExp ge = new AGreaterEqualNumericBinaryExp();
							ge.setLeft(exp);
							ge.setOp(new LexKeywordToken(VDMToken.GE, exp.getLocation()));
							ge.setRight(getIntLiteral(0));
							po = ge;
						}
						else
						{
							AIsExp isExp = new AIsExp();
							isExp.setBasicType(ent);
							isExp.setType(new ABooleanBasicType());
							isExp.setTest(exp);
							po = isExp;
						}
					}
				}
				else
				{
					AIsExp isExp = new AIsExp();
					isExp.setBasicType(ent);
					isExp.setType(new ABooleanBasicType());
					isExp.setTest(exp);
					po = isExp;
				}
			}
			else if (etype instanceof ABooleanBasicType)
			{
				if (!(exp instanceof ABooleanConstExp))
				{
					po = addIs(exp, etype);
				}
			}
			else if (etype instanceof ACharBasicType)
			{
				if (!(exp instanceof ACharLiteralExp))
				{
					po = addIs(exp, etype);
				}
			}
			else
			{
				po = addIs(exp, etype);
			}
		}
		else
		{
			po = addIs(exp, etype);
		}

		return po;
	}

	/**
	 * Just produce one is_(<expression>, <type>) node. 
	 */
	private PExp addIs(PExp exp, PType type)
	{
		AIsExp isExp = new AIsExp();
		isExp.setBasicType(type);
		isExp.setType(new ABooleanBasicType());
		isExp.setTest(exp);
		return isExp;
	}
	

}