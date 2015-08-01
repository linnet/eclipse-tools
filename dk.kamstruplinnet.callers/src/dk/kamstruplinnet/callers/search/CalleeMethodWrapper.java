package dk.kamstruplinnet.callers.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;


/**
 * @author jl
 */
public class CalleeMethodWrapper extends MethodWrapper {
    private Comparator mMethodWrapperComparator = new MethodWrapperComparator();

    private class MethodWrapperComparator implements Comparator {
        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            MethodWrapper m1 = (MethodWrapper) o1;
            MethodWrapper m2 = (MethodWrapper) o2;

            CallLocation callLocation1 = m1.getMethodCall().getFirstCallLocation();
            CallLocation callLocation2 = m2.getMethodCall().getFirstCallLocation();
            
            if (callLocation1 != null && callLocation2 != null) {
                if (callLocation1.getStart() == callLocation2.getStart()) {
                    return callLocation1.getEnd() - callLocation2.getEnd();
                }
                return callLocation1.getStart() - callLocation2.getStart();
            }

            return 0;
        }

    }
    
    /**
     * Constructor for CalleeMethodWrapper.
     * @param parent
     * @param method
     */
    public CalleeMethodWrapper(MethodWrapper parent, MethodCall methodCall) {
        super(parent, methodCall);
    }

    /* Returns the calls sorted after the call location
     * @see dk.kamstruplinnet.callers.search.MethodWrapper#getCalls()
     */
    public MethodWrapper[] getCalls() {
        MethodWrapper[] result = super.getCalls();
        Arrays.sort(result, mMethodWrapperComparator);
        return result;
    }

    /**
     * Find callees called from the current method.
     * @see dk.kamstruplinnet.callers.MethodWrapper#findChildren()
     */
    protected Map findChildren(IProgressMonitor progressMonitor) {
        return findChildrenUsingAST(progressMonitor);
//        return findChildrenUsingSearch();
    }

    /**
     * @see dk.kamstruplinnet.callers.CallerMethodWrapper#createMethodWrapper(org.eclipse.jdt.core.IMethod)
     */
    protected MethodWrapper createMethodWrapper(MethodCall methodCall) {
        return new CalleeMethodWrapper(this, methodCall);
    }

    protected Map findChildrenUsingAST(IProgressMonitor progressMonitor) {
        CalleeAnalyzerVisitor visitor = new CalleeAnalyzerVisitor(getMethod(), progressMonitor);
        initCalls();

        ICompilationUnit icu = getMethod().getCompilationUnit();

        if (icu != null) {
            CompilationUnit cu = AST.parseCompilationUnit(icu, true);
            cu.accept(visitor);

        }
        return visitor.getCallees();
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.callers.search.MethodWrapper#getTaskName()
     */
    protected String getTaskName() {
        return null;
    }
}
