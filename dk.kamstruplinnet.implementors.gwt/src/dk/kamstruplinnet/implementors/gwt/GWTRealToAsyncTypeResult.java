package dk.kamstruplinnet.implementors.gwt;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import dk.kamstruplinnet.implementors.core.TypeResult;

public class GWTRealToAsyncTypeResult extends TypeResult {
	public GWTRealToAsyncTypeResult(IType type) {
		super(type);
	}
	
	public IMethod[] getMethods(IMethod method) {
        String[] parameterTypes = method.getParameterTypes();
        int parameterCount = parameterTypes.length;

        String[] asyncParameterTypes = new String[parameterCount + 1];
        System.arraycopy(parameterTypes, 0, asyncParameterTypes, 0, parameterCount);
        asyncParameterTypes[parameterCount] = "QAsyncCallback<QString;>;"; 
        
		try {
			IMethod result = JavaModelUtil.findMethod(method.getElementName(), asyncParameterTypes, false, getType());
			if (result != null) {
				return new IMethod[] { result };
			} else {
				return null;
			}
		} catch (JavaModelException e) {
			GWTImplementorsPlugin.log(e);
		}
		
		return null;
		
	}


}
