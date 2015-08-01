package dk.kamstruplinnet.callers.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import dk.kamstruplinnet.callers.CallersPlugin;
import dk.kamstruplinnet.callers.search.MethodWrapper;


/**
 * @author jl
 */
public class CallersContentProvider implements ITreeContentProvider {
    private final static Object[] EMPTY_ARRAY = new Object[0];

    public CallersContentProvider() {
        super();
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof TreeRoot) {
            TreeRoot dummyRoot = (TreeRoot) parentElement;

            return new Object[] { dummyRoot.getRoot() };
        } else if (parentElement instanceof MethodWrapper) {
            MethodWrapper methodWrapper = ((MethodWrapper) parentElement);
            if (methodWrapper.getLevel() <= CallersPlugin.getDefault().getMaxCallDepth()) {
                return methodWrapper.getCalls();
            } else {
                return new Object[] { TreeTermination.INSTANCE };
            }
        }

        return EMPTY_ARRAY;
    }

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof MethodWrapper) {
            return ((MethodWrapper) element).getParent();
        }

        return null;
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element == TreeRoot.EMPTY_ROOT || element == TreeTermination.INSTANCE) {
            return false;
        }

        return true;
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
