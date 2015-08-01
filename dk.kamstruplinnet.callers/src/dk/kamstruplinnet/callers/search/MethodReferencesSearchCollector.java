package dk.kamstruplinnet.callers.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;

import java.util.Map;


/**
 * @author jl
 */
class MethodReferencesSearchCollector implements IJavaSearchResultCollector {
    private CallSearchResultCollector searchResults;
    private boolean mRequireExactMatch = true;
    private IProgressMonitor mProgressMonitor;

    MethodReferencesSearchCollector() {
        searchResults = new CallSearchResultCollector();
    }

    /**
     * @see org.eclipse.jdt.core.search.IJavaSearchResultCollector#aboutToStart()
     */
    public void aboutToStart() {
    }

    /**
     * @see org.eclipse.jdt.core.search.IJavaSearchResultCollector#accept(org.eclipse.core.resources.IResource, int, int, org.eclipse.jdt.core.IJavaElement, int)
     */
    public void accept(IResource resource, int start, int end,
        IJavaElement enclosingElement, int accuracy) throws CoreException {
        if (mRequireExactMatch && (accuracy != IJavaSearchResultCollector.EXACT_MATCH)) {
            return;
        }

        if ((enclosingElement != null) && enclosingElement instanceof IMethod) {
            IMethod method = (IMethod) enclosingElement;
            searchResults.addMethod(method, method, start, end);
        }
    }

    /**
     * @see org.eclipse.jdt.core.search.IJavaSearchResultCollector#done()
     */
    public void done() {
    }

    public Map getCallers() {
        return searchResults.getCallers();
    }

    /**
     * @see org.eclipse.jdt.core.search.IJavaSearchResultCollector#getProgressMonitor()
     */
    public IProgressMonitor getProgressMonitor() {
        return mProgressMonitor;
    }

    /**
     * @param monitor
     */
    void setProgressMonitor(SubProgressMonitor monitor) {
        this.mProgressMonitor = monitor;
    }
}
