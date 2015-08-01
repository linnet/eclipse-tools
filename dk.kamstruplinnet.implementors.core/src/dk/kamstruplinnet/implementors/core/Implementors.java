package dk.kamstruplinnet.implementors.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;


/**
 * The main plugin class to be used in the desktop.
 */
public class Implementors extends Plugin {
    private final String IMPLEMENTOR_FINDER_EXTENSION_POINT_ID = "dk.kamstruplinnet.implementors.core.implementorproviders"; //$NON-NLS-1$
    private IImplementorFinder[] mImplementorFinders;

    //The shared instance.
    private static Implementors plugin;

    /**
     * The constructor.
     */
    public Implementors(IPluginDescriptor descriptor) {
        super(descriptor);
        plugin = this;
    }

    /**
     * Returns the shared instance.
     */
    public static Implementors getInstance() {
        return plugin;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public static void log(Throwable t) {
        getInstance().getLog().log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Internal Error in Implementors Core plugin", t)); //$NON-NLS-1$
    }
    
    private static String getPluginId() {
        return getInstance().getDescriptor().getUniqueIdentifier();
    }

    private IImplementorFinder[] getImplementorFinders() {
        if (mImplementorFinders == null) {
            mImplementorFinders = computeImplementorFinders();
        }

        return mImplementorFinders;
    }

    /**
     * @return
     */
    private IImplementorFinder[] computeImplementorFinders() {
        IPluginRegistry registry = Platform.getPluginRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(IMPLEMENTOR_FINDER_EXTENSION_POINT_ID);

        IConfigurationElement[] configurations = extensionPoint.getConfigurationElements();
        List results = new ArrayList();

        for (int i = 0; i < configurations.length; i++) {
            try {
                IConfigurationElement each = configurations[i];
                Object provider = each.createExecutableExtension("class"); //$NON-NLS-1$

                if (provider instanceof IImplementorFinder) {
                    results.add(provider);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }

        return (IImplementorFinder[]) results.toArray(new IImplementorFinder[results.size()]);
    }

    /**
     * Searches for implementors of the specified Java elements. Only the first element of the
     * elements parameter is taken into consideration.
     * 
     * @param elements
     * @return An array of found implementing Java elements.
     */
    public IJavaElement[] searchForImplementors(IJavaElement[] elements, IProgressMonitor progressMonitor) {
        try {
            if (elements != null && elements.length > 0) {
                IJavaElement element = elements[0];
                element = getFieldConverter().convertFieldOrLocalVariableToType(element);
                if (element instanceof IMethod) {
                    IMethod method = (IMethod) element;
                    TypeResult[] implementingTypes = findImplementingTypes(method.getDeclaringType(), progressMonitor);
                    return findMethods(method, implementingTypes, false, progressMonitor);
                } else if (element instanceof IType) {
                    IType type = (IType) element;
                    TypeResult[] implementingTypes = findImplementingTypes(type, progressMonitor);
                    return buildJavaElementArray(type, implementingTypes);
                }
            }
        } catch (Exception e) {
            log(e);
        }
        
        return null;
    }

    private FieldConverter getFieldConverter() {
        return new FieldConverter();
    }
    
    /**
     * Returns an array of IJavaElement's built from an array of TypeResult's.
     * If the type java.lang.Object is included in the input, it is discarded.
     * 
     * @param types
     * @return
     */
    private IJavaElement[] buildJavaElementArray(IType self, TypeResult[] types) {
        Collection result = new ArrayList(types.length);
        for (int i = 0; i < types.length; i++) {
            TypeResult typeResult = types[i];
            if (!"java.lang.Object".equals(typeResult.getType().getFullyQualifiedName()) &&
                    !self.equals(typeResult.getType())) { 
                result.add(typeResult.getType());
            }
        }
        return (IJavaElement[]) result.toArray(new IJavaElement[result.size()]);
    }

    /**    
     * Searches for interfaces which are implemented by the declaring classes of
     * the specified Java elements. Only the first element of the elements parameter is
     * taken into consideration.
    * 
    * @param elements
     * @return An array of found interfaces implemented by the declaring classes
     *         of the specified Java elements (currently only IMethod instances)
    */
    public IJavaElement[] searchForInterfacesAndAbstractTypes(IJavaElement[] elements, IProgressMonitor progressMonitor) {
        if (elements != null && elements.length > 0) {
            IJavaElement element = elements[0];
    
            try {
                element = getFieldConverter().convertFieldOrLocalVariableToType(element);

                if (element instanceof IMethod) {
                    IMethod method = (IMethod) element;
                    IType type = method.getDeclaringType();
                    TypeResult[] interfaces = findInterfacesAndAbstractTypes(type, progressMonitor);
                    if (!progressMonitor.isCanceled()) {
                        return findMethods(method, interfaces, true, progressMonitor);
                    }
                } else if (element instanceof IType) {
                    IType type = (IType) element;
                    TypeResult[] interfaces = findInterfacesAndAbstractTypes(type, progressMonitor);
                    return buildJavaElementArray(type, interfaces);
                }
            } catch (Exception e) {
                Implementors.log(e);
            }
        }

        return null;
    }

    /**
     * @param type
     * @return
     */
    private TypeResult[] findInterfacesAndAbstractTypes(IType type, IProgressMonitor progressMonitor) {
        Collection interfacesAndAbstractTypes = new ArrayList();

        IImplementorFinder[] finders = getImplementorFinders();

        for (int i = 0; i < finders.length && !progressMonitor.isCanceled(); i++) {
            Collection types = finders[i].findInterfacesAndAbstractTypes(type, new SubProgressMonitor(progressMonitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));

            if (types != null) {
                interfacesAndAbstractTypes.addAll(types);
            }
        }
        return (TypeResult[]) interfacesAndAbstractTypes.toArray(new TypeResult[interfacesAndAbstractTypes.size()]);
    }

    /**
     * Finds IMethod instances on the specified IType instances with identical
     * signatures as the specified IMethod parameter.
     * 
     * @param method
     *            The method to find "equals" of.
     * @param types
     *            The types in which the search is performed.
     * @return          An array of methods which match the method parameter.
     */
    private IJavaElement[] findMethods(IMethod method, TypeResult[] types, boolean allowAbstractMethods,
            IProgressMonitor progressMonitor) {
        Collection foundMethods = new ArrayList();
            
        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(progressMonitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
        subProgressMonitor.beginTask("", types.length); //$NON-NLS-1$
        try {
    
            for (int i = 0; i < types.length; i++) {
                TypeResult typeResult = types[i];
                IMethod[] methods = typeResult.getMethods(method);
                if (methods != null) {
                    for (int j = 0; j < methods.length; j++) {
                        int flags= methods[j].getFlags();
                        if (((allowAbstractMethods && !method.equals(methods[j])) || (!allowAbstractMethods && !Flags.isAbstract(flags))) && !Flags.isSynthetic(flags)) {
                            foundMethods.add(methods[j]);
                        }
                    }
                }
                subProgressMonitor.worked(1);
            }
        } catch (JavaModelException jme) {
            Implementors.log(jme);
        } finally {
            subProgressMonitor.done();
        }

        return (IJavaElement[]) foundMethods.toArray(new IJavaElement[foundMethods.size()]);
    }

    private TypeResult[] findImplementingTypes(IType type, IProgressMonitor progressMonitor) {
        Collection implementingTypes = new ArrayList();

        IImplementorFinder[] finders = getImplementorFinders();

        for (int i = 0; i < finders.length && !progressMonitor.isCanceled(); i++) {
            Collection typeResults = finders[i].findImplementingTypes(type, new SubProgressMonitor(progressMonitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
            if (typeResults != null) {
                implementingTypes.addAll(typeResults);
            }
        }
        return (TypeResult[]) implementingTypes.toArray(new TypeResult[implementingTypes.size()]);
    }
}
