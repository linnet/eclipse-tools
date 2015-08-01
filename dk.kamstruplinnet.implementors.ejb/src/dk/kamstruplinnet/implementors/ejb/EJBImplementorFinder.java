package dk.kamstruplinnet.implementors.ejb;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;

import dk.kamstruplinnet.implementors.core.IImplementorFinder;


/**
 * @author jl
 */
public class EJBImplementorFinder implements IImplementorFinder {
    
    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.IImplementorFinder#findImplementingTypes(org.eclipse.jdt.core.IType)
     */
    public Collection findImplementingTypes(IType type, IProgressMonitor progressMonitor) {
        return EJBImplementorsPlugin.getInstance().findImplementingTypes(type, progressMonitor);
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.core.IImplementorFinder#findInterfaces(org.eclipse.jdt.core.IType, org.eclipse.core.runtime.IProgressMonitor)
     */
    public Collection findInterfacesAndAbstractTypes(IType type, IProgressMonitor progressMonitor) {
        return EJBImplementorsPlugin.getInstance().findInterfaces(type, progressMonitor);
    }
}
