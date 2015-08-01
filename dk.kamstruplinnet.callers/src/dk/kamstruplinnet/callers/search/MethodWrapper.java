package dk.kamstruplinnet.callers.search;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.widgets.Display;

import dk.kamstruplinnet.callers.CallersPlugin;


/**
 * This class represents the general parts of a method call (either to or from a
 * method).
 *
 * @author jl
 */
public abstract class MethodWrapper {
    private Map mElements = null;

    /*
     * A cache of previously found methods. This cache should be searched
     * before adding a "new" method object reference to the list of elements.
     * This way previously found methods won't be searched again.
     */
    private Map mMethodCache;
    private MethodCall mMethodCall;
    
    private MethodWrapper mParent;
    private int mLevel;

    /**
     * Constructor CallerElement.
     */
    public MethodWrapper(MethodWrapper parent, MethodCall methodCall) {
        super();

        if (methodCall == null) {
            throw new IllegalArgumentException("Parameter method cannot be null");
        }

        if (parent == null) {
            setMethodCache(new HashMap());
            mLevel = 1;
        } else {
            setMethodCache(parent.getMethodCache());
            mLevel = parent.getLevel() + 1;
        }

        this.mMethodCall = methodCall;
        this.mParent = parent;
    }

    /**
     * @return int
     */
    public int getLevel() {
        return mLevel;
    }

    protected void addCallToCache(MethodCall methodCall) {
        Map cachedCalls = lookupMethod(this.getMethodCall());
        cachedCalls.put(methodCall.getKey(), methodCall);
    }

    /**
     * Method createMethodWrapper.
     * @param method
     * @return MethodWrapper
     */
    protected abstract MethodWrapper createMethodWrapper(MethodCall methodCall);

    protected void doFindChildren() {
        Map existingResults = lookupMethod(getMethodCall());

        if (existingResults != null) {
            mElements = new HashMap();
            mElements.putAll(existingResults);
        } else {
            initCalls();

            try {
                ModalContext.run(getRunnableWithProgress(), true,
                    getProgressMonitor(), Display.getCurrent());
            } catch (InterruptedException ie) {
                initCalls();
            } catch (Exception e) {
                CallersPlugin.logError("Error searching in modal context", e);
            }
        }
    }

    private IProgressMonitor getProgressMonitor() {
        return CallersPlugin.getDefault().getProgressMonitor();
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth.getClass() != getClass()) {
            return false;
        }

        MethodWrapper other = (MethodWrapper) oth;

        if (this.mParent == null) {
            if (other.mParent != null) {
                return false;
            }
        } else {
            if (!this.mParent.equals(other.mParent)) {
                return false;
            }
        }

        if (this.getMethodCall() == null) {
            if (other.getMethodCall() != null) {
                return false;
            }
        } else {
            if (!this.getMethodCall().equals(other.getMethodCall())) {
                return false;
            }
        }

        return true;
    }

    
    /**
     * This method finds the children of the current IMethod (either callers or
     * callees, depending on the concrete subclass.
     * @return The result of the search for children
     */
    protected abstract Map findChildren(IProgressMonitor progressMonitor);

    /**
     * Method getCallerElements.
     * @return The child caller elements of this element
     */
    public MethodWrapper[] getCalls() {
        try {
            if (mElements == null) {
                doFindChildren();
            }

            MethodWrapper[] result = new MethodWrapper[mElements.size()];
            int i = 0;

            for (Iterator iter = mElements.keySet().iterator(); iter.hasNext();) {
                MethodCall methodCall = getMethodCallFromMap(mElements, iter.next());
                result[i++] = createMethodWrapper(methodCall);
            }

            return result;
        } catch (Exception e) {
            CallersPlugin.logError("Error when finding calls", e);

            return new MethodWrapper[0];
        }
    }

    /**
     * Method getMethod.
     * @return Object
     */
    public IMethod getMethod() {
        return getMethodCall().getMethod();
    }

    protected Map getMethodCache() {
        return mMethodCache;
    }

    /**
     * @return MethodCall
     */
    public MethodCall getMethodCall() {
        return mMethodCall;
    }

    private MethodCall getMethodCallFromMap(Map elements, Object key) {
        return (MethodCall) elements.get(key);
    }

    /**
     * Method getName.
     */
    public String getName() {
        if (getMethodCall() != null) {
            return getMethodCall().getMethod().getElementName();
        } else {
            return "";
        }
    }

    /**
     * Method getParent.
     * @return
     */
    public MethodWrapper getParent() {
        return mParent;
    }

    protected IRunnableWithProgress getRunnableWithProgress() {
        return new IRunnableWithProgress() {
                /**
                 * @see dk.kamstruplinnet.callers.CallerMethodWrapper#run(org.eclipse.core.runtime.IProgressMonitor)
                 */
                public void run(IProgressMonitor progressMonitor)
                    throws InvocationTargetException, InterruptedException {
                    if (progressMonitor != null) {
                        progressMonitor.beginTask(getTaskName(), IProgressMonitor.UNKNOWN);
                    }
                    try {
                        performSearch(progressMonitor);
                    } finally {
                        if (progressMonitor != null) {
                            progressMonitor.done();
                        }
                    }
                }
            };
    }

    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (mParent != null) {
            result = (PRIME * result) + mParent.hashCode();
        }

        if (getMethodCall() != null) {
            result = (PRIME * result) + getMethodCall().getMethod().hashCode();
        }

        return result;
    }

    private void initCacheForMethod() {
        Map cachedCalls = new HashMap();
        getMethodCache().put(this.getMethodCall().getKey(), cachedCalls);
    }

    protected void initCalls() {
        this.mElements = new HashMap();

        initCacheForMethod();
    }

    /**
     * Looks up a previously created search result in the "global" cache.
     * @param method
     * @return List List of previously found search results
     */
    protected Map lookupMethod(MethodCall methodCall) {
        return (Map) getMethodCache().get(methodCall.getKey());
    }

    protected void performSearch(IProgressMonitor progressMonitor) {
        mElements = findChildren(progressMonitor);

        for (Iterator iter = mElements.keySet().iterator(); iter.hasNext();) {
            MethodCall methodCall = getMethodCallFromMap(mElements, iter.next());
            addCallToCache(methodCall);
        }
    }

    protected void setMethodCache(Map methodCache) {
        mMethodCache = methodCache;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String result;

        result = "CallerElement[name=" + getName() + ", children=";

        if (mElements == null) {
            result += "unknown]";
        } else {
            result += (mElements.size() + "]");
        }

        return result;
    }

    protected abstract String getTaskName();
}
