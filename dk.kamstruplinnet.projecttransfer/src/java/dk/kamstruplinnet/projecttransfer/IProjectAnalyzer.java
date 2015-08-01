package dk.kamstruplinnet.projecttransfer;

import java.util.Collection;

/**
 * @author jl
 */
public interface IProjectAnalyzer {
    /**
     * @param createdProjects
     */
    public void analyzeProjects(Collection createdProjects);
    
    public Collection getMissing();

    /**
     * @return
     */
    public String getMissingFormatted();

    /**
     * @return
     */
    public boolean hasMissing();
}