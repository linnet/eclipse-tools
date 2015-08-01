package dk.kamstruplinnet.callers.views;

class TreeRoot {
    public static final Object EMPTY_ROOT = new Integer(42);
    public static final TreeRoot EMPTY_TREE = new TreeRoot(EMPTY_ROOT);
    private Object mRoot;

    /**
     * Constructor for TreeRoot.
     */
    public TreeRoot(Object root) {
        this.mRoot = root;
    }

    Object getRoot() {
        return mRoot;
    }
}
