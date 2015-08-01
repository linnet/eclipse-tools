package dk.kamstruplinnet.callers;

import org.eclipse.jdt.ui.JavaElementLabelProvider;


/**
 * @author jl
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface CallersConstants {
    public static final String GROUP_MAIN = "MENU_MAIN";
    public static final String GROUP_SEARCH_SCOPE = "MENU_SEARCH_SCOPE";

    public static final String PLUGIN_ID = "dk.kamstruplinnet.callers";

    public static final String PREF_DETAIL_SASH_TREE_WIDTH = "PREF_DETAIL_SASH_TREE_WIDTH"; 
    public static final String PREF_DETAIL_SASH_ORIENTATION = "PREF_DETAIL_SASH_ORIENTATION";
    
    public static final String PREF_JAVA_LABEL_FORMAT = "PREF_JAVA_LABEL_FORMAT";            
    
    public static final String PREF_USE_FILTERS = "PREF_USE_FILTERS";
    public static final String PREF_INACTIVE_FILTERS_LIST = "PREF_INACTIVE_FILTERS_LIST";
    public static final String PREF_ACTIVE_FILTERS_LIST = "PREF_ACTIVE_FILTERS_LIST";

    public static final String JAVA_FORMAT_DEFAULT = String.valueOf(JavaElementLabelProvider.SHOW_DEFAULT);
    public static final String JAVA_FORMAT_LONG = String.valueOf(JavaElementLabelProvider.SHOW_OVERLAY_ICONS | JavaElementLabelProvider.SHOW_PARAMETERS | JavaElementLabelProvider.SHOW_RETURN_TYPE | JavaElementLabelProvider.SHOW_POST_QUALIFIED);
    public static final String JAVA_FORMAT_SHORT = String.valueOf(JavaElementLabelProvider.SHOW_BASICS);

    public static final String DETAIL_ORIENTATION_VERTICAL = "vertical";
    public static final String DETAIL_ORIENTATION_HORIZONTAL = "horizontal";

    public static final String PREF_MAX_CALL_DEPTH = "PREF_MAX_CALL_DEPTH";
    
    public static final String PREF_ACTIVATE_EDITOR_ON_SELECT = "PREF_ACTIVATE_EDITOR_ON_SELECT";

    public static final String PREF_USE_IMPLEMENTORS_FOR_CALLER_SEARCH = "PREF_USE_IMPLEMENTORS_FOR_CALLER_SEARCH";
    public static final String PREF_USE_IMPLEMENTORS_FOR_CALLEE_SEARCH = "PREF_USE_IMPLEMENTORS_FOR_CALLEE_SEARCH";
}
