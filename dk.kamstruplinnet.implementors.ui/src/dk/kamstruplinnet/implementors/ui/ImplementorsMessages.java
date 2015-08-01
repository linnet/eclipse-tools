package dk.kamstruplinnet.implementors.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author jl
 */
public class ImplementorsMessages {

    private static final String BUNDLE_NAME= "dk.kamstruplinnet.implementors.ui.ImplementorsMessages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE=
        ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * 
     */
    private ImplementorsMessages() {
        // Nothing here...
    }
    /**
     * @param key
     * @return
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
