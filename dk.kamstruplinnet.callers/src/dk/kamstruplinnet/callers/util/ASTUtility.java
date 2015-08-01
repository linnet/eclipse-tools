package dk.kamstruplinnet.callers.util;

import dk.kamstruplinnet.callers.CallersPlugin;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;


/**
 * @author jl
 */
public final class ASTUtility {
    /**
     * Returns the fully qualified name for a ITypeBinding or null
     * if the name can't be determined.
     * @param type The binding to determine the fully qualified name for.
     * @return The fully qualified name of the binding.
     */
    public static String fullyQualifiedName(ITypeBinding type) {
        if (type == null) { // check that we really got an ITypeBinding

            return null;
        }

        // if type is anonymous then it doesn't have a name
        if (type.isAnonymous()) {
            return null;
        }

        // if type is an array get the element type
        ITypeBinding baseType = (type.isArray() ? type.getElementType() : type);

        // add the package name (if one is used)
        IPackageBinding typePackage = baseType.getPackage();
        String result = "";

        if (typePackage != null) {
            if (!typePackage.isUnnamed()) {
                String pkgName = typePackage.getName();
                result = pkgName + ".";
            }
        }

        // loop through and add all the declaring classes (only for nested classes)
        ITypeBinding declaringClass = type;

        while (declaringClass.isNested()) {
            declaringClass = declaringClass.getDeclaringClass();
            result += (declaringClass.getName() + ".");
        }

        // finally, add the type name itself and return our result
        result += type.getName().intern();

        return result;
    }

    /**
     * Converts an AST org.eclipse.jdt.core.dom.IVariableBinding to an
     * org.eclipse.jtd.core.IField, or null if the conversion is not possible.
     * @param field The IVariableBinding to convert to an IField.
     * @param in The Java project the binding is contained within.
     * @return The IField corresponding to binding, or null.
     */
    public static IField lookupIField(IVariableBinding field, IJavaProject in) {
        IField result = null; // assume we don't find an IField

        if (field != null) {
            if (field.isField()) {
                IType declaringClass = lookupIType(field.getDeclaringClass(), in);

                if (declaringClass != null) {
                    result = declaringClass.getField(field.getName());

                    if (result == null) {
                        CallersPlugin.logDebug("(lookup) failed to find the field : " +
                            field.getName());
                    }
                } else {
                    CallersPlugin.logDebug(
                        "(no declaring class) failed to find the field : " +
                        field.getName());
                }
            } else {
                CallersPlugin.logDebug("(not a field) failed to find the field : " +
                    field.getName());
            }
        } else {
            CallersPlugin.logDebug("(null binding) failed to find the field");
        }

        return result;
    }

    /**
     * Converts an AST org.eclipse.jdt.core.dom.IMethodBinding to an
     * org.eclipse.jtd.core.IMethod, or null if the conversion is not possible.
     * @param method The IMethodBinding to convert to an IMethod.
     * @param in The Java project the binding is contained within.
     * @return The IMethod corresponding to binding, or null.
     */
    public static IMethod lookupIMethod(IMethodBinding method, IJavaProject in) {
        IMethod result = null; // assume we don't find an IMethod

        if (method != null) {
            String methodName = method.getName();
            int parameterCount = method.getParameterTypes().length;

            // encode a parameter type signature
            String[] parameterTypeSignatures = new String[parameterCount];

            for (int i = 0; i < parameterCount; i++) {
                parameterTypeSignatures[i] = Signature.createTypeSignature(fullyQualifiedName(
                            method.getParameterTypes()[i]), true);
            }

            IType declaringClass = lookupIType(method.getDeclaringClass(), in);

            if (declaringClass != null) {
                result = declaringClass.getMethod(methodName, parameterTypeSignatures);

                if (result == null) {
                    CallersPlugin.logDebug("(lookup) failed to find the method : " +
                        methodName + " " + parameterTypeSignatures);
                }
            } else {
                CallersPlugin.logDebug("(no declaring class) failed to find the field : " +
                    methodName);
            }
        } else {
            CallersPlugin.logDebug("(null binding) failed to find the method");
        }

        return result;
    }

    /**
     * Converts an AST org.eclipse.jdt.core.dom.ITypeBinding into an
     * org.eclipse.jtd.core.IType, or null if the conversion is not possible.
     * This conversion is useful for resolving types on a project basis
     * a project basis (rather than within a single Java file).  If the given
     * ITypeBinding is an array then the IType of the element type is returned.
     * @param type The ITypeBinding to convert to an IType.
     * @param in The Java project the binding is contained within.
     * @return The IType corresponding to binding, or null.
     */
    public static IType lookupIType(ITypeBinding type, IJavaProject in) {
        IType result = null; // assume we don't find an IType

        if (type != null) {
            String fullyQualifiedName = fullyQualifiedName(type);

            if (fullyQualifiedName != null) { // did we get a name?

                try {
                    result = in.findType(fullyQualifiedName);
                } catch (JavaModelException e) {
                    //@ ignore since we return null
                }

                if (result == null) {
                    CallersPlugin.logDebug("(lookup) failed to find the type : " +
                        fullyQualifiedName);
                }
            } else {
                CallersPlugin.logDebug("(fullyQualifiedName) failed to find the type : " +
                    type.getName());
            }
        } else {
            CallersPlugin.logDebug("(null binding) failed to find the type");
        }

        return result;
    }

    /**
     * No instances allowed!
     */
    private ASTUtility() {
    }
    
    
}
