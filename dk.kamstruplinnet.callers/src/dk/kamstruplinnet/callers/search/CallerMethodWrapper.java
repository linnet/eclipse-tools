package dk.kamstruplinnet.callers.search;

import dk.kamstruplinnet.callers.CallersPlugin;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author jl
 */
public class CallerMethodWrapper extends MethodWrapper {
    public CallerMethodWrapper(MethodWrapper parent, MethodCall methodCall) {
        super(parent, methodCall);
    }

    /**
     * @see dk.kamstruplinnet.callers.CallerMethodWrapper#createMethodWrapper(org.eclipse.jdt.core.IMethod)
     */
    protected MethodWrapper createMethodWrapper(MethodCall methodCall) {
        return new CallerMethodWrapper(this, methodCall);
    }

    /**
     * @see dk.kamstruplinnet.callers.MethodWrapper#findChildren()
     * @return The result of the search for children
     */
    protected Map findChildren(IProgressMonitor progressMonitor) {
        try {
            MethodReferencesSearchCollector searchCollector = new MethodReferencesSearchCollector();
            SearchEngine searchEngine = new SearchEngine();

            for (Iterator iter = getMethods().iterator(); iter.hasNext() && !progressMonitor.isCanceled();) {
                IMethod method = (IMethod) iter.next();
                searchCollector.setProgressMonitor(new SubProgressMonitor(progressMonitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
                searchEngine.search(ResourcesPlugin.getWorkspace(), method,
                    IJavaSearchConstants.REFERENCES, getSearchScope(), searchCollector);
            }

            return searchCollector.getCallers();
        } catch (JavaModelException e) {
            CallersPlugin.logError("Error finding callers", e);

            return new HashMap(0);
        }
    }

    /**
     *
     */
    private Collection getMethods() {
        Collection result = new ArrayList();
        
        result.add(getMethod());
        result.addAll(CallersPlugin.getDefault().getInterfaceMethods(getMethod()));

        return result;
    }

    protected IJavaSearchScope getSearchScope() {
        return CallersPlugin.getDefault().getSearchScope();
    }
    
    protected String getTaskName() {
        return "Finding callers...";
    }
}
