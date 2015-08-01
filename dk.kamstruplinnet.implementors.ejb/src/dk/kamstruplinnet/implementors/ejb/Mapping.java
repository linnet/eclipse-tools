package dk.kamstruplinnet.implementors.ejb;
/**
 * @author jl
 */
interface Mapping {
    static final int COMPONENT_INTERFACE = 1;
    static final int HOME_INTERFACE = 2;
    static final int REMOTE_INTERFACE = COMPONENT_INTERFACE;
    static final int REMOTE_HOME_INTERFACE = HOME_INTERFACE;
    static final int LOCAL_INTERFACE = COMPONENT_INTERFACE;
    static final int LOCAL_HOME_INTERFACE = HOME_INTERFACE;
}
