package dk.kamstruplinnet.projecttransfer;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class ProjectTransferPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ProjectTransferPlugin plugin;
    private IProjectAnalyzer[] mProjectAnalyzers;
	
	/**
	 * The constructor.
	 */
	public ProjectTransferPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ProjectTransferPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

    /**
     * Returns an import handler, i.e. a class responsible for importing projects. 
     */
    public static IProjectImportHandler getImportHandler() {
        return new ProjectTransferHandler();
    }
    
    /**
     * Returns an import handler, i.e. a class responsible for importing projects. 
     */
    public static IProjectExportHandler getExportHandler() {
        return new ProjectTransferHandler();
    }

    public static void log(Exception e) {
        if (e instanceof CoreException) {
            CoreException ce = (CoreException) e;
            getDefault().getLog().log(ce.getStatus());
        } else {
            getDefault().getLog().log(new Status(IStatus.ERROR, "dk.kamstruplinnet.projecttransfer", 0, "Error in ProjectTransfer", e));  //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    private void addProjectAnalyzer(Collection analyzers, String clazzName) {
        try {
			IProjectAnalyzer projectAnalyzer = (IProjectAnalyzer) Class.forName(clazzName).newInstance();
            addProjectAnalyzer(analyzers, projectAnalyzer);
		} catch (ExceptionInInitializerError e) {
            // Ignore as the class may be invalid on this platform 
            System.err.println(e);
		} catch (ClassNotFoundException e) {
            // Ignore as the class may be invalid on this platform 
            System.err.println(e);
		} catch (InstantiationException e) {
            // Ignore as the class may be invalid on this platform 
            System.err.println(e);
		} catch (Exception e) {
            log(e);
		}
    }
    
    private void addProjectAnalyzer(Collection analyzers, IProjectAnalyzer projectAnalyzer) {
        analyzers.add(projectAnalyzer);
    }
    
    /**
     * @return
     */
    public IProjectAnalyzer[] getProjectAnalyzers() {
        if (mProjectAnalyzers == null) {
            Collection analyzerCollection = new ArrayList();
            addProjectAnalyzer(analyzerCollection, "dk.kamstruplinnet.projecttransfer.ClasspathProjectAnalyzer"); //$NON-NLS-1$
            addProjectAnalyzer(analyzerCollection, "dk.kamstruplinnet.projecttransfer.PathProjectAnalyzer"); //$NON-NLS-1$
            addProjectAnalyzer(analyzerCollection, "dk.kamstruplinnet.projecttransfer.UserLibraryProjectAnalyzer"); //$NON-NLS-1$
            mProjectAnalyzers = (IProjectAnalyzer[]) analyzerCollection.toArray(new IProjectAnalyzer[analyzerCollection.size()]);
        }
        return mProjectAnalyzers;
    }
}
