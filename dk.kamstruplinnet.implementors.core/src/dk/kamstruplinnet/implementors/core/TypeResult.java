package dk.kamstruplinnet.implementors.core;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;


/**
 * @author jl
 */
public class TypeResult {
    private IType mType;

    public TypeResult(IType type) {
        mType = type;
    }
    
    public IType getType() {
        return mType;
    }

    public IMethod[] getMethods(IMethod method) {
        return mType.findMethods(method);
    }
    
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (obj == this) {
            return true;
        }
        
        if (!(obj instanceof TypeResult)) {
            return false;
        }
        
        TypeResult other = (TypeResult) obj;
        return mType.equals(other.mType);
    }
    
    public int hashCode() {
        return mType.hashCode();
    }
}
