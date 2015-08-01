package dk.kamstruplinnet.callers.views;

import dk.kamstruplinnet.callers.CallersPlugin;
import dk.kamstruplinnet.callers.search.*;

import org.eclipse.jdt.ui.JavaElementLabelProvider;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.swt.graphics.Image;


/**
 * @author jl
 */
public class CallersLabelProvider extends LabelProvider {
    private JavaElementLabelProvider javaElementLabelProvider = createJavaLabelProvider();

    /*
     * @see ILabelProvider#getText(Object)
     */
    public String getText(Object element) {
        if (element instanceof MethodWrapper) {
            MethodWrapper methodWrapper = (MethodWrapper) element;

            if (methodWrapper.getMethod() != null) {
                return javaElementLabelProvider.getText(methodWrapper.getMethod());
            } else {
                return "Root";
            }
        } else if (element == TreeTermination.INSTANCE){
            return "- Max level reached -";
        }

        return "- no method selected -";
    }

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        if (element instanceof MethodWrapper) {
            MethodWrapper methodWrapper = (MethodWrapper) element;

            if (methodWrapper.getMethod() != null) {
                return javaElementLabelProvider.getImage(methodWrapper.getMethod());
            }
        }

        //        CallersPlugin.getDefault().logDebug("getImage: Returning super's image: "+element);
        return super.getImage(element);
    }

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#dispose()
     */
    public void dispose() {
        super.dispose();

        disposeJavaLabelProvider();
    }

    private void disposeJavaLabelProvider() {
        if (javaElementLabelProvider != null) {
            javaElementLabelProvider.dispose();
        }
    }

    /**
     * @return JavaElementLabelProvider
     */
    private JavaElementLabelProvider createJavaLabelProvider() {
        return new JavaElementLabelProvider(CallersPlugin.getDefault().getJavaLabelSettings());
    }

    /**
     * Updates the Java label provider with the new settings.
     */    
    void updateJavaLabelSettings() {
        disposeJavaLabelProvider();
        javaElementLabelProvider = createJavaLabelProvider();
    }
}
