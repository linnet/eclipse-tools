package dk.kamstruplinnet.implementors.ejb;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import dk.kamstruplinnet.implementors.core.TypeResult;

/**
 * This class is used for parsing the relevant parts of a EJB deployment descriptor.
 * 
 * @author jl
 */
class EJBDescriptorParser {
    static final int COMPONENT_INTERFACE = 1;
    static final int HOME_INTERFACE = 2;
    static final int REMOTE_INTERFACE = COMPONENT_INTERFACE;
    static final int REMOTE_HOME_INTERFACE = HOME_INTERFACE;
    static final int LOCAL_INTERFACE = COMPONENT_INTERFACE;
    static final int LOCAL_HOME_INTERFACE = HOME_INTERFACE;
    
    private IProgressMonitor mProgressMonitor;
    private String mClassName;
    private boolean mWithinBeanDeclaration;
    private String mRemote;
    private String mRemoteHome;
    private String mLocal;
    private String mLocalHome;
    private String mEjbClass;
    private IJavaProject mProject;

    private Map mInterfaceMap;
    private Map mImplementorMap;

    EJBDescriptorParser(IJavaProject project, IProgressMonitor progressMonitor) {
        this.mProject = project;
        this.mProgressMonitor = progressMonitor;
    }

    void parseEJBMappings(IPath path) {
        mImplementorMap = new HashMap();
        mInterfaceMap = new HashMap();

        try {
            XmlPullParser xpp = createParser();
            xpp.setInput(new FileReader(path.toFile()));
            processDocument(xpp);
        } catch (FileNotFoundException e) {
            EJBImplementorsPlugin.log(e);
        } catch (Exception e) {
            EJBImplementorsPlugin.log(e);
        }
    }

    private XmlPullParser createParser() {
        XmlPullParser xpp = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(
                        XmlPullParserFactory.PROPERTY_NAME), this.getClass());

            factory.setValidating(false);
            factory.setNamespaceAware(false);

            xpp = factory.newPullParser();
        } catch (XmlPullParserException e) {
            EJBImplementorsPlugin.log(e);
        }

        return xpp;
    }

    private void addMapping(String interfaceClassName, String ejbClassName, int interfaceKind) {
        IType interfaceType = lookupType(mProject, interfaceClassName);
        IType ejbClassType = lookupType(mProject, ejbClassName);

        if ((interfaceType != null) && (ejbClassType != null)) {
            if (interfaceKind == HOME_INTERFACE) {
                addMapping(mImplementorMap, interfaceType, new HomeToBeanTypeResult(ejbClassType));
                addMapping(mInterfaceMap, ejbClassType, new BeanToHomeTypeResult(interfaceType));
            } else {
                addMapping(mImplementorMap, interfaceType, new TypeResult(ejbClassType));
                addMapping(mInterfaceMap, ejbClassType, new TypeResult(interfaceType));
        }
    }
    }

    private void addMapping(Map map, IType keyType, TypeResult relatedType) {
        String handleIdentifier = keyType.getHandleIdentifier();
        Collection mappings = (Collection) map.get(handleIdentifier);
        
        if (mappings == null) {
            mappings = new HashSet();
            map.put(handleIdentifier, mappings);
        }
        
        mappings.add(relatedType);
    }

    /**
     * @param ejbClass
     * @return
     */
    private IType lookupType(IJavaProject project, String fullyQualifiedName) {
        if ((project != null) && (fullyQualifiedName != null)) {
            try {
                return project.findType(fullyQualifiedName);
            } catch (JavaModelException e) {
                EJBImplementorsPlugin.log(e);
            }
        }

        return null;
    }

    public void processDocument(XmlPullParser xpp)
        throws XmlPullParserException, IOException {
        mWithinBeanDeclaration = false;

        int eventType = xpp.getEventType();

        do {
            if (eventType == XmlPullParser.START_TAG) {
                processStartElement(xpp);
            } else if (eventType == XmlPullParser.END_TAG) {
                processEndElement(xpp);
            } else if (eventType == XmlPullParser.TEXT) {
                processText(xpp);
            }

            eventType = xpp.next();
            if (mProgressMonitor != null) {
                mProgressMonitor.worked(1);
            }
        } while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled());
    }

    /**
     * @return
     */
    private boolean isCancelled() {
        if (mProgressMonitor != null) {
            return mProgressMonitor.isCanceled();
        }
        return false;
    }

    public void processStartElement(XmlPullParser xpp) {
        String name = xpp.getName();

        if (isBeanTypeElement(name)) {
            mWithinBeanDeclaration = true;
            
            mRemote = null;
            mRemoteHome = null;
            mLocal = null;
            mLocalHome = null;
        }
    }

    private boolean isBeanTypeElement(String name) {
        return "session".equals(name) || "entity".equals(name); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void processEndElement(XmlPullParser xpp) {
        String name = xpp.getName();

        if (isBeanTypeElement(name)) {
            mWithinBeanDeclaration = false;
        }

        if (mWithinBeanDeclaration) {
            if ("remote".equals(name)) { //$NON-NLS-1$
                mRemote = getClassName();
            } else if ("home".equals(name)) { //$NON-NLS-1$
                mRemoteHome = getClassName();
            } else if ("local".equals(name)) { //$NON-NLS-1$
                mLocal = getClassName();
            } else if ("local-home".equals(name)) { //$NON-NLS-1$
                mLocalHome = getClassName();
            } else if ("ejb-class".equals(name)) { //$NON-NLS-1$
                mEjbClass = getClassName();

                if (mEjbClass != null) {
                    addMapping(mRemote, mEjbClass, Mapping.REMOTE_INTERFACE);
                    addMapping(mRemoteHome, mEjbClass, Mapping.REMOTE_HOME_INTERFACE);
                    addMapping(mLocal, mEjbClass, Mapping.LOCAL_INTERFACE);
                    addMapping(mLocalHome, mEjbClass, Mapping.LOCAL_HOME_INTERFACE);
                }
                mWithinBeanDeclaration = false;
            }
        }
    }

    /**
 * @return
 */
    private String getClassName() {
        return mClassName;
    }

    public void processText(XmlPullParser xpp) {
        if (mWithinBeanDeclaration) {
            mClassName = xpp.getText();
        }
    }
    /**
     * @return
     */
    public Map getImplementors() {
        return mImplementorMap;
    }

    /**
     * @return
     */
    public Map getInterfaces() {
        return mInterfaceMap;
    }

}
