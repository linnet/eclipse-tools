package dk.kamstruplinnet.quickmarks;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author jl
 */
public class Messages {
    private static final String BUNDLE_NAME = "dk.kamstruplinnet.quickmarks.messages";//$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        // TODO Auto-generated method stub
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}