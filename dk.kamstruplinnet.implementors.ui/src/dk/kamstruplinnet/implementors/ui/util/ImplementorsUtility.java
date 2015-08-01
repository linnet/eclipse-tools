package dk.kamstruplinnet.implementors.ui.util;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

/**
 * This class copies functionality from the internal EditorInput class in order to
 * open IJavaElements without activating the editor.
 * 
 * @author jl
 */
public class ImplementorsUtility {
    /**
     * Opens a Java editor for an element (IJavaElement, IFile, IStorage...).
     * Currently the copied functionality is unable to open class files since this
     * would require interfacing with internal API.
     * 
     * @return the IEditorPart or null if wrong element type or opening failed
     */
    public static IEditorPart openInEditor(Object inputElement, boolean activate) throws JavaModelException, PartInitException {
        if (inputElement instanceof IJavaElement) {
            return JavaUI.openInEditor((IJavaElement)inputElement);
        } else {
            return null;
        }
    }
}
