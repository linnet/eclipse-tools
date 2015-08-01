package dk.kamstruplinnet.callers.search;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;

import dk.kamstruplinnet.callers.CallersPlugin;


/**
 * @author jl
 */
public class CallLocation {
    private IMethod mMethod;
    private IMethod mCalledMethod;
    private int mStart;
    private int mEnd;
    private String mCallText;

    /**
     * @param method
     * @param cu
     * @param start
     * @param end
     */
    public CallLocation(IMethod method, IMethod calledMethod, int start, int end) {
        this.mMethod = method;
        this.mCalledMethod = calledMethod;
        this.mStart = start;
        this.mEnd = end;
        
        mCallText = initializeCallText();
    }

    public String toString() {
        return mCallText;
    }

    private String initializeCallText() {
        try {
            ICompilationUnit compilationUnit = mMethod.getCompilationUnit();
            if (mMethod != null && compilationUnit != null) {
                IBuffer buffer = compilationUnit.getBuffer();
                return buffer.getText(mStart, (mEnd-mStart));
            } else {
                return mMethod.getOpenable().getBuffer().getText(mStart, (mEnd-mStart));
            }
        } catch (Exception e) {
            CallersPlugin.logError("CallLocation::toString: Error creating text", e);
            return "- error -";
        }
    }

    /**
     * 
     */
    public int getEnd() {
        return mEnd;
    }

    /**
     * 
     */
    public int getStart() {
        return mStart;
    }
    
    public IMethod getMethod() {
        return mMethod;
    }

    /**
     * @return IMethod
     */
    public IMethod getCalledMethod() {
        return mCalledMethod;
    }
}
