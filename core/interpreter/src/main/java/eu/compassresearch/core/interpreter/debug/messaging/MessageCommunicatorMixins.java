package eu.compassresearch.core.interpreter.debug.messaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.overture.ast.definitions.AClassClassDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexQuoteToken;
import org.overture.ast.node.INode;
import org.overture.ast.node.Node;
import org.overture.ast.types.AClassType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.PType;
import org.overture.interpreter.values.BooleanValue;
import org.overture.interpreter.values.CharacterValue;
import org.overture.interpreter.values.FieldMap;
import org.overture.interpreter.values.FieldValue;
import org.overture.interpreter.values.FunctionValue;
import org.overture.interpreter.values.IntegerValue;
import org.overture.interpreter.values.NaturalOneValue;
import org.overture.interpreter.values.NaturalValue;
import org.overture.interpreter.values.QuoteValue;
import org.overture.interpreter.values.RationalValue;
import org.overture.interpreter.values.RealValue;
import org.overture.interpreter.values.RecordValue;
import org.overture.interpreter.values.Value;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.Module.SetupContext;

import eu.compassresearch.ast.definitions.AActionClassDefinition;
import eu.compassresearch.ast.lex.CmlLexNameToken;
import eu.compassresearch.ast.types.AChannelType;
import eu.compassresearch.core.interpreter.api.CmlBehaviour;
import eu.compassresearch.core.interpreter.api.events.ChannelEvent;
import eu.compassresearch.core.interpreter.api.events.ChannelObserver;
import eu.compassresearch.core.interpreter.api.events.EventSourceHandler;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransitionSet;
import eu.compassresearch.core.interpreter.api.transitions.TauTransition;
import eu.compassresearch.core.interpreter.api.values.ChannelValue;
import eu.compassresearch.core.interpreter.api.values.CmlChannel;
import eu.compassresearch.core.interpreter.api.values.LatticeTopValue;
import eu.compassresearch.core.interpreter.api.values.MultiConstraint;
import eu.compassresearch.core.interpreter.api.values.ValueConstraint;
import eu.compassresearch.core.interpreter.debug.Breakpoint;
import eu.compassresearch.core.interpreter.debug.CmlProcessDTO;

public class MessageCommunicatorMixins
{
	/*
	 * All mixins must be declared as: static abstract class
	 */

	static abstract class NodeMixIn
	{
		@JsonIgnore
		INode parent;
		@SuppressWarnings("rawtypes")
		@JsonIgnore
		Set _visitedNodes;
	}

	static abstract class LexNameTokenMixIn
	{
		LexNameTokenMixIn(@JsonProperty("module") String module,
				@JsonProperty("name") String name,
				@JsonProperty("location") ILexLocation location)
		{
		}

		@JsonIgnore
		List<PType> typeQualifier;
		// @JsonIgnore
		// ILexLocation location;
	}

	static abstract class LexQuoteTokenMixIn
	{
		LexQuoteTokenMixIn(@JsonProperty("value") String value,
				@JsonProperty("location") ILexLocation location)
		{
		}
	}

	// IDE

	static abstract class BreakpointMixIn
	{
		BreakpointMixIn(@JsonProperty("id") int id,
				@JsonProperty("file") String file,
				@JsonProperty("line") int line)
		{
		}

	}

	@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
	static abstract class CmlProcessDTOMixIn
	{
	}

	// cosim

	static abstract class MultiConstraintMixIn
	{
		public MultiConstraintMixIn(
				@JsonProperty("constraints") List<ValueConstraint> constraints)
		{
		}
	}

	// values
	static abstract class QuoteValueMixIn
	{
		QuoteValueMixIn(@JsonProperty("value") String value)
		{
		}

	}

	static abstract class BooleanValueMixIn
	{
		BooleanValueMixIn(@JsonProperty("value") boolean value)
		{
		}

	}

	static abstract class NaturalValueMixIn
	{
		NaturalValueMixIn(@JsonProperty("longVal") long value)
		{
		}

		@JsonIgnore
		public double value;
	}

	static abstract class NaturalOneValueMixIn
	{
		NaturalOneValueMixIn(@JsonProperty("longVal") long value)
		{
		}

		@JsonIgnore
		public double value;
	}

	static abstract class IntegerValueMixIn
	{
		IntegerValueMixIn(@JsonProperty("longVal") long value)
		{
		}

		@JsonIgnore
		double value;
	}

	static abstract class RationalValueMixIn
	{
		RationalValueMixIn(@JsonProperty("value") double value)
		{
		}

	}

	static abstract class RealValueMixIn
	{
		RealValueMixIn(@JsonProperty("value") double value)
		{
		}

	}

	static abstract class TokenValueMixIn
	{
		TokenValueMixIn(@JsonProperty("value") Value exp)
		{
		}
	}

	static abstract class CharacterValueMixIn
	{
		CharacterValueMixIn(@JsonProperty("unicode") char value)
		{
		}
	}

	static abstract class RecordValueMixIn
	{
		RecordValueMixIn(@JsonProperty("type") ARecordInvariantType type,
				@JsonProperty("fieldmap") FieldMap mapvalues,
				@JsonProperty("invariant") FunctionValue invariant)
		{

		}

		@JsonIgnore
		FunctionValue invariant;
	}

	static abstract class FieldValueMixIn
	{
		FieldValueMixIn(@JsonProperty("name") String name,
				@JsonProperty("value") Value value,
				@JsonProperty("comparable") boolean comparable)
		{

		}

	}

	static abstract class CmlChannelMixIn
	{

		@JsonIgnore
		EventSourceHandler<ChannelObserver, ChannelEvent> selectObservers;

		CmlChannelMixIn(@JsonProperty("channelType") AChannelType channelType,
				@JsonProperty("name") ILexNameToken name)
		{
		}
	}

	static abstract class ChannelValueMixIn
	{
		ChannelValueMixIn(@JsonProperty("channel") CmlChannel channel,
				@JsonProperty("values") List<Value> values,
				@JsonProperty("constraints") List<ValueConstraint> constraints)
		{
		}
	}

	static abstract class LatticeTopValueMixIn
	{
		LatticeTopValueMixIn(@JsonProperty("type") PType type)
		{
		}
	}

	static abstract class TauTransitionMixIn
	{
		TauTransitionMixIn(
				@JsonProperty("eventSource") CmlBehaviour eventSource,
				@JsonProperty("destinationNode") INode destinationNode,
				@JsonProperty("transitionMessage") String transitionMessage)
		{
		}
	}

	private static Map<Class<?>, String[]> ignore = new HashMap<Class<?>, String[]>();

	static
	{
		ignore.put(AClassType.class, new String[] { "_classdef" });
		ignore.put(ATypeDefinition.class, new String[] { "_classDefinition",
				"_invdef" });
		ignore.put(AClassClassDefinition.class, new String[] { "_definitions" });
		ignore.put(AActionClassDefinition.class, new String[] { "_definitions" });
		ignore.put(ARecordInvariantType.class, new String[] { "_invDef"/*
																		 * , "_fields"
																		 */});
		ignore.put(PType.class, new String[] { "_definitions", "_location",
				"_resolved" });
		ignore.put(CmlTransitionSet.class, new String[] { "silentEvents" });
		ignore.put(CmlChannel.class, new String[] { "selectObservers" });
		// ignore.put(AChannelType.class, new String[] { "_location","_definitions" });

		ignore.put(CmlLexNameToken.class, new String[] { "location", "old",
				"explicit", "hashcode" });
		// ignore.put(LatticeTopValue.class, new String[] { "type" });//Commented this, not sure which case that made us
		// add ignore for it
	}

	public static void setup(SetupContext ctxt)
	{
		ctxt.setMixInAnnotations(Node.class, NodeMixIn.class);
		ctxt.setMixInAnnotations(CmlLexNameToken.class, LexNameTokenMixIn.class);
		ctxt.setMixInAnnotations(org.overture.ast.lex.LexNameToken.class, LexNameTokenMixIn.class);

		// IDE
		ctxt.setMixInAnnotations(Breakpoint.class, BreakpointMixIn.class);
		ctxt.setMixInAnnotations(CmlProcessDTO.class, CmlProcessDTOMixIn.class);

		// Cosim
		ctxt.setMixInAnnotations(QuoteValue.class, QuoteValueMixIn.class);
		ctxt.setMixInAnnotations(LexQuoteToken.class, LexQuoteTokenMixIn.class);
		ctxt.setMixInAnnotations(BooleanValue.class, BooleanValueMixIn.class);
		ctxt.setMixInAnnotations(NaturalValue.class, NaturalValueMixIn.class);
		ctxt.setMixInAnnotations(NaturalOneValue.class, NaturalOneValueMixIn.class);
		ctxt.setMixInAnnotations(RationalValue.class, RationalValueMixIn.class);
		ctxt.setMixInAnnotations(RealValue.class, RealValueMixIn.class);
		ctxt.setMixInAnnotations(org.overture.interpreter.values.TokenValue.class, TokenValueMixIn.class);
		ctxt.setMixInAnnotations(CharacterValue.class, CharacterValueMixIn.class);
		ctxt.setMixInAnnotations(RecordValue.class, RecordValueMixIn.class);
		ctxt.setMixInAnnotations(FieldValue.class, FieldValueMixIn.class);
		ctxt.setMixInAnnotations(CmlChannel.class, CmlChannelMixIn.class);
		ctxt.setMixInAnnotations(LatticeTopValue.class, LatticeTopValueMixIn.class);
		ctxt.setMixInAnnotations(MultiConstraint.class, MultiConstraintMixIn.class);
		ctxt.setMixInAnnotations(ChannelValue.class, ChannelValueMixIn.class);
		ctxt.setMixInAnnotations(IntegerValue.class, IntegerValueMixIn.class);

		ctxt.setMixInAnnotations(TauTransition.class, TauTransitionMixIn.class);

		ctxt.appendAnnotationIntrospector(new JsonIgnoreIntrospector(ignore));
	}
}
