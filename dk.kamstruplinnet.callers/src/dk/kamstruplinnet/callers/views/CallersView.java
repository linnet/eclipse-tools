package dk.kamstruplinnet.callers.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import dk.kamstruplinnet.callers.CallersConstants;
import dk.kamstruplinnet.callers.CallersPlugin;
import dk.kamstruplinnet.callers.actions.CallersActionGroup;
import dk.kamstruplinnet.callers.actions.SearchScopeActionGroup;
import dk.kamstruplinnet.callers.search.CallLocation;
import dk.kamstruplinnet.callers.search.CalleeMethodWrapper;
import dk.kamstruplinnet.callers.search.CallerMethodWrapper;
import dk.kamstruplinnet.callers.search.MethodCall;
import dk.kamstruplinnet.callers.search.MethodWrapper;
import dk.kamstruplinnet.callers.util.CallersUtility;


/**
 * This is the main view for the callers plugin. It builds a tree of callers/callees
 * and allows the user to double click an entry to go to the selected method.
 *
 * @author Jesper Kamstrup Linnet
 */
public class CallersView extends ViewPart implements IDoubleClickListener,
    ISelectionChangedListener, IPropertyChangeListener {

    public static final String CALLERS_VIEW_ID = "dk.kamstruplinnet.callers.view";
    private static final String TAG_CALLER_MODE = "caller";
    private static final int SHOW_CALLERS = 1;
    private static final int SHOW_CALLEES = 2;
    private static final String TAG_CALL_DETAIL_MODE = "details";
    private static final String TAG_CALL_DETAIL_TREE_WEIGHT = "weight";
    private static final int SHOW_DETAILS = 3;
    private static final int SHOW_NO_DETAILS = 4;
    private Menu mTreeContextMenu;
    private Menu mListContextMenu;

    private CallersActionGroup mActions;
    private SearchScopeActionGroup mSearchScopeActions;

    private CalleeMethodWrapper mCalleeRoot;
    private CallerMethodWrapper mCallerRoot;
    private IMemento mMemento;

    /**
     * If true the caller tree is shown. If false the callee tree is shown.
     */
    private boolean mShowCallers = true;
    private IMethod mShownMethod;
    private TreeViewer mTreeViewer;
    private ListViewer mListViewer;
    private SashForm mSashForm;
    private boolean mShowCallDetails;
    private int mInitialTreeWeight;

    private ISelectionProvider mSelectionProvider;
    
    private List mMethodHistory;
    /**
     * A selection provider/listener for this view.
     * It is a selection provider fo this view's site.
     */
    protected class SelectionProvider implements ISelectionProvider {
        /**
         * Selection change listeners.
         */
        private ListenerList selectionChangedListeners = new ListenerList();
                
        /* (non-Javadoc)
         * Method declared on ISelectionProvider.
         */
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            selectionChangedListeners.add(listener);    
        }
        /* (non-Javadoc)
         * Method declared on ISelectionProvider.
         */
        public ISelection getSelection() {
            return CallersView.this.getSelection();
        }
        /* (non-Javadoc)
         * Method declared on ISelectionProvider.
         */
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            selectionChangedListeners.remove(listener);
        }
        /* (non-Javadoc)
         * Method declared on ISelectionChangedListener.
         */
        public void selectionChanged(final SelectionChangedEvent event) {
            // pass on the notification to listeners
            Object[] listeners = selectionChangedListeners.getListeners();
            for (int i = 0; i < listeners.length; ++i) {
                final ISelectionChangedListener l = (ISelectionChangedListener)listeners[i];
                Platform.run(new SafeRunnable() {
                    public void run() {
                        l.selectionChanged(event);
                    }
                    public void handleException(Throwable e) {
                        super.handleException(e);
                        //If and unexpected exception happens, remove it
                        //to make sure the workbench keeps running.
                        removeSelectionChangedListener(l);
                    }
                });     
            }
        }
        /* (non-Javadoc)
         * Method declared on ISelectionProvider.
         */
        public void setSelection(ISelection selection) {
        }
    }


    public CallersView() {
        super();
    }

    public void createPartControl(Composite parent) {
        setTitle("Call Hierarchy");

        mSelectionProvider = new SelectionProvider();
        getSite().setSelectionProvider(mSelectionProvider);
        
        mMethodHistory = new ArrayList();
        
        createSashForm(parent);
        createTreeViewer(mSashForm);
        createLocationList(mSashForm);
        setCallDetailLayout();

        createTreePopupMenu();
        createListPopupMenu();
        
        initKeyListener();
        makeActions();
        fillActionBars();
    }

    /**
     *
     */
    private void setCallDetailLayout() {
        setCallDetailOrientation();
        setCallDetailWidth();
        showOrHideCallDetailsView();
    }

    private void setCallDetailWidth() {
        int treeWidth = getInitialTreeWeight();
        mSashForm.setWeights(new int[] { treeWidth, 100 - treeWidth });
    }

    private int getInitialTreeWeight() {
        if ((mInitialTreeWeight == 0) || (mInitialTreeWeight >= 100)) {
            return 75;
        } else {
            return mInitialTreeWeight;
        }
    }

    private void setCallDetailOrientation() {
        mSashForm.setOrientation(CallersPlugin.getDefault().getCallDetailOrientation());
    }

    private void createSashForm(Composite parent) {
        mSashForm = new SashForm(parent, SWT.NONE);
    }

    /**
     * @param parent
     */
    private void createLocationList(Composite parent) {
        mListViewer = new ListViewer(parent);

        mListViewer.setContentProvider(new LocationContentProvider());
        mListViewer.setLabelProvider(new LocationLabelProvider());
        mListViewer.setInput(new ArrayList());
        mListViewer.addDoubleClickListener(this);
    }

    private void createTreePopupMenu() {
        MenuManager menuMgr = createPopupMenu();

        mTreeContextMenu = menuMgr.createContextMenu(mTreeViewer.getControl());
        mTreeViewer.getControl().setMenu(mTreeContextMenu);

        // Register viewer with site. This must be done before making the actions.
        getSite().registerContextMenu(menuMgr, mTreeViewer);
    }
    
    private void createListPopupMenu() {
        MenuManager menuMgr = createPopupMenu();

        mListContextMenu = menuMgr.createContextMenu(mListViewer.getControl());
        mListViewer.getControl().setMenu(mListContextMenu);

        // Register viewer with site. This must be done before making the actions.
        getSite().registerContextMenu(menuMgr, mListViewer);
    }
    
    /**
     *
     */
    private MenuManager createPopupMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            /* (non-Javadoc)
             * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
             */
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        
        return menuMgr;
    }

    private void createTreeViewer(Composite parent) {
        mTreeViewer = new TreeViewer(parent, SWT.SIMPLE);
        mTreeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        mTreeViewer.setUseHashlookup(true);
        mTreeViewer.setContentProvider(new CallersContentProvider());
        mTreeViewer.setLabelProvider(new CallersLabelProvider());
        mTreeViewer.setInput(getInitalInput());
        mTreeViewer.addDoubleClickListener(this);
        mTreeViewer.addSelectionChangedListener(this);
    }

    /**
     * @return ISelectionChangedListener
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Object selectedElement = selection.getFirstElement();

            if (selectedElement instanceof MethodWrapper) {
                MethodWrapper methodWrapper = (MethodWrapper) selectedElement;
                updateDetailsView(methodWrapper);
            } else {
                updateDetailsView(null);
            }
        }
    }

    /**
     *
     */
    public void dispose() {
        disposeMenu(mTreeContextMenu);
        disposeMenu(mListContextMenu);

        CallersPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);

        super.dispose();
    }

    private void disposeMenu(Menu contextMenu) {
        if ((contextMenu != null) && !contextMenu.isDisposed()) {
            contextMenu.dispose();
        }
    }

    private void fillActionBars() {
        IActionBars actionBars = getActionBars();
        mActions.fillActionBars(actionBars);
        mSearchScopeActions.fillActionBars(actionBars);
    }

    private IActionBars getActionBars() {
        return getViewSite().getActionBars();
    }

    private CalleeMethodWrapper getCalleeRoot() {
        if (mCalleeRoot == null) {
            mCalleeRoot = new CalleeMethodWrapper(null, new MethodCall(mShownMethod));
        }

        return mCalleeRoot;
    }

    private CallerMethodWrapper getCallerRoot() {
        if (mCallerRoot == null) {
            mCallerRoot = new CallerMethodWrapper(null, new MethodCall(mShownMethod));
        }

        return mCallerRoot;
    }

    /**
     * Method getCompilationUnit.
     * @param editor
     * @return ICompilationUnit
     */
    private ICompilationUnit getCompilationUnit(IEditorPart editor) {
        IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
        ICompilationUnit unit = manager.getWorkingCopy(editor.getEditorInput());

        return unit;
    }

    /**
     * Double click listener which jumps to the method in the source code.
     *
     * @return IDoubleClickListener
     */
    public void doubleClick(DoubleClickEvent event) {
        jumpToSelection(event.getSelection());
    }

    public void jumpToSelection(ISelection selection) {
        try {
            if ((selection != null) && selection instanceof IStructuredSelection) {
                Object structuredSelection = ((IStructuredSelection) selection).getFirstElement();

                if (structuredSelection instanceof MethodWrapper) {
                    MethodWrapper methodWrapper = (MethodWrapper) structuredSelection;
                    CallLocation firstCall = methodWrapper.getMethodCall().getFirstCallLocation();

                    if (firstCall != null) {
                        jumpToLocation(firstCall);
                    } else {
                        jumpToMethod(methodWrapper.getMethod());
                    }
                } else if (structuredSelection instanceof CallLocation) {
                    jumpToLocation((CallLocation) structuredSelection);
                }
            }
        } catch (Exception e) {
            CallersPlugin.logError("Error handling double click", e);
        }
    }

    public void jumpToDeclarationOfSelection() {
        ISelection selection = null;
        try {
            if (mTreeViewer.getControl().isFocusControl()) {
                selection = mTreeViewer.getSelection();
            } else if (mListViewer.getControl().isFocusControl()) {
                selection = mListViewer.getSelection();
            }
                            
            if ((selection != null) && selection instanceof IStructuredSelection) {
                Object structuredSelection = ((IStructuredSelection) selection).getFirstElement();

                if (structuredSelection instanceof MethodWrapper) {
                    MethodWrapper methodWrapper = (MethodWrapper) structuredSelection;

                    jumpToMethod(methodWrapper.getMethod());
                } else if (structuredSelection instanceof CallLocation) {
                    jumpToMethod(((CallLocation) structuredSelection).getCalledMethod());
                }
            }
        } catch (Exception e) {
            CallersPlugin.logError("Error handling double click", e);
        }
    }

    private void jumpToMethod(IMethod method) {
        if (method != null) {
            try {
                IEditorPart methodEditor = CallersUtility.openInEditor(method, CallersPlugin.getDefault().getActivateEditorOnSelect());
                JavaUI.revealInEditor(methodEditor, (IJavaElement) method);
            } catch (JavaModelException e) {
                CallersPlugin.logError("Error getting underlying resource", e);
            } catch (PartInitException e) {
                CallersPlugin.logError("Error opening editor", e);
            }
        }
    }

    private void jumpToLocation(CallLocation callLocation) {
        try {
            IEditorPart methodEditor = CallersUtility.openInEditor(callLocation.getMethod(), CallersPlugin.getDefault().getActivateEditorOnSelect());

            if (methodEditor instanceof ITextEditor) {
                ITextEditor editor = (ITextEditor) methodEditor;
                editor.selectAndReveal(callLocation.getStart(),
                    (callLocation.getEnd() - callLocation.getStart()));
            }
        } catch (JavaModelException e) {
            CallersPlugin.logError("Error getting underlying resource", e);
        } catch (PartInitException e) {
            CallersPlugin.logError("Error opening editor", e);
        } catch (Exception e) {
            CallersPlugin.logError("Unknown error jumping to search result", e);
        }
    }

    /**
     * Method getInitalInput.
     * @return Object
     */
    private Object getInitalInput() {
        return TreeRoot.EMPTY_TREE;
    }

    /**
     * Returns the current selection.
     */
    protected ISelection getSelection() {
        return mTreeViewer.getSelection();
    }

    /**
     * @return int
     */
    private int getShowCallersAsInt() {
        if (isShowingCallers()) {
            return SHOW_CALLERS;
        } else {
            return SHOW_CALLEES;
        }
    }

    /**
     * @return int
     */
    private int getShowCallDetailsAsInt() {
        if (isShowingCallDetails()) {
            return SHOW_DETAILS;
        } else {
            return SHOW_NO_DETAILS;
        }
    }

    /**
     * Wraps the root of a MethodWrapper tree in a dummy root in order to show
     * it in the tree.
     *
     * @param root The root of the MethodWrapper tree.
     * @return A new MethodWrapper which is a dummy root above the specified root.
     */
    private TreeRoot getWrappedRoot(MethodWrapper root) {
        TreeRoot dummyRoot = new TreeRoot(root);

        return dummyRoot;
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void init(IViewSite site, IMemento memento)
        throws PartInitException {
        super.init(site, memento);
        mMemento = memento;
        restoreCallerState(memento);
        restoreCallDetailState(memento);

        CallersPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(CallersConstants.PREF_DETAIL_SASH_ORIENTATION)) {
            setCallDetailOrientation();
        } else if (event.getProperty().equals(CallersConstants.PREF_JAVA_LABEL_FORMAT)) {
            updateJavaLabelFormat();
        }
    }

    /**
     *
     */
    private void initKeyListener() {
        KeyListener keyListener = new KeyAdapter() {
                public void keyReleased(KeyEvent event) {
                    handleKeyEvent(event);
                }
            };

        mSashForm.addKeyListener(keyListener);
        mTreeViewer.getControl().addKeyListener(keyListener);
        mListViewer.getControl().addKeyListener(keyListener);
    }
    
    protected void handleKeyEvent(KeyEvent event) {
        mActions.handleKeyEvent(event);
    }
    
    public boolean isShowingCallers() {
        return mShowCallers;
    }

    public boolean isShowingCallDetails() {
        return mShowCallDetails;
    }

    private boolean isWithinMethodRange(int offset, IMethod method)
        throws JavaModelException {
        ISourceRange range = method.getSourceRange();

        return ((offset >= range.getOffset()) &&
        (offset <= (range.getOffset() + range.getLength())));
    }

    /**
     *
     */
    private void makeActions() {
        mActions = new CallersActionGroup(this);
        mSearchScopeActions = new SearchScopeActionGroup(this);
    }

    protected void fillContextMenu(IMenuManager manager) {
        manager.add(new GroupMarker(CallersConstants.GROUP_MAIN));
        manager.add(new GroupMarker(CallersConstants.GROUP_SEARCH_SCOPE));

        mActions.setContext(new ActionContext(getSelection()));
        mActions.fillContextMenu(manager);
        mActions.setContext(null);
        
        mActions.setContext(new ActionContext(getSelection()));
        mSearchScopeActions.fillContextMenu(manager);
        mActions.setContext(null);

        manager.add(new GroupMarker(IContextMenuConstants.GROUP_ADDITIONS));
    }

    /**
     *
     */
    public void refresh() {
        setCalleeRoot(null);
        setCallerRoot(null);

        updateView();
    }

    private void restoreCallerState(IMemento memento) {
        Integer state = null;

        if (memento != null) {
            state = memento.getInteger(TAG_CALLER_MODE);
        }

        // If no memento try an restore from preference store
        if (state == null) {
            IPreferenceStore store = CallersPlugin.getDefault().getPreferenceStore();
            state = new Integer(store.getInt(TAG_CALLER_MODE));
        }

        if (state.intValue() == SHOW_CALLERS) {
            mShowCallers = true;
        } else if (state.intValue() == SHOW_CALLEES) {
            mShowCallers = false;
        } else {
            mShowCallers = true;
        }
    }

    /**
     * @param memento
     */
    private void restoreCallDetailState(IMemento memento) {
        Integer state = null;
        Integer treeWeight = null;

        if (memento != null) {
            state = memento.getInteger(TAG_CALL_DETAIL_MODE);
            treeWeight = memento.getInteger(TAG_CALL_DETAIL_TREE_WEIGHT);
        }

        // If no memento try an restore from preference store
        if (state == null) {
            IPreferenceStore store = CallersPlugin.getDefault().getPreferenceStore();
            state = new Integer(store.getInt(TAG_CALL_DETAIL_MODE));
        }

        if (treeWeight == null) {
            IPreferenceStore store = CallersPlugin.getDefault().getPreferenceStore();
            treeWeight = new Integer(store.getInt(TAG_CALL_DETAIL_TREE_WEIGHT));
        }

        if (state.intValue() == SHOW_DETAILS) {
            mShowCallDetails = true;
        } else if (state.intValue() == SHOW_NO_DETAILS) {
            mShowCallDetails = false;
        } else {
            mShowCallDetails = false;
        }

        if (treeWeight != null) {
            mInitialTreeWeight = treeWeight.intValue();
        }
    }

    /**
     * @param memento
     */
    private void saveCallerState(IMemento memento) {
        if (memento != null) {
            memento.putInteger(TAG_CALLER_MODE, getShowCallersAsInt());
        } else {
            //if memento is null save in preference store
            IPreferenceStore store = CallersPlugin.getDefault().getPreferenceStore();
            store.setValue(TAG_CALLER_MODE, getShowCallersAsInt());
        }
    }

    private void saveCallDetailState(IMemento memento) {
        if (memento != null) {
            memento.putInteger(TAG_CALL_DETAIL_MODE, getShowCallDetailsAsInt());
            memento.putInteger(TAG_CALL_DETAIL_TREE_WEIGHT, getCallDetailTreeWeight());
        } else {
            //if memento is null save in preference store
            IPreferenceStore store = CallersPlugin.getDefault().getPreferenceStore();
            store.setValue(TAG_CALL_DETAIL_MODE, getShowCallDetailsAsInt());
            store.setValue(TAG_CALL_DETAIL_TREE_WEIGHT, getCallDetailTreeWeight());
        }
    }

    private int getCallDetailTreeWeight() {
        int treeWidth = mSashForm.getWeights()[0];
        int totalWidth = treeWidth + mSashForm.getWeights()[1];

        if (totalWidth > 0) {
            return (treeWidth * 100) / totalWidth;
        } else {
            return 75;
        }
    }

    public void saveState(IMemento memento) {
        if (mTreeViewer == null) {
            // part has not been created
            if (mMemento != null) { //Keep the old state;
                memento.putMemento(mMemento);
            }

            return;
        }

        saveCallerState(memento);
        saveCallDetailState(memento);
    }

    private void setCalleeRoot(CalleeMethodWrapper calleeRoot) {
        this.mCalleeRoot = calleeRoot;
    }

    private void setCallerRoot(CallerMethodWrapper callerRoot) {
        this.mCallerRoot = callerRoot;
    }

    public void setFocus() {
        mTreeViewer.getTree().setFocus();
    }

    /**
     * Method setMethod.
     * @param method
     */
    public void setMethod(IMethod method) {
        if (method != null && !method.equals(mShownMethod)) {
            addHistoryEntry(method);
        }
        this.mShownMethod = method;

        refresh();
    }
    
    public IMethod getMethod() {
        if ((getSelection() != null) && getSelection() instanceof IStructuredSelection) {
            Object structuredSelection = ((IStructuredSelection) getSelection()).getFirstElement();

            if (structuredSelection instanceof MethodWrapper) {
                return ((MethodWrapper) structuredSelection).getMethod();
            }
        }
        return null;
    }

    public IJavaSearchScope getSearchScope() {
        return mSearchScopeActions.getSearchScope();
    }
    
    /**
     * @param show
     */
    public void setShowCallers(boolean show) {
        mShowCallers = show;
        updateView();
    }

    public void setShowCallDetails(boolean show) {
        mShowCallDetails = show;
        showOrHideCallDetailsView();
    }

    /**
     * Method updateCurrentMethod.
     * @param editor
     */
    public void updateCurrentMethod() {
        try {
            IEditorPart editor = getSite().getPage().getActiveEditor();

            if (editor instanceof JavaEditor) {
                JavaEditor javaEditor = (JavaEditor) editor;
				IJavaElement input = SelectionConverter.getInput(javaEditor);
				if (!ActionUtil.isProcessable(getSite().getShell(), input))
					return;

				IJavaElement[] elements =
					SelectionConverter.codeResolveOrInputHandled(
						javaEditor,
						getSite().getShell(),
						"Error looking up method");
				if (elements == null)
					return;
				List candidates = new ArrayList(elements.length);
				for (int i = 0; i < elements.length; i++) {
					IJavaElement[] resolvedElements =
						getCandidates(elements[i]);
					if (resolvedElements != null)
						candidates.addAll(Arrays.asList(resolvedElements));
				}
				if (candidates.isEmpty()) {
                    ISelection selection = editor.getEditorSite().getSelectionProvider()
                                                          .getSelection();

                    if (selection instanceof ITextSelection) {
    					IJavaElement enclosingMethod =
    						getEnclosingMethod(input, (ITextSelection) selection);
    					if (enclosingMethod != null) {
    						candidates.add(enclosingMethod);
    					}
                    }
                }
                if (candidates != null && candidates.size() == 1) {
                    IJavaElement firstElement = (IJavaElement) candidates.iterator().next();
                    if (firstElement instanceof IMethod) {
                        IMethod method = (IMethod) firstElement;     
                        setMethod(method);
                    }
                }
			}

/*
            if (editor instanceof JavaEditor) {
                IJavaElement[] elements = SelectionConverter.codeResolve((JavaEditor) editor);

                if (elements != null && elements.length == 1 && elements[0] instanceof IMethod) {
                    IMethod method = (IMethod) elements[0];     
                    setMethod(method);
                }
            }
*/
        } catch (Exception e) {
            CallersPlugin.logError("Error switching to callers view", e);
        }
    }

    /**
     * Converts the input to a possible input candidates
     */ 
    private IJavaElement[] getCandidates(Object input) {
        if (!(input instanceof IJavaElement)) {
            return null;
        }
        IJavaElement elem= (IJavaElement) input;
        if (elem.getElementType() == IJavaElement.METHOD) {
            return new IJavaElement[] { elem };
        }
        return null;    
    }

    private IJavaElement getEnclosingMethod(IJavaElement input, ITextSelection selection) {
        IJavaElement enclosingElement= null;
        try {
            switch (input.getElementType()) {
                case IJavaElement.CLASS_FILE :
                    IClassFile classFile= (IClassFile) input.getAncestor(IJavaElement.CLASS_FILE);
                    if (classFile != null) {
                        enclosingElement= classFile.getElementAt(selection.getOffset());
                    }
                    break;
                case IJavaElement.COMPILATION_UNIT :
                    ICompilationUnit cu= (ICompilationUnit) input.getAncestor(IJavaElement.COMPILATION_UNIT);
                    if (cu != null) {
                        enclosingElement= cu.getElementAt(selection.getOffset());
                    }
                    break;
            }
            if (enclosingElement != null && enclosingElement.getElementType() == IJavaElement.METHOD) {
                return enclosingElement;
            }
        } catch (JavaModelException e) {
            CallersPlugin.logError("Error find method", e);
        }

        return null;
    }


    /**
     * Method updateView.
     */
    private void updateView() {
        try {
            if ((mShownMethod != null)) {
                CallersPlugin.getDefault().setSearchScope(getSearchScope());
                
                if (mShowCallers) {
                    setTitle("Calls to method");
                    mTreeViewer.setInput(getWrappedRoot(getCallerRoot()));
                } else {
                    setTitle("Calls from method");
                    mTreeViewer.setInput(getWrappedRoot(getCalleeRoot()));
                }

                showOrHideCallDetailsView();

                mTreeViewer.expandToLevel(2);
                mTreeViewer.getTree().setFocus();
                mTreeViewer.getTree().setSelection(new TreeItem[] {
                        mTreeViewer.getTree().getItems()[0]
                    });
                updateDetailsView(null);
            }
        } catch (Exception e) {
            CallersPlugin.logError("Error updating view", e);
        }
    }

    private void updateDetailsView(MethodWrapper methodWrapper) {
        if (methodWrapper != null) {
            mListViewer.setInput(methodWrapper.getMethodCall().getCallLocations());
        } else {
            mListViewer.setInput("");
        }
    }

    private void showOrHideCallDetailsView() {
        if (mShowCallDetails) {
            mSashForm.setMaximizedControl(null);
        } else {
            mSashForm.setMaximizedControl(mTreeViewer.getControl());
        }
    }

    private void updateJavaLabelFormat() {
        ((CallersLabelProvider) mTreeViewer.getLabelProvider()).updateJavaLabelSettings();
        mTreeViewer.refresh();
    }

        
    /**
     * Adds the entry if new. Inserted at the beginning of the history entries list.
     */     
    private void addHistoryEntry(IJavaElement entry) {
        if (mMethodHistory.contains(entry)) {
            mMethodHistory.remove(entry);
        }
        mMethodHistory.add(0, entry);
        mActions.setHistoryEnabled(true);
    }
    
    private void updateHistoryEntries() {
        for (int i= mMethodHistory.size() - 1; i >= 0; i--) {
            IMethod method = (IMethod) mMethodHistory.get(i);
            if (!method.exists()) {
                mMethodHistory.remove(i);
            }
        }
        mActions.setHistoryEnabled(!mMethodHistory.isEmpty());
    }
    
    /**
     * Goes to the selected entry, without updating the order of history entries.
     */ 
    public void gotoHistoryEntry(IMethod entry) {
        if (mMethodHistory.contains(entry)) {
            setMethod(entry);
        }
    }   
    
    /**
     * Gets all history entries.
     */
    public IMethod[] getHistoryEntries() {
        if (mMethodHistory.size() > 0) {
            updateHistoryEntries();
        }
        return (IMethod[]) mMethodHistory.toArray(new IMethod[mMethodHistory.size()]);
    }
    
    /**
     * Sets the history entries
     */
    public void setHistoryEntries(IMethod[] elems) {
        mMethodHistory.clear();
        for (int i= 0; i < elems.length; i++) {
            mMethodHistory.add(elems[i]);
        }
        updateHistoryEntries();
    }
}
