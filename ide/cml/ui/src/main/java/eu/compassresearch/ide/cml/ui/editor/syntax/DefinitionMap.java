package eu.compassresearch.ide.cml.ui.editor.syntax;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.PDefinition;

import eu.compassresearch.ast.definitions.AActionParagraphDefinition;
import eu.compassresearch.ast.definitions.AFunctionParagraphDefinition;
import eu.compassresearch.ast.definitions.AOperationParagraphDefinition;
import eu.compassresearch.ast.definitions.ATypesParagraphDefinition;
import eu.compassresearch.ast.definitions.AValueParagraphDefinition;
import eu.compassresearch.ast.definitions.SOperationDefinition;

public class DefinitionMap {

	public interface DefinitionHandler {
		public List<Wrapper<? extends PDefinition>> extractSubdefinition(
				PDefinition pdef);
	}

	private static final Map<Class<?>, ? extends DefinitionHandler> DEFINITION_MAP = createMap();

	public static DefinitionHandler getDelegate(Class<?> cls) {
		DefinitionHandler r = DEFINITION_MAP.get(cls);
		if (r == null)
			System.err.println("No delegate found for definition class "
					+ cls.getCanonicalName());
		return r;
	}

	private static Map<Class<?>, ? extends DefinitionHandler> createMap() {
		Map<Class<?>, DefinitionHandler> map = new HashMap<Class<?>, DefinitionHandler>();
		map.put(AActionParagraphDefinition.class,
				new AActionParagraphDefinitionHandler());
		map.put(AValueParagraphDefinition.class,
				new AValueParagraphDefinitionHandler());
		map.put(AFunctionParagraphDefinition.class,
				new AFunctionParagraphDefinitionHandler());
		map.put(ATypesParagraphDefinition.class,
				new ATypesParagraphDefinitionHandler());
		map.put(AOperationParagraphDefinition.class,
				new AOperationParagraphDefinitionHandler());
		return Collections.unmodifiableMap(map);
	}

	private static class AValueParagraphDefinitionHandler implements
			DefinitionHandler {

		public List<Wrapper<? extends PDefinition>> extractSubdefinition(PDefinition pdef) {
			List<Wrapper<? extends PDefinition>> r = new LinkedList<Wrapper<? extends PDefinition>>();

			for (PDefinition subdef : ((AValueParagraphDefinition) pdef)
					.getValueDefinitions()) {
				String nameguard = "?";
				String typeguard = "?";
				if (null != subdef.getName())
					nameguard = subdef.getName().name;
				if (null != subdef.getType())
					typeguard = subdef.getType().toString();
				r.add(Wrapper.newInstance(subdef, nameguard + ": " + typeguard));
			}
			return r;
		}
	}

	private static class AFunctionParagraphDefinitionHandler implements
			DefinitionHandler {

		public List<Wrapper<? extends PDefinition>> extractSubdefinition(PDefinition pdef) {
		    List<Wrapper<? extends PDefinition>> r = new LinkedList<Wrapper<? extends PDefinition>>();
			for (PDefinition subdef : ((AFunctionParagraphDefinition) pdef)
					.getFunctionDefinitions()) {
				r.add(Wrapper.newInstance(subdef, subdef.getName().name + ": "
						+ subdef.getType()));
			}
			return r;
		}
	}

	private static class ATypesParagraphDefinitionHandler implements
			DefinitionHandler {

		public List<Wrapper<? extends PDefinition>> extractSubdefinition(PDefinition pdef) {
		    List<Wrapper<? extends PDefinition>> r = new LinkedList<Wrapper<? extends PDefinition>>();
			for (ATypeDefinition subdef : ((ATypesParagraphDefinition) pdef)
					.getTypes())
				r.add(Wrapper.newInstance(subdef, subdef.getName().name));
			return r;
		}
	}

	private static class AOperationParagraphDefinitionHandler implements
			DefinitionHandler {

		public List<Wrapper<? extends PDefinition>> extractSubdefinition(PDefinition pdef) {
		    List<Wrapper<? extends PDefinition>> r = new LinkedList<Wrapper<? extends PDefinition>>();
			for (SOperationDefinition subdef : ((AOperationParagraphDefinition) pdef)
					.getOperations())
				r.add(Wrapper.newInstance(subdef, subdef.getName().name + ": "
						+ subdef.getType()));
			return r;
		}
	}

	private static class AActionParagraphDefinitionHandler implements
			DefinitionHandler {
		public List<Wrapper<? extends PDefinition>> extractSubdefinition(PDefinition pdef) {
		    List<Wrapper<? extends PDefinition>> r = new LinkedList<Wrapper<? extends PDefinition>>();
			String nameguard;
			if (pdef instanceof AActionParagraphDefinition) {
				if (null == pdef.getName())
					nameguard = "?";
				else
					nameguard = pdef.getName().name;
				r.add(Wrapper.newInstance(pdef, nameguard));
			}
			return r;
		}
	}

}