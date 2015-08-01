package dk.kamstruplinnet.callers;

import org.eclipse.jface.resource.ImageDescriptor;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author jl
 */
public class CallersPluginImages {
    private final static URL BASE_URL = CallersPlugin.getDefault().getDescriptor()
                                                     .getInstallURL();

    public static ImageDescriptor create(String name) {
        return ImageDescriptor.createFromURL(makeImageURL(name));
    }

    private static URL makeImageURL(String name) {
        String path = "icons/" + name;
        URL url = null;

        try {
            url = new URL(BASE_URL, path);
        } catch (MalformedURLException e) {
            CallersPlugin.logError("Image not found", e);

            return null;
        }

        return url;
    }
}
