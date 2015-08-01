package dk.kamstruplinnet.implementors.gwt;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;

import dk.kamstruplinnet.implementors.core.IImplementorFinder;


/**
 * @author jl
 */
public class GWTImplementorFinder implements IImplementorFinder {
    
    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.IImplementorFinder#findImplementingTypes(org.eclipse.jdt.core.IType)
     */
    public Collection findImplementingTypes(IType type, IProgressMonitor progressMonitor) {
        return GWTImplementorsPlugin.getInstance().findImplementingTypes(type, progressMonitor);
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.core.IImplementorFinder#findInterfaces(org.eclipse.jdt.core.IType, org.eclipse.core.runtime.IProgressMonitor)
     */
    public Collection findInterfacesAndAbstractTypes(IType type, IProgressMonitor progressMonitor) {
        return GWTImplementorsPlugin.getInstance().findInterfaces(type, progressMonitor);
    }
}
