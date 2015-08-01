package dk.kamstruplinnet.quickmarks;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Plugin class.
 */
public class QuickmarksPlugin extends AbstractUIPlugin {
    private static final String QUICKMARKER_TYPE = ".quickmarker"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "dk.kamstruplinnet.quickmarks"; //$NON-NLS-1$
    private static final String QUICKMARKS_DEBUG = PLUGIN_ID + "/debug" ; //$NON-NLS-1$

    static final String FILE = "file"; //$NON-NLS-1$
    static final String NUMBER = "number"; //$NON-NLS-1$


    static boolean DEBUG = false;
    
    private static QuickmarksPlugin plugin;

    /**
     * The constructor.
     */
    public QuickmarksPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
        plugin = this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        try {
            configurePluginDebugOptions();
        } catch (RuntimeException e) {
            log(e);
        }
    }
    
    /**
     * Returns the shared instance.
     */
    public static QuickmarksPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public static void log(Throwable t) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Internal Error in Quickmarks plugin", t)); //$NON-NLS-1$
    }

//    private static String getPluginId() {
//        return getDefault().getBundle().getSymbolicName();
//    }
//
    /**
     * @return
     */
    public String getMarkerType() {
        return PLUGIN_ID + QUICKMARKER_TYPE;
    }

    public void configurePluginDebugOptions(){
        if(isDebugging()){
            String option = Platform.getDebugOption(QUICKMARKS_DEBUG);
            if(option != null) DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
        }
    }

    /**
     * If debugging is enabled, print the exception to the console.
     * 
     * @param e
     */
    public static void debug(Exception e) {
        if (DEBUG) {
            System.out.println(e);
        }
    }
}