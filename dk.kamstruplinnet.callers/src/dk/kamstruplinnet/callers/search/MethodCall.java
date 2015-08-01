/*
 * Created on 07-02-2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code Template
 */
package dk.kamstruplinnet.callers.search;

import org.eclipse.jdt.core.IMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author jl
 */
public class MethodCall {
    private List mCallLocations;
    private IMethod mMethod;

    /**
     * @param enclosingElement
     */
    public MethodCall(IMethod enclosingElement) {
        this.mMethod = enclosingElement;
    }

    /**
     * @param location
     */
    public void addCallLocation(CallLocation location) {
        if (mCallLocations == null) {
            mCallLocations = new ArrayList();
        }

        mCallLocations.add(location);
    }

    /**
     * @return Object
     */
    public Object getKey() {
        return getMethod().getHandleIdentifier();
    }

    /**
     *
     */
    public IMethod getMethod() {
        return mMethod;
    }

    /**
     * 
     */
    public Collection getCallLocations() {
        return mCallLocations;
    }
    
    public CallLocation getFirstCallLocation() {
        if (mCallLocations != null && !mCallLocations.isEmpty()) {
            return (CallLocation)mCallLocations.get(0);
        } else {
            return null;
        }
    }
}
