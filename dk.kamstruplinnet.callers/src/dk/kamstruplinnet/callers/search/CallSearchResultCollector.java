/*
 * Created on 10-02-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package dk.kamstruplinnet.callers.search;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;

import dk.kamstruplinnet.callers.CallersPlugin;

/**
 * @author jl
 */
public class CallSearchResultCollector {
    private Map mCalledMethods;
    private String[] mPackageNames = null;

    public CallSearchResultCollector() {
        this.mCalledMethods = createCalledMethodsData();

        initializePackageFilters();
    }

    protected Map createCalledMethodsData() {
        return new HashMap();
    }

    
    protected void addMethod(IMethod method, IMethod calledMethod, int start, int end) {
        if (method != null && calledMethod != null) {
            if (!isIgnored(calledMethod)) {
                MethodCall methodCall = (MethodCall) mCalledMethods.get(calledMethod.getHandleIdentifier());
        
                if (methodCall == null) {
                    methodCall = new MethodCall(calledMethod);
                    mCalledMethods.put(calledMethod.getHandleIdentifier(), methodCall);
                }
        
                methodCall.addCallLocation(new CallLocation(method, calledMethod, start, end));
            }
        }
    }

    public Map getCallers() {
        return mCalledMethods;
    }

    /**
     * Method getPackageNames.
     * @param strings
     * @return String[]
     */
    private String[] getPackageNames(String[] filters) {
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                if (filters[i].endsWith(".*")) {
                    filters[i] = filters[i].substring(0, filters[i].length() - 2);
                }
            }
        }

        return filters;
    }

    private void initializePackageFilters() {
        String[] filters = CallersPlugin.getDefault().getIgnoreFilters();
        mPackageNames = getPackageNames(filters);
    }

    /**
     * Method isIgnored.
     * @param enclosingElement
     * @return boolean
     */
    private boolean isIgnored(IMethod enclosingElement) {
        try {
            if ((mPackageNames != null) && (mPackageNames.length > 0)) {
                String fullyQualifiedName = ((IMethod) enclosingElement).getDeclaringType()
                                             .getFullyQualifiedName();
        
                for (int i = 0; i < mPackageNames.length; i++) {
                    if (matchPackage(fullyQualifiedName, mPackageNames[i])) {
                        return true;
                    }
                }
            }
        
            return false;
        } catch (Exception e) {
            CallersPlugin.logError("Error when ignoring packages", e);
        
            return false;
        }
    }

    private boolean matchPackage(String fullyQualifiedName, String filter) {
        return fullyQualifiedName.startsWith(filter);
    }

}
