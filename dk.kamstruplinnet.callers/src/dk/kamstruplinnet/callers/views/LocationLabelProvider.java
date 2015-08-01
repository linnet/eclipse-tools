package dk.kamstruplinnet.callers.views;

import org.eclipse.jface.viewers.LabelProvider;

import dk.kamstruplinnet.callers.search.CallLocation;


/**
 * @author jl
 */
public class LocationLabelProvider extends LabelProvider {
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof CallLocation) {
            CallLocation callLocation = (CallLocation) element;
            return removeWhitespaceOutsideStringLiterals(callLocation.toString());
        }
        return super.getText(element);
    }

    /**
     * @param string
     * @return String
     */
    private String removeWhitespaceOutsideStringLiterals(String s) {
        StringBuffer buf = new StringBuffer();
        boolean withinString = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"') {
                withinString = !withinString;
            }
            if (withinString) {
                buf.append(ch);
            } else if (Character.isWhitespace(ch)) {
                if (buf.length() == 0 || !Character.isWhitespace(buf.charAt(buf.length()-1))) {
                    if (ch != ' ')
                        ch = ' ';
                    buf.append(ch);
                }
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

}
