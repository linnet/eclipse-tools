package dk.kamstruplinnet.projecttransfer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author jl
 */
public abstract class ProjectAnalyzer implements IProjectAnalyzer {

    private Collection mMissing = new HashSet();

    protected boolean isJavaProject(final IProject project) throws CoreException {
        return project.hasNature(JavaCore.NATURE_ID);
    }

    public void analyzeProjects(Collection createdProjects) {
        for (Iterator iter = createdProjects.iterator(); iter.hasNext();) {
            IProject project = (IProject) iter.next();
            try {
                analyzeProject(project);
            } catch (CoreException e) {
                ProjectTransferPlugin.log(e);
            }
            
        }
    }

    /**
     * @param project
     */
    protected abstract void analyzeProject(IProject project) throws CoreException;

    protected boolean addMissing(String missingVariable) {
        return mMissing.add(missingVariable);
    }
    
    /* (non-Javadoc)
     * @see dk.kamstruplinnet.projecttransfer.IProjectAnalyzer#getMissingIds()
     */
    public Collection getMissing() {
        return mMissing;
    }
    
    public boolean hasMissing() {
        return !mMissing.isEmpty();
    }

    public String getMissingFormatted() {
        StringBuffer buf = new StringBuffer();
        
        for (Iterator iter = getMissing().iterator(); iter.hasNext();) {
            String variable = (String) iter.next();
            buf.append(Messages.getFormattedString(getMissingFormatString(), variable));
        }
        buf.append('\n');
        return buf.toString();
    }

    protected abstract String getMissingFormatString();
    
}
