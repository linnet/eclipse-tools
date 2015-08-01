package dk.kamstruplinnet.implementors.core;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

/**
 * @author jl
 */
public class FieldConverter {
    /* (non-Javadoc)
     * @see dk.kamstruplinnet.implementors.core.FieldConverter#convertFieldOrLocalVariableToType(org.eclipse.jdt.core.IJavaElement)
     */
    public IJavaElement convertFieldOrLocalVariableToType(IJavaElement element) throws JavaModelException {
        if (element instanceof ILocalVariable) {
            ILocalVariable localVariable = (ILocalVariable) element;
            IMember declaringMethod = ((IMember) localVariable.getParent());
            element = findTypeFromSignature(localVariable.getTypeSignature(), declaringMethod.getDeclaringType());
        } else if (element instanceof IField) {
            IField field = (IField) element;
            element = findTypeFromSignature(field.getTypeSignature(), field.getDeclaringType());
        }
        return element;
    }
    
    /**
     * @param element
     * @param localVariable
     * @param declaringMethod
     * @return
     */
    private IJavaElement findTypeFromSignature(String typeSignature, IType declaringType) throws JavaModelException {
        IJavaElement element = null;
        String resolvedTypeName = JavaModelUtil.getResolvedTypeName(typeSignature, declaringType);
        if (resolvedTypeName != null) {
            element = JavaModelUtil.findTypeContainer(declaringType.getJavaProject(), resolvedTypeName);
        }
        return element;
    }
}
