package eu.compassresearch.ide.cml.ui.editor.syntax;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.lex.LexLocation;
import org.overture.ast.node.INode;

import eu.compassresearch.ide.cml.ui.editor.core.CmlEditor;
import eu.compassresearch.ide.cml.ui.editor.core.dom.CmlSourceUnit;

public class CmlContentPageOutliner extends ContentOutlinePage implements
	IContentOutlinePage {

    private CmlSourceUnit input;
    private CmlEditor editor;
    private CmlTreeContentProvider provider;
    private OutlineLabelProvider labelprovider;
    private TreeViewer viewer;

    // public static final int ALL_LEVELS = -1;

    public void setTreeSelection(INode element) {
	Wrapper w;
	PDefinition pdef =  (PDefinition) element;
	String dscr = TopLevelDefinitionMap.getDescription(pdef.getClass());
	if (dscr == null)
	    w = Wrapper.newInstance(pdef, pdef.getName().name);
	else
	    w = Wrapper.newInstance(pdef, dscr);
	System.out.println("Setting outline selection to " + w.toString());
	getTreeViewer().setSelection(new StructuredSelection(w), true);

	// viewer.setSelection(element, true);
    }

    public void refresh() {
	final Display curDisp = Display.getDefault();
	if (curDisp != null)
	    curDisp.syncExec(new Runnable() {
		public void run() {
		    getTreeViewer().refresh();
		    // getTreeViewer().expandAll();
		}
	    });

    }

    public CmlContentPageOutliner(CmlEditor editor) {
	provider = new CmlTreeContentProvider(this.getControl());
	this.editor = editor;
    }

    @Override
    public void createControl(Composite parent) {
	super.createControl(parent);
	viewer = getTreeViewer();
	viewer.setContentProvider(provider);
	viewer.setUseHashlookup(true);
	viewer.addSelectionChangedListener(new ISelectionChangedListener() {

	    public void selectionChanged(SelectionChangedEvent event) {
		// if the selection is empty clear the label
		
		//FIXME Remove the Editor->Outline->Editor selection loop.
		if (event.getSelection().isEmpty()) {
		    System.out.println("Empty Selection");
		    return;
		}
		if (event.getSelection() instanceof IStructuredSelection) {
		    IStructuredSelection selection = (IStructuredSelection) event
			    .getSelection();
		    StringBuffer toShow = new StringBuffer();
		    for (Iterator iterator = selection.iterator(); iterator
			    .hasNext();) {

		
			Object o = iterator.next();
			System.out.println("Selected " + o.toString());
			if (o instanceof Wrapper) {
			    Wrapper w = (Wrapper) o;
			    // FIXME Find a better system to extract locations
			    for (Method m : w.value.getClass().getMethods()) {
				if ("getLocation".equals(m.getName())) {
				    try {
					LexLocation loc = (LexLocation) m
						.invoke(w.value, new Object[0]);
					editor.setHighlightRange(
						loc.startOffset, 0, true);
				    } catch (Exception e) {
					e.printStackTrace();
				    }
				}
			    }
			}
		    }
		}
	    }
	});

	labelprovider = new OutlineLabelProvider();
	viewer.setLabelProvider(labelprovider);
	viewer.expandAll();
	viewer.setInput(input);
	// FIXME ldc -Need to add proper filters
	getTreeViewer().expandAll();
    }

    public void setInput(CmlSourceUnit input) {
	this.input = input;
    }

}
