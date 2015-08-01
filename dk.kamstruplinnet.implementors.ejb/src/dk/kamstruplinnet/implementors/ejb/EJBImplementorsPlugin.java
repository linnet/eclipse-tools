package dk.kamstruplinnet.implementors.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * The main plugin class to be used in the desktop.
 */
public class EJBImplementorsPlugin extends AbstractUIPlugin {
    private static final String TAG_DEPLOYMENT_DESCRIPTOR_MASK= "DEPLOYMENT_DESCRIPTOR_MASK"; //$NON-NLS-1$
    private static final String DEFAULT_DEPLOYMENT_DESCRIPTOR_MASK= "ejb-jar.xml"; //$NON-NLS-1$
    private Map mProjectMap;

    //The shared instance.
    private static EJBImplementorsPlugin plugin;

    /**
     * The constructor.
     */
    public EJBImplementorsPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
        plugin = this;
    }

    /**
     * Returns the shared instance.
     */
    public static EJBImplementorsPlugin getInstance() {
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
                "Internal Error in Implementors EJB plugin", t)); //$NON-NLS-1$
    }

    private static String getPluginId() {
        return getInstance().getDescriptor().getUniqueIdentifier();
    }

    /**
     *
     */
    private Map getProjectMap() {
        if (mProjectMap == null) {
            mProjectMap = new HashMap();
        }

        return mProjectMap;
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.actions.IImplementorFinder#findImplementingTypes(org.eclipse.jdt.core.IType)
     */
    public Collection findImplementingTypes(IType type, IProgressMonitor progressMonitor) {
        Collection result = new HashSet();

        IJavaProject[] projects = getEJBProjects();

        for (int i = 0; i < projects.length; i++) {
            IJavaProject project = projects[i];

            EJBProjectMappings mappings = getProjectMappings(project);
            Collection types = mappings.findImplementingTypes(type, progressMonitor);

            if ((types != null) && (types.size() > 0)) {
                result.addAll(types);
            }
        }

        return result;
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.actions.IImplementorFinder#findImplementingTypes(org.eclipse.jdt.core.IType)
     */
    public Collection findInterfaces(IType type, IProgressMonitor progressMonitor) {
        Collection result = new HashSet();

        IJavaProject[] projects = getEJBProjects();

        for (int i = 0; i < projects.length; i++) {
            IJavaProject project = projects[i];

            EJBProjectMappings mappings = getProjectMappings(project);
            Collection types = mappings.findInterfaces(type, progressMonitor);

            if ((types != null) && (types.size() > 0)) {
                result.addAll(types);
            }
        }

        return result;
    }

    /**
     * This method returns a list of Java projects which are EJB enabled.
     * So far, all Java projects are EJB enabled (no project nature has been developed).
     * @return
     */
    private IJavaProject[] getEJBProjects() {
        try {
            IJavaProject[] allProjects = JavaCore.create(ResourcesPlugin.getWorkspace()
                                                                        .getRoot())
                                                 .getJavaProjects();

            Collection ejbAwareProjects = new ArrayList();

            for (int i = 0; i < allProjects.length; i++) {
                IJavaProject project = allProjects[i];
                EJBProjectProperties properties = new EJBProjectProperties(project.getProject());

                if (properties.isEJBEnabled()) {
                    ejbAwareProjects.add(project);
                }
            }

            return (IJavaProject[]) ejbAwareProjects.toArray(new IJavaProject[ejbAwareProjects.size()]);
        } catch (JavaModelException e) {
            log(e);
        }

        return new IJavaProject[0];
    }

    EJBProjectMappings getProjectMappings(IJavaProject project) {
        EJBProjectMappings projectMappings = (EJBProjectMappings) getProjectMap().get(project.getHandleIdentifier());

        if (projectMappings == null) {
            projectMappings = new EJBProjectMappings(project);
            getProjectMap().put(project.getHandleIdentifier(), projectMappings);
        }

        return projectMappings;
    }
    
    String getDeploymentDescriptorMask() {
        IPreferenceStore settings= getPreferenceStore();
        String mask= settings.getString(TAG_DEPLOYMENT_DESCRIPTOR_MASK);
        if (mask != null && !IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(mask)) {
            return mask;
        }
        return DEFAULT_DEPLOYMENT_DESCRIPTOR_MASK;
    }
    
    void setDeploymentDescriptorMask(String mask) {
        IPreferenceStore settings= getPreferenceStore();
        settings.setValue(TAG_DEPLOYMENT_DESCRIPTOR_MASK, mask);
    }
}
