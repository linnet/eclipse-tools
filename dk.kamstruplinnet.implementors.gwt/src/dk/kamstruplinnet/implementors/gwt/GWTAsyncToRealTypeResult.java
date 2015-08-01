package dk.kamstruplinnet.implementors.gwt;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import dk.kamstruplinnet.implementors.core.TypeResult;

public class GWTAsyncToRealTypeResult extends TypeResult {

	public GWTAsyncToRealTypeResult(IType type) {
		super(type);
	}
	
	public IMethod[] getMethods(IMethod method) {
        String[] asyncParameterTypes = method.getParameterTypes();
        int parameterCount = asyncParameterTypes.length;
		String[] parameterTypes = new String[parameterCount - 1];
        System.arraycopy(asyncParameterTypes, 0, parameterTypes, 0, parameterCount - 1);
		try {
			IMethod result = JavaModelUtil.findMethod(method.getElementName(), parameterTypes, false, getType());
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
