package dk.kamstruplinnet.implementors.core;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;


/**
 * @author jl
 */
public class JavaImplementorFinder implements IImplementorFinder {
    public Collection findImplementingTypes(IType type, IProgressMonitor progressMonitor) {
        ITypeHierarchy typeHierarchy;
        try {
            typeHierarchy = type.newTypeHierarchy(progressMonitor);
            IType[] potentialImplementors = typeHierarchy.getAllClasses();
            HashSet result = new HashSet(potentialImplementors.length);
            for (int i = 0; i < potentialImplementors.length; i++) {
                result.add(new TypeResult(potentialImplementors[i]));
            }
            
            return result;
        } catch (JavaModelException e) {
            Implementors.log(e);
        }
        return null; 
    }

    public Collection findInterfacesAndAbstractTypes(IType type, IProgressMonitor progressMonitor) {
        ITypeHierarchy typeHierarchy;
        try {
            typeHierarchy = type.newTypeHierarchy(progressMonitor);
            IType[] potentialInterfaces = typeHierarchy.getAllSupertypes(type);
            
            HashSet result = new HashSet(potentialInterfaces.length);
            for (int i = 0; i < potentialInterfaces.length; i++) {
                IType implementorType = potentialInterfaces[i];
                result.add(new TypeResult(implementorType));
            }
            
            return result;
        } catch (JavaModelException e) {
            Implementors.log(e);
        }
        return null; 
    }


}
