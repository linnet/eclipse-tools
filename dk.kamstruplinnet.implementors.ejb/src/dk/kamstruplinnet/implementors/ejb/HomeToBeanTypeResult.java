package dk.kamstruplinnet.implementors.ejb;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import dk.kamstruplinnet.implementors.core.TypeResult;

/**
 * @author jl
 */
class HomeToBeanTypeResult extends TypeResult {
    /**
     * @param type
     */
    public HomeToBeanTypeResult(IType type) {
        super(type);
    }
    
    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.core.TypeResult#getMethods(org.eclipse.jdt.core.IMethod)
     */
    public IMethod[] getMethods(IMethod method) {
        IMethod newMethod = findMethodWithPrefix(method, "ejb");  //$NON-NLS-1$
        if (newMethod == null) {
            newMethod = findMethodWithPrefix(method, "ejbHome");  //$NON-NLS-1$
        }
        
        if (newMethod != null) {
            return new IMethod[] { newMethod };
        } else {
            return super.getMethods(method);
        }
    }
    
    private String uppercaseFirstLetter(String s) {
        if (s != null && s.length() > 0) {
            return Character.toUpperCase(s.charAt(0))+s.substring(1);
        }
        return s;
    }
    
    private IMethod findMethodWithPrefix(IMethod method, String prefix) {
        IMethod result = null;
        try {
            result = JavaModelUtil.findMethod(prefix + uppercaseFirstLetter(method.getElementName()), method.getParameterTypes(), false, getType());
        } catch (JavaModelException e) {
            // Just ignore as this may not be a problem (we try first ordinary ejb methods (like ejbCreate, ejbRemove etc. and second ejbHome methods.)
        }
        return result;
    }
}
