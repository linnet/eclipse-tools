package dk.kamstruplinnet.implementors.ui;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import dk.kamstruplinnet.implementors.core.Implementors;

/**
 * The main plugin class to be used in the desktop.
 */
public class ImplementorsUI extends AbstractUIPlugin {
    //The shared instance.
	private static ImplementorsUI plugin;
	//Resource bundle.
	
	/**
	 * The constructor.
	 */
	public ImplementorsUI(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

    /**
	 * Returns the shared instance.
	 */
	public static ImplementorsUI getInstance() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

    public static void logDebug(String message) {
        if (getInstance().isDebugging()) {
            getInstance().getLog().log(new Status(IStatus.INFO, getPluginId(), IStatus.OK,
                    message, null));
        }
    }

    public static void log(Throwable t) {
        getInstance().getLog().log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Internal Error in Implementors UI plugin", t));  //$NON-NLS-1$
    }
    
    private static String getPluginId() {
        return getInstance().getDescriptor().getUniqueIdentifier();
    }
    
    /**
     * Searches for implementors of the specified Java elements.
     * Only the first element of the elements parameter is taken into consideration.
     * 
     * @param elements
     * @return An array of found implementing Java elements (currently only IMethod instances)
     */
    public IJavaElement[] searchForImplementors(IJavaElement[] elements, IProgressMonitor progressMonitor) {
        return getImplementors().searchForImplementors(elements, progressMonitor);
    }

    /**    
     * Searches for interfaces which are implemented by the declaring classes of the specified Java elements.
     * Also, only the first element of the elements parameter is taken into consideration.
     * 
     * @param elements
     * @return An array of found interfaces implemented by the declaring classes of the specified Java elements
     *           (currently only IMethod instances)
     */
    public IJavaElement[] searchForInterfacesAndAbstractTypes(IJavaElement[] elements, IProgressMonitor progressMonitor) {
        return getImplementors().searchForInterfacesAndAbstractTypes(elements, progressMonitor);
    }        
    
    private Implementors getImplementors() {
        Plugin corePlugin = Platform.getPlugin("dk.kamstruplinnet.implementors.core");
        return (Implementors) corePlugin;
    }
}
