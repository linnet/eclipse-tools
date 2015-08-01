package dk.kamstruplinnet.implementors.ejb;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * @author jl
 */
class EJBProjectProperties {
    private static final String ENABLED_PROPERTY_FALSE = "false"; //$NON-NLS-1$
    private static final String ENABLED_PROPERTY_TRUE = "true"; //$NON-NLS-1$
    
    private static final String PATH_DELIMITER = ";"; //$NON-NLS-1$
    private static final List EMPTY_LIST = new ArrayList(0);
    private IResource mResource;

    EJBProjectProperties(IResource projectResource) {
        this.mResource = projectResource;
    }

    Collection getPaths() {
        return getPaths(false);
    }
    
    private Collection getPaths(boolean ignoreNonExistent) {
        try {
            String property = getResource().getPersistentProperty(getDescriptorFilesPropertyName());

            Collection paths = parsePathProperty(property, ignoreNonExistent);

            return paths;
        } catch (CoreException e) {
            EJBImplementorsPlugin.log(e);
        }

        return EMPTY_LIST;
    }

    Collection getExistingResources() {
        return getPaths(true);
    }

    boolean isEJBEnabled() {
        try {
            String value = getResource().getPersistentProperty(getEnabledPropertyName());
            return ENABLED_PROPERTY_TRUE.equals(value);
        } catch (CoreException e) {
            EJBImplementorsPlugin.log(e);
        }
        return false;
    }
    
    void setEJBEnabled(boolean enabled) {
        try {
            String value = enabled ? ENABLED_PROPERTY_TRUE : ENABLED_PROPERTY_FALSE;
            getResource().setPersistentProperty(getEnabledPropertyName(), value);
        } catch (CoreException e) {
            EJBImplementorsPlugin.log(e);
        }
    }
    
    private Collection parsePathProperty(String property, boolean ignoreNonExistent) {
        Collection paths = new ArrayList();

        if (property != null) {
            StringTokenizer st = new StringTokenizer(property, PATH_DELIMITER);
            IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

            while (st.hasMoreElements()) {
                String element = st.nextToken();
                IPath path = new Path(element);
                IResource resource = workspaceRoot.getFile(path);

                if (resource != null) {
                    if (!ignoreNonExistent || (ignoreNonExistent && resource.exists())) {
                        paths.add(resource);
                    }
                } else {
                    if (!ignoreNonExistent) {
                        paths.add(element);
                    }
                }
            }
        }

        return paths;
    }

    void saveFilePaths(Collection resources) {
        try {
            String propertyString = computeProperty(resources);

            if (propertyString.length() == 0) {
                propertyString = null;
            }

            getResource().setPersistentProperty(getDescriptorFilesPropertyName(), propertyString);
        } catch (CoreException e) {
            EJBImplementorsPlugin.log(e);
        }
    }

    /**
     *
     */
    private IResource getResource() {
        return mResource;
    }

    /**
     * @param mPaths
     * @return
     */
    private String computeProperty(Collection resources) {
        StringBuffer result = new StringBuffer();

        if (resources != null) {
            for (Iterator iter = resources.iterator(); iter.hasNext();) {
                if (result.length() > 0) {
                    result.append(PATH_DELIMITER);
                }

                Object element = iter.next();

                if (element instanceof IResource) {
                    IResource resource = (IResource) element;

                    result.append(resource.getFullPath().toString());
                } else if (element instanceof String) {
                    result.append(element);
                }
            }
        }

        return result.toString();
    }

    private QualifiedName getDescriptorFilesPropertyName() {
        return new QualifiedName("dk.kamstruplinnet.implementors.ejb", "descriptor-files"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private QualifiedName getEnabledPropertyName() {
        return new QualifiedName("dk.kamstruplinnet.implementors.ejb", "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
}
