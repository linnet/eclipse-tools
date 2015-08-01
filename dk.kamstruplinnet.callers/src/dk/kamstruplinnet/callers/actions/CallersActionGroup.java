package dk.kamstruplinnet.callers.actions;

import dk.kamstruplinnet.callers.CallersConstants;
import dk.kamstruplinnet.callers.CallersPlugin;
import dk.kamstruplinnet.callers.CallersPluginImages;
import dk.kamstruplinnet.callers.search.MethodWrapper;
import dk.kamstruplinnet.callers.util.CallersUtility;
import dk.kamstruplinnet.callers.views.CallersView;

import org.eclipse.jdt.core.IMethod;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;


/**
 * @author jl
 */
public class CallersActionGroup extends ActionGroup {
    private ToggleCallersAction mToggleCallersAction;
    private ToggleCallersAction mToggleCalleesAction;
    private ToggleCallDetailsAction mToggleCallDetailsAction;
    private RefreshAction mRefreshAction;
    private JumpToDeclarationAction mJumpToDeclarationAction;
    private FocusOnSelectionAction mFocusOnSelectionAction;
    private HistoryDropDownAction mHistoryDropDownAction;
    private CallersView mView;

    public CallersActionGroup(CallersView view) {
        this.mView = view;
        createActions();
    }

    /**
     * @param view
     */
    private void createActions() {
        mToggleCallersAction = new ToggleCallersAction("Show Calls &To Method (F8)",
                "Show Calls To Method (F8)", CallersPluginImages.create("CallUp.gif"),
                true);
        mToggleCalleesAction = new ToggleCallersAction("Show Calls &From Method (F9)",
                "Show Calls From Method (F9)",
                CallersPluginImages.create("CallDown.gif"), false);
        mToggleCallDetailsAction = new ToggleCallDetailsAction();
        mRefreshAction = new RefreshAction();
        mJumpToDeclarationAction = new JumpToDeclarationAction();
        mFocusOnSelectionAction = new FocusOnSelectionAction();
        mHistoryDropDownAction = new HistoryDropDownAction(getView());
        mHistoryDropDownAction.setEnabled(false);
    }

    protected void resetOtherChecked(ToggleCallersAction checkedAction) {
        IAction otherAction;

        if (checkedAction == mToggleCallersAction) {
            otherAction = mToggleCalleesAction;
        } else {
            otherAction = mToggleCallersAction;
        }

        otherAction.setChecked(false);
    }

    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        fillToolBar(actionBars.getToolBarManager());
        fillViewMenu(actionBars.getMenuManager());
    }

    void fillToolBar(IToolBarManager toolBar) {
        toolBar.removeAll();

        toolBar.add(mHistoryDropDownAction);
        toolBar.add(mRefreshAction);
        toolBar.add(mToggleCallersAction);
        toolBar.add(mToggleCalleesAction);
        toolBar.add(mToggleCallDetailsAction);
    }

    void fillViewMenu(IMenuManager menu) {
        menu.add(mRefreshAction);
        menu.add(mFocusOnSelectionAction);
        menu.add(mToggleCallersAction);
        menu.add(mToggleCalleesAction);
        menu.add(mToggleCallDetailsAction);

        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    public void fillContextMenu(IMenuManager menu) {
        menu.appendToGroup(CallersConstants.GROUP_MAIN, mJumpToDeclarationAction);
        menu.appendToGroup(CallersConstants.GROUP_MAIN, mFocusOnSelectionAction);
        menu.appendToGroup(CallersConstants.GROUP_MAIN, mToggleCallDetailsAction);
        super.fillContextMenu(menu);
    }

    public void handleKeyEvent(KeyEvent event) {
        if (event.stateMask == 0) {
            if (event.keyCode == SWT.F3) {
                if ((mJumpToDeclarationAction != null) &&
                            mJumpToDeclarationAction.isEnabled()) {
                    mJumpToDeclarationAction.run();

                    return;
                }
            } else if (event.keyCode == SWT.F5) {
                if ((mRefreshAction != null) && mRefreshAction.isEnabled()) {
                    mRefreshAction.run();

                    return;
                }
            } else if (event.keyCode == SWT.F8) {
                if ((mToggleCallersAction != null) && mToggleCallersAction.isEnabled()) {
                    mToggleCallersAction.run();

                    return;
                }
            } else if (event.keyCode == SWT.F9) {
                if ((mToggleCalleesAction != null) && mToggleCalleesAction.isEnabled()) {
                    mToggleCalleesAction.run();

                    return;
                }
            }
        }
    }

    private class ToggleCallersAction extends Action {
        private boolean mShowCallers;

        /**
         * @param text
         * @param image
         */
        public ToggleCallersAction(String text, String tooltip, ImageDescriptor image,
            boolean showCallers) {
            super(text, image);
            this.mShowCallers = showCallers;

            setToolTipText(tooltip);
            setChecked(showCallers ? getView().isShowingCallers()
                                   : (!getView().isShowingCallers()));
        }

        public void run() {
            setChecked(true);
            resetOtherChecked(this);
            getView().setShowCallers(mShowCallers);
        }
    }

    private class ToggleCallDetailsAction extends Action {
        public ToggleCallDetailsAction() {
            super("Show Call &Details", CallersPluginImages.create("toggle_details.gif"));
            setToolTipText("Show Call Details");

            setChecked(getView().isShowingCallDetails());
        }

        /*
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            setCallDetails(!getView().isShowingCallDetails());
        }

        private void setCallDetails(boolean show) {
            setChecked(show);
            getView().setShowCallDetails(show);
        }
    }

    private class RefreshAction extends Action {
        public RefreshAction() {
            super("&Refresh@F5", CallersPluginImages.create("refresh.gif"));
            setToolTipText("&Refresh the view (F5)");
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            try {
                getView().refresh();
            } catch (RuntimeException e) {
                CallersPlugin.logError("RefreshAction::run", e);
            }
        }
    }

    private class JumpToDeclarationAction extends Action {
        public JumpToDeclarationAction() {
            super("&Open Declaration@F3");
            setToolTipText("Jump To Declaration Of Method (F3)");
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            try {
                getView().jumpToDeclarationOfSelection();
            } catch (RuntimeException e) {
                CallersPlugin.logError("JumpToDeclarationAction::run", e);
            }
        }
    }

    private class FocusOnSelectionAction extends Action {
        public FocusOnSelectionAction() {
            super("&Focus on selection");
            setDescription("Focus on selection");
            setToolTipText("Focus on selection");
        }

        private ISelection getSelection() {
            ISelectionProvider provider = getView().getSite().getSelectionProvider();

            if (provider != null) {
                return provider.getSelection();
            }

            return null;
        }

        /*
         * @see Action#run
         */
        public void run() {
            Object element = CallersUtility.getSingleElement(getSelection());

            if (element instanceof MethodWrapper) {
                getView().setMethod(((MethodWrapper) element).getMethod());
            }
        }

        public boolean canActionBeAdded() {
            Object element = CallersUtility.getSingleElement(getSelection());

            if (element instanceof IMethod) {
                setText("Focus on :" + ((IMethod) element).getElementName());

                return true;
            }

            return false;
        }
    }

    /**
     * @return CallersView
     */
    protected CallersView getView() {
        return mView;
    }

    /**
     * @param b
     */
    public void setHistoryEnabled(boolean enabled) {
        mHistoryDropDownAction.setEnabled(enabled);
    }
}
