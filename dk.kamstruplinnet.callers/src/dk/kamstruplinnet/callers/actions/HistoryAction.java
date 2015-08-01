package dk.kamstruplinnet.callers.actions;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import dk.kamstruplinnet.callers.views.CallersView;


/**
 * Action used for the type hierarchy forward / backward buttons
 */
public class HistoryAction extends Action {
    private CallersView mView;
    private IMethod mMethod;
    private static JavaElementLabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_POST_QUALIFIED | JavaElementLabelProvider.SHOW_PARAMETERS | JavaElementLabelProvider.SHOW_RETURN_TYPE);

    public HistoryAction(CallersView viewPart, IMethod element) {
        super();
        mView = viewPart;
        mMethod = element;

        String elementName = getElementLabel(element);
        setText(elementName);
        setImageDescriptor(getImageDescriptor(element));

        setDescription(getFormattedString("HistoryAction.description", elementName));
        setToolTipText(getFormattedString("HistoryAction.tooltip", elementName));
    }

    /**
     * @param element
     * @return ImageDescriptor
     */
    private ImageDescriptor getImageDescriptor(IMethod element) {
        return null;
    }

    /**
     * @param string
     * @param elementName
     * @return String
     */
    private String getFormattedString(String prefix, String elementName) {
        return prefix + elementName;
    }

    /**
     * @param element
     * @return String
     */
    private String getElementLabel(IJavaElement element) {
        if (element != null) {
            return labelProvider.getText(element);
        }

        return "- null -";
    }

    //    private ImageDescriptor getImageDescriptor(IJavaElement elem) {
    //        JavaElementImageProvider imageProvider= new JavaElementImageProvider();
    //        ImageDescriptor desc= imageProvider.getBaseImageDescriptor(elem, 0);
    //        imageProvider.dispose();
    //        return desc;
    //    }
    //    

    /*
     * @see Action#run()
     */
    public void run() {
        mView.gotoHistoryEntry(mMethod);
    }
}
