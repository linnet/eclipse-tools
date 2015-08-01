package dk.kamstruplinnet.callers.search;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import dk.kamstruplinnet.callers.CallersPlugin;
import dk.kamstruplinnet.callers.util.Binding2JavaModel;


/**
 * @author jl
 */
class CalleeAnalyzerVisitor extends ASTVisitor {
    private IMethod mMethod;
    private int mMethodEndPosition;
    private int mMethodStartPosition;
    private CallSearchResultCollector mSearchResults;
    private IProgressMonitor mProgressMonitor;

    CalleeAnalyzerVisitor(IMethod method, IProgressMonitor progressMonitor) {
        mSearchResults = new CallSearchResultCollector();
        this.mMethod = method;
        this.mProgressMonitor = progressMonitor;

        try {
            ISourceRange sourceRange = method.getSourceRange();
            this.mMethodStartPosition = sourceRange.getOffset();
            this.mMethodEndPosition = mMethodStartPosition + sourceRange.getLength();
        } catch (JavaModelException jme) {
            CallersPlugin.logError("Error getting start and end of method: " +
                mMethod.getElementName(), jme);
        }
    }

    /**
     * Method getCallees.
     * @return CallerElement
     */
    public Map getCallees() {
        return mSearchResults.getCallers();
    }

    private boolean isNodeWithinMethod(ASTNode node) {
        int nodeStartPosition = node.getStartPosition();
        int nodeEndPosition = nodeStartPosition + node.getLength();

        if (nodeStartPosition < mMethodStartPosition) {
            return false;
        }

        if (nodeEndPosition > mMethodEndPosition) {
            return false;
        }

        return true;
    }

    /**
     * Adds the specified method binding to the search results.
     * @param calledMethodBinding
     * @param node
     */
    protected void addMethodCall(IMethodBinding calledMethodBinding, ASTNode node) {
        try {
            if (calledMethodBinding != null) {
                mProgressMonitor.worked(1);
                
                ITypeBinding calledTypeBinding = calledMethodBinding.getDeclaringClass();
                IType calledType = null;
                if (!calledTypeBinding.isAnonymous()) {
                    calledType = Binding2JavaModel.find(calledTypeBinding,
                        mMethod.getJavaProject());
                } else {
                    calledType = Binding2JavaModel.find(calledTypeBinding.getInterfaces()[0], mMethod.getJavaProject());
                }

                IMethod calledMethod = Binding2JavaModel.findIncludingSupertypes(calledMethodBinding,
                        calledType, new SubProgressMonitor(mProgressMonitor, 100, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));

                if (calledMethod != null && calledType.isInterface()) {
                    calledMethod = findImplementingMethods(calledMethod);
                }
                
                if (!isIgnoredBySearchScope(calledMethod)) {
                    mSearchResults.addMethod(mMethod, calledMethod, node.getStartPosition(),
                        node.getStartPosition() + node.getLength());
                }
            }
        } catch (JavaModelException jme) {
            CallersPlugin.logError("Error adding callee search result", jme);
        }
    }

    /**
     * @param calledMethod
     */
    private IMethod findImplementingMethods(IMethod calledMethod) {
        Collection implementingMethods = CallersPlugin.getDefault().getImplementingMethods(calledMethod);
        if (implementingMethods.size() == 0 || implementingMethods.size() > 1) {
            return calledMethod;
        } else {
            return (IMethod) implementingMethods.iterator().next();
        }
    }

    /**
     * @param enclosingElement
     * @return
     */
    private boolean isIgnoredBySearchScope(IMethod enclosingElement) {
        if (enclosingElement != null) {
            return !CallersPlugin.getDefault().getSearchScope().encloses(enclosingElement);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ClassInstanceCreation)
     */
    public boolean visit(ClassInstanceCreation node) {
        if (!isNodeWithinMethod(node)) {
            return false;
        }

        addMethodCall(node.resolveConstructorBinding(), node);

        return true;
    }

    /**
     * Find all constructor invocations (<code>this(...)</code>) from the called method. Since we only traverse into the
     * AST on the wanted method declaration, this method should not hit on more constructor
     * invocations than those in the wanted method.
     *
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
     */
    public boolean visit(ConstructorInvocation node) {
        if (!isNodeWithinMethod(node)) {
            return false;
        }

        addMethodCall(node.resolveConstructorBinding(), node);

        return true;
    }

    /**
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
     */
    public boolean visit(MethodDeclaration node) {
        return isNodeWithinMethod(node);
    }

    /**
     * Find all method invocations from the called method. Since we only traverse into the
     * AST on the wanted method declaration, this method should not hit on more method
     * invocations than those in the wanted method.
     *
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodInvocation)
     */
    public boolean visit(MethodInvocation node) {
        if (!isNodeWithinMethod(node)) {
            return false;
        }

        addMethodCall(node.resolveMethodBinding(), node);

        return true;
    }

    /**
     * Find invocations of the supertype's constructor from the called method (=constructor).
     * Since we only traverse into the AST on the wanted method declaration, this method should
     * not hit on more method invocations than those in the wanted method.
     *
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperConstructorInvocation)
     */
    public boolean visit(SuperConstructorInvocation node) {
        if (!isNodeWithinMethod(node)) {
            return false;
        }

        addMethodCall(node.resolveConstructorBinding(), node);

        return true;
    }

    /**
     * Find all method invocations from the called method. Since we only traverse into the
     * AST on the wanted method declaration, this method should not hit on more method
     * invocations than those in the wanted method.
     *
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodInvocation)
     */
    public boolean visit(SuperMethodInvocation node) {
        if (!isNodeWithinMethod(node)) {
            return false;
        }

        addMethodCall(node.resolveMethodBinding(), node);

        return true;
    }
}
