package dk.kamstruplinnet.implementors.ejb;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

/**
 * This class represents the mappings for a specific project.
 * @author jl
 */
class EJBProjectMappings {
    private EJBProjectProperties mProperties;

    private IJavaProject mProject;

    private Map mInterfaceMappings;
    private Map mImplementorMappings;
    
    EJBProjectMappings(IJavaProject project) {
        this.mProject = project;
        mProperties = new EJBProjectProperties(project.getResource()); 
    }

    /**
     * @param type
     * @return
     */
    Collection findImplementingTypes(IType type, IProgressMonitor progressMonitor) {
        return (Collection) getImplementorMappings(progressMonitor).get(type.getHandleIdentifier());
    }

    /**
     * @param type
     * @return
     */
    Collection findInterfaces(IType type, IProgressMonitor progressMonitor) {
        return (Collection) getInterfaceMappings(progressMonitor).get(type.getHandleIdentifier());
    }
    
    void updateMappings() {
        mImplementorMappings = null;
        mInterfaceMappings = null;
    }
    
    /**
     * 
     */
    private Map getImplementorMappings(IProgressMonitor progressMonitor) {
        if (mImplementorMappings == null) {
            buildMappings(progressMonitor);
        }
        return mImplementorMappings;
    }

    /**
     * 
     */
    private Map getInterfaceMappings(IProgressMonitor progressMonitor) {
        if (mInterfaceMappings == null) {
            buildMappings(progressMonitor);
        }
        return mInterfaceMappings;
    }

    private void buildMappings(IProgressMonitor progressMonitor) {
        Collection deploymentDescriptors = mProperties.getExistingResources();

        mImplementorMappings = new HashMap();
        mInterfaceMappings = new HashMap();
        if (deploymentDescriptors != null && deploymentDescriptors.size() > 0) {        
            SubProgressMonitor subProgressMonitor = new SubProgressMonitor(progressMonitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
            EJBDescriptorParser parser = new EJBDescriptorParser(mProject, subProgressMonitor);
            for (Iterator iter = deploymentDescriptors.iterator(); iter.hasNext();) {
                IResource deploymentDescriptor = (IResource) iter.next();
                parser.parseEJBMappings(deploymentDescriptor.getLocation());
                mImplementorMappings.putAll(parser.getImplementors());
                mInterfaceMappings.putAll(parser.getInterfaces());
                subProgressMonitor.worked(1);
            }
        }
    }

}
