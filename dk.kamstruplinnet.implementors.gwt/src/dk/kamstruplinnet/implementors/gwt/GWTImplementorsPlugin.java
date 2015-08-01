package dk.kamstruplinnet.implementors.gwt;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * The main plugin class to be used in the desktop.
 */
public class GWTImplementorsPlugin extends AbstractUIPlugin {

	private static final String SERVICE_CLASSNAME = "com.google.gwt.user.server.rpc.RemoteServiceServlet"; //$NON-NLS-1$
	private static final String SERVICE_INTERFACENAME = "com.google.gwt.user.client.rpc.RemoteService"; //$NON-NLS-1$
	private static final String ASYNC_SUFFIX = "Async"; //$NON-NLS-1$

	private static final String GWT_NATURE = "com.google.gwt.eclipse.core.gwtNature"; //$NON-NLS-1$

    //The shared instance.
    private static GWTImplementorsPlugin plugin;

    /**
     * The constructor.
     */
    public GWTImplementorsPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
        plugin = this;
    }

    /**
     * Returns the shared instance.
     */
    public static GWTImplementorsPlugin getInstance() {
        return plugin;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    static void log(Throwable t) {
        getInstance().getLog().log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR,
                "Internal Error in Implementors GWT plugin", t)); //$NON-NLS-1$
    }

    private static String getPluginId() {
        return getInstance().getDescriptor().getUniqueIdentifier();
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.actions.IImplementorFinder#findImplementingTypes(org.eclipse.jdt.core.IType)
     */
    public Collection findImplementingTypes(IType type, IProgressMonitor progressMonitor) {
        Collection result = new HashSet();

        try {
        	IType realGWTInterface = getRealGWTInterface(type, progressMonitor);
        	if (realGWTInterface != null && isGWTProject(realGWTInterface.getJavaProject())) {
        		result = getGWTImplementations(realGWTInterface, progressMonitor);
        	}
		} catch (JavaModelException e) {
			log(e);
		}
		catch (CoreException e) {
			log(e);
		}

        return result;
    }

    /**
     * Returns a collection of TypeResult instances representing the implementations of the GWT interface. 
     * @param gwtInterfaceType
     * @param progressMonitor
     * @return
     * @throws JavaModelException
     */
    private Collection getGWTImplementations(IType gwtInterfaceType, IProgressMonitor progressMonitor) throws JavaModelException {
        ITypeHierarchy typeHierarchy;

        typeHierarchy = gwtInterfaceType.newTypeHierarchy(progressMonitor);
        IType[] potentialImplementors = typeHierarchy.getAllClasses();
        HashSet result = new HashSet();
        for (int i = 0; i < potentialImplementors.length; i++) {
            result.add(new GWTAsyncToRealTypeResult(potentialImplementors[i]));
        }
        
        return result;
    }
    
    private IType getRealGWTInterface(IType type, IProgressMonitor progressMonitor) throws JavaModelException {
		String interfaceName = type.getFullyQualifiedName();
		
		if (!(type.isInterface() && interfaceName.endsWith(ASYNC_SUFFIX))) {
			return null;
		}
		
		String realInterfaceName = interfaceName.substring(0, interfaceName.length() - 5);
		
		IJavaElement realInterface = JavaModelUtil.findTypeContainer(type.getJavaProject(), realInterfaceName);
		if (realInterface instanceof IType) {
			IType realInterfaceType = (IType)realInterface;
			if (realInterfaceType.isInterface()) {
				ITypeHierarchy interfaceHierarchy = realInterfaceType.newSupertypeHierarchy(progressMonitor);
				if (isGWTServiceInterface(realInterfaceType, interfaceHierarchy)) {
					return realInterfaceType;
				}
			}
		}

		return null;
    }
    
    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.actions.IImplementorFinder#findImplementingTypes(org.eclipse.jdt.core.IType)
     */
    public Collection findInterfaces(IType type, IProgressMonitor progressMonitor) {
        Collection result = new HashSet();

        try {
        	if (type.isClass()) {
				ITypeHierarchy hierarchy = type.newSupertypeHierarchy(progressMonitor);
	
				if (isGWTServiceImplementation(type, hierarchy)) {
					IType serviceInterfaceType = getGWTServiceInterface(hierarchy);
					IJavaElement asyncInterface = JavaModelUtil.findTypeContainer(serviceInterfaceType.getJavaProject(), serviceInterfaceType.getFullyQualifiedName() + ASYNC_SUFFIX);
					if (asyncInterface instanceof IType) {
						result.add(new GWTRealToAsyncTypeResult((IType) asyncInterface));
			        }
				}
        	}
		} catch (JavaModelException e) {
			log(e);
		}

        return result;
    }

    private IType getGWTServiceInterface(ITypeHierarchy hierarchy) {
    	IType[] allInterfaces = hierarchy.getAllInterfaces();
    	
    	for (int i = 0; i < allInterfaces.length; i++) {
    		IType interfaceType = allInterfaces[i];
			if (isGWTServiceInterface(interfaceType, hierarchy)) {
				return interfaceType;
			}
		}
		return null;
	}

	private boolean isGWTServiceImplementation(IType implementationType, ITypeHierarchy hierarchy) {
		IType[] allSuperclasses = hierarchy.getAllSuperclasses(implementationType);
		for (int i = 0; i < allSuperclasses.length; i++) {
			IType superClass = allSuperclasses[i];
			if (SERVICE_CLASSNAME.equals(superClass.getFullyQualifiedName())) {
				return true;
			}
		}
		return false;
    }
	
	private boolean isGWTServiceInterface(IType interfaceType, ITypeHierarchy hierarchy) {
		IType[] allSuperinterfaces = hierarchy.getAllSuperInterfaces(interfaceType);
		for (int i = 0; i < allSuperinterfaces.length; i++) {
			IType superInterface = allSuperinterfaces[i];
			if (SERVICE_INTERFACENAME.equals(superInterface.getFullyQualifiedName())) {
				return true;
			}
		}
		return false;
    }

    private boolean isGWTProject(IJavaProject project) throws CoreException {
		return project.getProject().hasNature(GWT_NATURE);
	}
}
