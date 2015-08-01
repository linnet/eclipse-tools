package dk.kamstruplinnet.projecttransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * This class is used for determining a set of prefixes more or less
 * common of the paths supplied.
 *  
 * @author jl
 */
class PathPrefixComputer {
    private Collection mPaths;
    private int mMinimumOccurrences;
    private static IPrefixNormalizer prefixNormalizer;

    private class Prefix {
        public String prefix;
        public int occurrences;
        
        public Prefix(String prefix) {
            this.prefix = prefix;
            this.occurrences = 1;
        }

        public void increment() {
            occurrences++;
        }
    }
    
    private interface IPrefixNormalizer {
        String normalizePrefix(String prefix);
    }
    
    private static class CaseInsensitivePrefixNormalizer implements IPrefixNormalizer {
        public String normalizePrefix(String prefix) {
            return prefix.toUpperCase();
        }
    }
    
    private static class CaseSensitivePrefixNormalizer implements IPrefixNormalizer {
        public String normalizePrefix(String prefix) {
            return prefix;
        }
    }
    
    static {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        if (os == null || os.startsWith("Windows")) { //$NON-NLS-1$
            prefixNormalizer = new CaseInsensitivePrefixNormalizer();
        } else {
            prefixNormalizer = new CaseSensitivePrefixNormalizer();
        }
    }
    
    public PathPrefixComputer(Collection paths) {
        mPaths = paths;
        mMinimumOccurrences = paths.size() > 1 ? 1 : 0;
    }
    
    public Collection getPrefixes() {
        List result = new ArrayList();
        
        Map incidents = new HashMap();
        for (Iterator iter = mPaths.iterator(); iter.hasNext();) {
            IPath path = (IPath) iter.next();
            
            String fullPrefixPath = (path.getDevice() != null ? path.getDevice() : "") + IPath.SEPARATOR; //$NON-NLS-1$
            addPrefixOccurrence(incidents, fullPrefixPath);
            String[] segments = path.segments();
            for (int i = 0; i < segments.length - 1; i++) {
                fullPrefixPath = appendPath(fullPrefixPath, segments[i]);
                addPrefixOccurrence(incidents, fullPrefixPath);
            }
        }
        
        for (Iterator iter = incidents.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Prefix prefix = (Prefix) incidents.get(key);
            if (prefix.occurrences > mMinimumOccurrences) {
                result.add(prefix.prefix);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    /**
     * @param fullPrefixPath
     * @param segment
     * @return
     */
    private String appendPath(String fullPrefixPath, String segment) {
        return fullPrefixPath + segment + IPath.SEPARATOR;
    }

    /**
     * @param fullPrefixPath
     */
    private void addPrefixOccurrence(Map prefixMap, String fullPrefixPath) {
        String prefixKey = getPrefixKey(fullPrefixPath);
        Prefix prefix = (Prefix) prefixMap.get(prefixKey);
        if (prefix == null) {
            prefix = new Prefix(fullPrefixPath);
        } else {
            prefix.increment();
        }
        prefixMap.put(prefixKey, prefix);
    }

    private String getPrefixKey(String fullPrefixPath) {
        return prefixNormalizer.normalizePrefix(fullPrefixPath);
    }
    
    public static void main(String[] args) {
        Collection paths = new ArrayList();
        paths.add(new Path("c:\\data\\something")); //$NON-NLS-1$
        paths.add(new Path("c:\\data\\another")); //$NON-NLS-1$
        paths.add(new Path("c:\\data\\Another\\subproject1")); //$NON-NLS-1$
        paths.add(new Path("c:\\data\\another\\subproject2")); //$NON-NLS-1$
//        paths.add(new Path("/data/something"));
//        paths.add(new Path("/data/another"));
//        paths.add(new Path("/data/another/subproject1"));
//        paths.add(new Path("/data/another/subproject2"));
        
        PathPrefixComputer computer = new PathPrefixComputer(paths);
        Collection prefixes = computer.getPrefixes();
        int i = 0;
        for (Iterator iter = prefixes.iterator(); iter.hasNext();) {
            String prefix = (String) iter.next();
            System.out.println(""+(i++)+": "+prefix);  //$NON-NLS-1$//$NON-NLS-2$
        }
    }
}
