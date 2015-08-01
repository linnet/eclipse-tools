package dk.kamstruplinnet.projecttransfer.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import junit.framework.TestCase;

/**
 * @author jl
 */
public class ExportActionTest extends TestCase {

    /**
     * Constructor for ExportActionTest.
     * @param name
     */
    public ExportActionTest(String name) {
        super(name);
    }

    public void testGetSelectedProjects() {
    }

    public void testGetAllProjectPaths() {
        ExportAction exportAction = new ExportAction();
        
        List expectedList = new ArrayList();
        expectedList.add("c:/java/eclipse/runtime-workspace/Test/.project");
        expectedList.add("c:/java/eclipse/runtime-workspace/Test af Todo/.project");
        
        List projectPaths = exportAction.getAllProjectPaths("c:/java/eclipse/runtime-workspace");
        assertTrue("Project paths were incorrect: "+projectPaths+", expected: "+expectedList, CollectionUtils.isEqualCollection(expectedList, projectPaths));
    }
}
