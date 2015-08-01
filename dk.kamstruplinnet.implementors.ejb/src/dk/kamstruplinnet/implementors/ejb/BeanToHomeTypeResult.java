package dk.kamstruplinnet.implementors.ejb;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import dk.kamstruplinnet.implementors.core.TypeResult;

/**
 * @author jl
 */
class BeanToHomeTypeResult extends TypeResult {
    private static final String EJB_HOME_PREFIX = "ejbHome"; //$NON-NLS-1$
    private static final String EJB_PREFIX = "ejb"; //$NON-NLS-1$
    private final static int EJB_PREFIX_LENGTH = EJB_PREFIX.length();
    private final static int EJB_HOME_PREFIX_LENGTH = EJB_HOME_PREFIX.length();
    
    /**
     * @param type
     */
    public BeanToHomeTypeResult(IType type) {
        super(type);
    }
    
    public IMethod[] getMethods(IMethod method) {
        String methodName = method.getElementName();
        int methodPrefix = getMethodPrefixLength(methodName);
        
        if (methodPrefix > 0) {
            String interfaceMethodName;
            if ("ejbPostCreate".equals(methodName)) { //$NON-NLS-1$
                interfaceMethodName = "create"; //$NON-NLS-1$
            } else {
                interfaceMethodName = lowercaseFirstLetter(methodName.substring(methodPrefix));
            }
            
            IMethod newMethod = null;
            try {
                newMethod = JavaModelUtil.findMethod(interfaceMethodName, method.getParameterTypes(), false, getType());
            } catch (JavaModelException e) {
                EJBImplementorsPlugin.log(e);
            }
            
            return new IMethod[] { newMethod };
        } else {
            return super.getMethods(method);
        }
    }
    
    private String lowercaseFirstLetter(String s) {
        if (s != null && s.length() > 0) {
            return Character.toLowerCase(s.charAt(0))+s.substring(1);
        }
        return s;
    }
    
    private int getMethodPrefixLength(String methodName) {
        if (isEjbHomeMethod(methodName)) {
            return EJB_HOME_PREFIX_LENGTH;
        } else if (isEjbMethod(methodName)) {
            return EJB_PREFIX_LENGTH;
        }
        return 0;
    }
    
    private boolean isEjbMethod(String methodName) {
        return methodName.startsWith(EJB_PREFIX) && methodName.length() > EJB_PREFIX_LENGTH;
    }
    
    private boolean isEjbHomeMethod(String methodName) {
        return methodName.startsWith(EJB_HOME_PREFIX) && methodName.length() > EJB_HOME_PREFIX_LENGTH;
    }
    
}
