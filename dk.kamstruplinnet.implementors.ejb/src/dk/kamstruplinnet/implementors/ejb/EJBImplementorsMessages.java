package dk.kamstruplinnet.implementors.ejb;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author jl
 */
public class EJBImplementorsMessages {

    private static final String BUNDLE_NAME= "dk.kamstruplinnet.implementors.ejb.EJBImplementorsMessages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE=
        ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * 
     */
    private EJBImplementorsMessages() {
        // Do nothing...
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
    
    /**
     * Gets a string from the resource bundle and formats it with the argument
     *
     * @param key   the string used to get the bundle value, must not be null
     */
    public static String getFormattedString(String key, Object arg) {
        String format = null;

        try {
            format = RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!"; //$NON-NLS-2$ //$NON-NLS-1$
        }

        if (arg == null) {
            arg = ""; //$NON-NLS-1$
        }

        return MessageFormat.format(format, new Object[] { arg });
    }

    /**
     * Gets a string from the resource bundle and formats it with arguments
     */
    public static String getFormattedString(String key, String[] args) {
        return MessageFormat.format(RESOURCE_BUNDLE.getString(key), args);
    }
}
