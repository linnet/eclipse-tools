package dk.kamstruplinnet.callers.actions;

import dk.kamstruplinnet.callers.CallersConstants;
import dk.kamstruplinnet.callers.CallersPlugin;
import dk.kamstruplinnet.callers.views.CallersView;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author jl
 */
public class SearchScopeActionGroup extends ActionGroup {
    private final static String MENU_TEXT = "Search scope";
    protected SearchScopeAction mSelectedAction = null;
    protected String mSelectedWorkingSetName = null;
    
    private SearchScopeWorkspaceAction mSearchScopeWorkspaceAction;
    private SelectWorkingSetAction mSelectWorkingSetAction;
    private SearchScopeHierarchyAction mSearchScopeHierarchyAction;
    private SearchScopeProjectAction mSearchScopeProjectAction;
    private CallersView mView;

    public SearchScopeActionGroup(CallersView view) {
        this.mView = view;
        createActions();
    }

    /**
     * @param view
     */
    private void createActions() {
        mSearchScopeWorkspaceAction = new SearchScopeWorkspaceAction();
        mSelectWorkingSetAction = new SelectWorkingSetAction();
        mSearchScopeHierarchyAction = new SearchScopeHierarchyAction();
        mSearchScopeProjectAction = new SearchScopeProjectAction();

        setSelected(mSearchScopeWorkspaceAction);
    }

    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        fillViewMenu(actionBars.getMenuManager());
    }

    void fillViewMenu(IMenuManager menu) {
        menu.add(new GroupMarker(CallersConstants.GROUP_SEARCH_SCOPE));
        fillContextMenu(menu);
    }

    public void fillContextMenu(IMenuManager menu) {
        MenuManager javaSearchMM = new MenuManager(MENU_TEXT,
                CallersConstants.GROUP_SEARCH_SCOPE);

        javaSearchMM.addMenuListener(new IMenuListener() {
            /* (non-Javadoc)
             * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
             */
            public void menuAboutToShow(IMenuManager manager) {
                fillSearchActions(manager);
            }

        });
        menu.appendToGroup(CallersConstants.GROUP_SEARCH_SCOPE, javaSearchMM);


    }

    protected void fillSearchActions(IMenuManager javaSearchMM) {
        javaSearchMM.removeAll();
        Action[] actions = getActions();
        
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
        
            if (action.isEnabled()) {
                javaSearchMM.add(action);
            }
        }
        
        javaSearchMM.setVisible(!javaSearchMM.isEmpty());
    }

    protected void setSelected(SearchScopeAction newSelection) {
        if (newSelection instanceof SearchScopeWorkingSetAction) {
            mSelectedWorkingSetName = ((SearchScopeWorkingSetAction) newSelection).getWorkingSet().getName();
        } else {
            mSelectedWorkingSetName = null;
        }

        mSelectedAction = newSelection;
    }

    /**
     * @return SearchScopeAction[]
     */
    private Action[] getActions() {
        List actions = new ArrayList();
        addAction(actions, mSearchScopeWorkspaceAction);
        addAction(actions, mSearchScopeProjectAction);
        addAction(actions, mSearchScopeHierarchyAction);
        addAction(actions, mSelectWorkingSetAction);

        IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
                                                         .getWorkingSetManager();
        IWorkingSet[] sets = workingSetManager.getRecentWorkingSets();

        for (int i = 0; i < sets.length; i++) {
            SearchScopeWorkingSetAction workingSetAction = new SearchScopeWorkingSetAction(sets[i]);

            if (sets[i].getName().equals(mSelectedWorkingSetName)) {
                workingSetAction.setChecked(true);
            }
            actions.add(workingSetAction);
        }

        return (Action[]) actions.toArray(new Action[actions.size()]);
    }

    private void addAction(List actions, Action action) {
        if (action == mSelectedAction) {
            action.setChecked(true);
        } else {
            action.setChecked(false);
        }
        actions.add(action);
    }

    protected IWorkingSetManager getWorkingSetManager() {
        IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
                                                           .getWorkingSetManager();
        return workingSetManager;
    }

    /**
     * @param set
     * @param b
     */
    protected void setActiveWorkingSet(IWorkingSet set) {
        if (set != null) {
            mSelectedWorkingSetName = set.getName();
            mSelectedAction = new SearchScopeWorkingSetAction(set);
        } else {
            mSelectedWorkingSetName = null;
            mSelectedAction = null;
        }
    }

    /**
     * @return
     */
    protected IWorkingSet getActiveWorkingSet() {
        if (mSelectedWorkingSetName != null) {
            return getWorkingSetManager().getWorkingSet(mSelectedWorkingSetName);
        }            
        return null;
    }

    /**
     * @return CallersView
     */
    protected CallersView getView() {
        return mView;
    }

    /**
     * @return IJavaSearchScope
     */
    public IJavaSearchScope getSearchScope() {
        if (mSelectedAction != null) {
            return mSelectedAction.getSearchScope();
        }
        return null;
    }

    private abstract class SearchScopeAction extends Action {
        public SearchScopeAction(String text) {
            super(text);
        }

        public void run() {
            setSelected(this);
        }

        public abstract IJavaSearchScope getSearchScope();
    }

    private class SearchScopeWorkspaceAction extends SearchScopeAction {
        public SearchScopeWorkspaceAction() {
            super("&Workspace");
            setToolTipText("Search for calls in workspace");
        }

        public IJavaSearchScope getSearchScope() {
            return SearchEngine.createWorkspaceScope();
        }
    }

    private class SearchScopeWorkingSetAction extends SearchScopeAction {
        private IWorkingSet mWorkingSet;

        public SearchScopeWorkingSetAction(IWorkingSet workingSet) {
            super(workingSet.getName());
            setToolTipText("Search for calls in working set");

            this.mWorkingSet = workingSet;
        }

        /**
         * 
         */
        public IWorkingSet getWorkingSet() {
            return mWorkingSet;
        }

        public IJavaSearchScope getSearchScope() {
            return SearchEngine.createJavaSearchScope(getJavaElements(
                    mWorkingSet.getElements()));
        }

        /**
         * @param adaptables
         * @return IResource[]
         */
        private IJavaElement[] getJavaElements(IAdaptable[] adaptables) {
            Collection result = new ArrayList();

            for (int i = 0; i < adaptables.length; i++) {
                IJavaElement element = (IJavaElement) adaptables[i].getAdapter(IJavaElement.class);

                if (element != null) {
                    result.add(element);
                }
            }

            return (IJavaElement[]) result.toArray(new IJavaElement[result.size()]);
        }
    }

    private class SelectWorkingSetAction extends Action {
        public SelectWorkingSetAction() {
            super("Working &Set...");
            setToolTipText("Select working set");
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            IWorkingSetManager workingSetManager = getWorkingSetManager();
            IWorkingSetSelectionDialog dialog = workingSetManager.createWorkingSetSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), false);
            IWorkingSet workingSet= getActiveWorkingSet();
            if (workingSet != null) {
                dialog.setSelection(new IWorkingSet[]{workingSet});
            }

            if (dialog.open() == Window.OK) {
                IWorkingSet[] result= dialog.getSelection();
                if (result != null && result.length > 0) {
                    setActiveWorkingSet(result[0]);
                    workingSetManager.addRecentWorkingSet(result[0]);
                }
                else
                    setActiveWorkingSet(null);
            }
            
        }
    }

    private class SearchScopeHierarchyAction extends SearchScopeAction {
        public SearchScopeHierarchyAction() {
            super("&Hierarchy");
            setToolTipText("Search for calls in hierarchy");
        }

        public IJavaSearchScope getSearchScope() {
            try {
                IMethod method = getView().getMethod();
                if (method != null) {
                    return SearchEngine.createHierarchyScope(method.getDeclaringType());
                } else {
                    return null;
                }
            } catch (JavaModelException e) {
                CallersPlugin.logError("Error creating hierarchy search scope", e);
            }

            return null;
        }
    }

    private class SearchScopeProjectAction extends SearchScopeAction {
        public SearchScopeProjectAction() {
            super("&Project");
            setToolTipText("Search for calls in project");
        }

        public IJavaSearchScope getSearchScope() {
            IMethod method = getView().getMethod();
            IJavaProject project = null;
            if (method != null) {
                project = method.getJavaProject();
            }
            if (project != null) {
                return SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, false);
            } else {
                return null;
            }
        }
    }
}
