package dk.kamstruplinnet.callers.actions;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;

import org.eclipse.jdt.core.IMethod;

import dk.kamstruplinnet.callers.CallersPluginImages;
import dk.kamstruplinnet.callers.views.CallersView;

public class HistoryDropDownAction extends Action implements IMenuCreator {
    public static final int RESULTS_IN_DROP_DOWN= 10;

    private CallersView mView;
    private Menu mMenu;
    
    public HistoryDropDownAction(CallersView view) {
        mView = view;
        mMenu= null;
        setToolTipText("Previous call hierarchies"); //$NON-NLS-1$
        setImageDescriptor(CallersPluginImages.create("history_list.gif"));
        setMenuCreator(this);
    }

    public void dispose() {
        mView = null;
        if (mMenu != null) {
            mMenu.dispose();
            mMenu= null;
        }
    }

    public Menu getMenu(Menu parent) {
        return null;
    }

    public Menu getMenu(Control parent) {
        if (mMenu != null) {
            mMenu.dispose();
        }
        mMenu= new Menu(parent);
        IMethod[] elements= mView.getHistoryEntries();
        addEntries(mMenu, elements);
        return mMenu;
    }
    
    private boolean addEntries(Menu menu, IMethod[] elements) {
        boolean checked= false;
        
        int min= Math.min(elements.length, RESULTS_IN_DROP_DOWN);
        for (int i= 0; i < min; i++) {
            HistoryAction action= new HistoryAction(mView, elements[i]);
            action.setChecked(elements[i].equals(mView.getMethod()));
            checked= checked || action.isChecked();
            addActionToMenu(menu, action);
        }   
        return checked;
    }
    

    protected void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item= new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    public void run() {
    }
}
