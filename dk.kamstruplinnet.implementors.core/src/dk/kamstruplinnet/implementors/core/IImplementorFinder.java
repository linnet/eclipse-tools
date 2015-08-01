package dk.kamstruplinnet.implementors.core;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;

/**
 * @author jl
 */
public interface IImplementorFinder {
    /**
     * Find implementors of the specified IType instance.
     *  
     * @param type
     * @return
     */
    public abstract Collection findImplementingTypes(IType type, IProgressMonitor progressMonitor);

    /**
     * Find interfaces which are implemented by the specified IType instance.
     * 
     * @param type
     * @return
     */
    public abstract Collection findInterfacesAndAbstractTypes(IType type, IProgressMonitor progressMonitor);
}