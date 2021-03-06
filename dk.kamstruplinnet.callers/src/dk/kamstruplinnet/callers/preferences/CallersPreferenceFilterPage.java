package dk.kamstruplinnet.callers.preferences;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchViewerSorter;

import dk.kamstruplinnet.callers.CallersConstants;
import dk.kamstruplinnet.callers.CallersPlugin;


public class CallersPreferenceFilterPage extends PreferencePage
    implements IWorkbenchPreferencePage {
    private static final String DEFAULT_NEW_FILTER_TEXT = ""; //$NON-NLS-1$

    // Step filter widgets
    private CheckboxTableViewer fFilterViewer;
    private Table fFilterTable;
    protected Button fUseFiltersCheckbox;
    private Button fAddPackageButton;
    protected Button fRemoveFilterButton;
    private Button fAddFilterButton;
    private Button fEnableAllButton;
    private Button fDisableAllButton;
    protected Text fEditorText;
    protected String fInvalidEditorText = null;
    private TableEditor fTableEditor;
    private TableItem fNewTableItem;
    private Filter fNewStepFilter;
    private Label fTableLabel;
    FilterContentProvider fStepFilterContentProvider;

    public CallersPreferenceFilterPage() {
        super();
        setPreferenceStore(CallersPlugin.getDefault().getPreferenceStore());
        setDescription("Options for Call Hierarchy"); //$NON-NLS-1$
    }

    protected Control createContents(Composite parent) {
        //The main composite
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);

        createFilterPreferences(composite);

        return composite;
    }

    /**
 * @see IWorkbenchPreferencePage#init(IWorkbench)
 */
    public void init(IWorkbench workbench) {
    }

    /**
     * Set the default preferences for this page.
     */
    public static void initDefaults(IPreferenceStore store) {
        CallersPlugin.getDefault().initializeDefaultFilterPreferences(store);
    }

    /**
 * Create a group to contain the step filter related widgetry
 */
    private void createFilterPreferences(Composite parent) {
        // top level container
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);

        GridData gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);

        // use filters checkbox
        fUseFiltersCheckbox = new Button(container, SWT.CHECK);
        fUseFiltersCheckbox.setText("Use package filters"); //$NON-NLS-1$
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        fUseFiltersCheckbox.setLayoutData(gd);

        fUseFiltersCheckbox.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent se) {
                    toggleFilterWidgetsEnabled(fUseFiltersCheckbox.getSelection());
                }

                public void widgetDefaultSelected(SelectionEvent se) {
                }
            });

        //table label
        fTableLabel = new Label(container, SWT.NONE);
        fTableLabel.setText("Defined package filters"); //$NON-NLS-1$
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        fTableLabel.setLayoutData(gd);

        fFilterTable = new Table(container,
                SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION |
                SWT.BORDER);
        fFilterTable.setHeaderVisible(true);
        fFilterTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableLayout tlayout = new TableLayout();
        tlayout.addColumnData(new ColumnWeightData(100, 200));
        fFilterTable.setLayout(tlayout);

        TableColumn tableCol = new TableColumn(fFilterTable, SWT.LEFT);
        tableCol.setResizable(true);
        tableCol.setText("Enable/disable package");

        fFilterViewer = new CheckboxTableViewer(fFilterTable);
        fTableEditor = new TableEditor(fFilterTable);
        fFilterViewer.setLabelProvider(new FilterLabelProvider());
        fFilterViewer.setSorter(new FilterViewerSorter());
        fStepFilterContentProvider = new FilterContentProvider(fFilterViewer);
        fFilterViewer.setContentProvider(fStepFilterContentProvider);

        //@todo table width input just needs to be non-null
        fFilterViewer.setInput(this);

        fFilterViewer.addCheckStateListener(new ICheckStateListener() {
                public void checkStateChanged(CheckStateChangedEvent event) {
                    Filter filter = (Filter) event.getElement();
                    fStepFilterContentProvider.toggleFilter(filter);
                }
            });
        fFilterViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    ISelection selection = event.getSelection();

                    if (selection.isEmpty()) {
                        fRemoveFilterButton.setEnabled(false);
                    } else {
                        fRemoveFilterButton.setEnabled(true);
                    }
                }
            });

        createFilterButtons(container);

        boolean enabled = getPreferenceStore().getBoolean(CallersConstants.PREF_USE_FILTERS);
        fUseFiltersCheckbox.setSelection(enabled);
        toggleFilterWidgetsEnabled(enabled);
    }

    private void createFilterButtons(Composite container) {
        // button container
        Composite buttonContainer = new Composite(container, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        buttonContainer.setLayoutData(gd);

        GridLayout buttonLayout = new GridLayout();
        buttonLayout.numColumns = 1;
        buttonLayout.marginHeight = 0;
        buttonLayout.marginWidth = 0;
        buttonContainer.setLayout(buttonLayout);

        // Add filter button
        fAddFilterButton = new Button(buttonContainer, SWT.PUSH);
        fAddFilterButton.setText("Add filter"); //$NON-NLS-1$
        fAddFilterButton.setToolTipText("Key in the name of a new package filter"); //$NON-NLS-1$
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        fAddFilterButton.setLayoutData(gd);
        fAddFilterButton.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event e) {
                    editFilter();
                }
            });

        // Add package button
        fAddPackageButton = new Button(buttonContainer, SWT.PUSH);
        fAddPackageButton.setText("Add package..."); //$NON-NLS-1$
        fAddPackageButton.setToolTipText("Choose a package and add it to package filters"); //$NON-NLS-1$
        gd = getButtonGridData(fAddPackageButton);
        fAddPackageButton.setLayoutData(gd);
        fAddPackageButton.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event e) {
                    addPackage();
                }
            });

        // Remove button
        fRemoveFilterButton = new Button(buttonContainer, SWT.PUSH);
        fRemoveFilterButton.setText("Remove"); //$NON-NLS-1$
        fRemoveFilterButton.setToolTipText("Remove all selected package filters"); //$NON-NLS-1$
        gd = getButtonGridData(fRemoveFilterButton);
        fRemoveFilterButton.setLayoutData(gd);
        fRemoveFilterButton.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event e) {
                    removeFilters();
                }
            });
        fRemoveFilterButton.setEnabled(false);

        fEnableAllButton = new Button(buttonContainer, SWT.PUSH);
        fEnableAllButton.setText("Enable all"); //$NON-NLS-1$
        fEnableAllButton.setToolTipText("Enables all package filters"); //$NON-NLS-1$
        gd = getButtonGridData(fEnableAllButton);
        fEnableAllButton.setLayoutData(gd);
        fEnableAllButton.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event e) {
                    checkAllFilters(true);
                }
            });

        fDisableAllButton = new Button(buttonContainer, SWT.PUSH);
        fDisableAllButton.setText("Disable all"); //$NON-NLS-1$
        fDisableAllButton.setToolTipText("Disables all package filters"); //$NON-NLS-1$
        gd = getButtonGridData(fDisableAllButton);
        fDisableAllButton.setLayoutData(gd);
        fDisableAllButton.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event e) {
                    checkAllFilters(false);
                }
            });
    }

    void toggleFilterWidgetsEnabled(boolean enabled) {
        fFilterViewer.getTable().setEnabled(enabled);
        fAddPackageButton.setEnabled(enabled);
        fAddFilterButton.setEnabled(enabled);

        if (!enabled) {
            fRemoveFilterButton.setEnabled(enabled);
        } else if (!fFilterViewer.getSelection().isEmpty()) {
            fRemoveFilterButton.setEnabled(true);
        }
    }

    private GridData getButtonGridData(Button button) {
        GridData gd = new GridData(GridData.FILL_HORIZONTAL |
                GridData.VERTICAL_ALIGN_BEGINNING);
        GC gc = new GC(button);
        gc.setFont(button.getFont());

        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
                IDialogConstants.BUTTON_WIDTH);
        gd.widthHint = Math.max(widthHint,
                button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);

        gd.heightHint = Dialog.convertVerticalDLUsToPixels(fontMetrics,
                IDialogConstants.BUTTON_HEIGHT);

        return gd;
    }

    void checkAllFilters(boolean check) {
        Object[] filters = fStepFilterContentProvider.getElements(null);

        for (int i = 0; i != filters.length; i++) {
            ((Filter) filters[i]).setChecked(check);
        }

        fFilterViewer.setAllChecked(check);
    }

    /**
 * Create a new filter in the table (with the default 'new filter' value),
 * then open up an in-place editor on it.
 */
    void editFilter() {
        // if a previous edit is still in progress, finish it
        if (fEditorText != null) {
            validateChangeAndCleanup();
        }

        fNewStepFilter = fStepFilterContentProvider.addFilter(DEFAULT_NEW_FILTER_TEXT,
                true);
        fNewTableItem = fFilterTable.getItem(0);

        // create & configure Text widget for editor
        // Fix for bug 1766.  Border behavior on for text fields varies per platform.
        // On Motif, you always get a border, on other platforms,
        // you don't.  Specifying a border on Motif results in the characters
        // getting pushed down so that only there very tops are visible.  Thus,
        // we have to specify different style constants for the different platforms.
        int textStyles = SWT.SINGLE | SWT.LEFT;

        if (!SWT.getPlatform().equals("motif")) { //$NON-NLS-1$
            textStyles |= SWT.BORDER;
        }

        fEditorText = new Text(fFilterTable, textStyles);

        GridData gd = new GridData(GridData.FILL_BOTH);
        fEditorText.setLayoutData(gd);

        // set the editor
        fTableEditor.horizontalAlignment = SWT.LEFT;
        fTableEditor.grabHorizontal = true;
        fTableEditor.setEditor(fEditorText, fNewTableItem, 0);

        // get the editor ready to use
        fEditorText.setText(fNewStepFilter.getName());
        fEditorText.selectAll();
        setEditorListeners(fEditorText);
        fEditorText.setFocus();
    }

    private void setEditorListeners(Text text) {
        // CR means commit the changes, ESC means abort and don't commit
        text.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent event) {
                    if (event.character == SWT.CR) {
                        if (fInvalidEditorText != null) {
                            fEditorText.setText(fInvalidEditorText);
                            fInvalidEditorText = null;
                        } else {
                            validateChangeAndCleanup();
                        }
                    } else if (event.character == SWT.ESC) {
                        removeNewFilter();
                        cleanupEditor();
                    }
                }
            });

        // Consider loss of focus on the editor to mean the same as CR
        text.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent event) {
                    if (fInvalidEditorText != null) {
                        fEditorText.setText(fInvalidEditorText);
                        fInvalidEditorText = null;
                    } else {
                        validateChangeAndCleanup();
                    }
                }
            });

        // Consume traversal events from the text widget so that CR doesn't 
        // traverse away to dialog's default button.  Without this, hitting
        // CR in the text field closes the entire dialog.
        text.addListener(SWT.Traverse,
            new Listener() {
                public void handleEvent(Event event) {
                    event.doit = false;
                }
            });
    }

    protected void validateChangeAndCleanup() {
        String trimmedValue = fEditorText.getText().trim();

        // if the new value is blank, remove the filter
        if (trimmedValue.length() < 1) {
            removeNewFilter();
        }
        // if it's invalid, beep and leave sitting in the editor
        else if (!validateEditorInput(trimmedValue)) {
            fInvalidEditorText = trimmedValue;
            fEditorText.setText(
                "Invalid package filter. Return to continue; escape to exit."); //$NON-NLS-1$
            getShell().getDisplay().beep();

            return;

            // otherwise, commit the new value if not a duplicate
        } else {
            Object[] filters = fStepFilterContentProvider.getElements(null);

            for (int i = 0; i < filters.length; i++) {
                Filter filter = (Filter) filters[i];

                if (filter.getName().equals(trimmedValue)) {
                    removeNewFilter();
                    cleanupEditor();

                    return;
                }
            }

            fNewTableItem.setText(trimmedValue);
            fNewStepFilter.setName(trimmedValue);
            fFilterViewer.refresh();
        }

        cleanupEditor();
    }

    /**
 * Cleanup all widgetry & resources used by the in-place editing
 */
    protected void cleanupEditor() {
        if (fEditorText != null) {
            fNewStepFilter = null;
            fNewTableItem = null;
            fTableEditor.setEditor(null, null, 0);
            fEditorText.dispose();
            fEditorText = null;
        }
    }

    protected void removeNewFilter() {
        fStepFilterContentProvider.removeFilters(new Object[] { fNewStepFilter });
    }

    /**
 * A valid step filter is simply one that is a valid Java identifier.
 * and, as defined in the JDI spec, the regular expressions used for
 * step filtering must be limited to exact matches or patterns that
 * begin with '*' or end with '*'. Beyond this, a string cannot be validated
 * as corresponding to an existing type or package (and this is probably not
 * even desirable).  
 */
    private boolean validateEditorInput(String trimmedValue) {
        char firstChar = trimmedValue.charAt(0);

        if (!Character.isJavaIdentifierStart(firstChar)) {
            if (!(firstChar == '*')) {
                return false;
            }
        }

        int length = trimmedValue.length();

        for (int i = 1; i < length; i++) {
            char c = trimmedValue.charAt(i);

            if (!Character.isJavaIdentifierPart(c)) {
                if ((c == '.') && (i != (length - 1))) {
                    continue;
                }

                if ((c == '*') && (i == (length - 1))) {
                    continue;
                }

                return false;
            }
        }

        return true;
    }

    /**
 * Utility method to create and return a selection dialog that allows
 * selection of a specific Java package.  Empty packages are not returned.
 * If Java Projects are provided, only packages found within those projects
 * are included.  If no Java projects are provided, all Java projects in the
 * workspace are considered.
 */
    public static ElementListSelectionDialog createAllPackagesDialog(Shell shell,
        IJavaProject[] originals, final boolean includeDefaultPackage)
        throws JavaModelException {
        final List packageList = new ArrayList();

        if (originals == null) {
            IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
            IJavaModel model = JavaCore.create(wsroot);
            originals = model.getJavaProjects();
        }

        final IJavaProject[] projects = originals;
        final JavaModelException[] exception = new JavaModelException[1];
        ProgressMonitorDialog monitor = new ProgressMonitorDialog(shell);
        IRunnableWithProgress r = new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {
                        Set packageNameSet = new HashSet();
                        monitor.beginTask("Searching...", projects.length); //$NON-NLS-1$

                        for (int i = 0; i < projects.length; i++) {
                            IPackageFragment[] pkgs = projects[i].getPackageFragments();

                            for (int j = 0; j < pkgs.length; j++) {
                                IPackageFragment pkg = pkgs[j];

                                if (!pkg.hasChildren() &&
                                            (pkg.getNonJavaResources().length > 0)) {
                                    continue;
                                }

                                String pkgName = pkg.getElementName();

                                if (!includeDefaultPackage && (pkgName.length() == 0)) {
                                    continue;
                                }

                                if (packageNameSet.add(pkgName)) {
                                    packageList.add(pkg);
                                }
                            }

                            monitor.worked(1);
                        }

                        monitor.done();
                    } catch (JavaModelException jme) {
                        exception[0] = jme;
                    }
                }
            };

        try {
            monitor.run(false, false, r);
        } catch (InvocationTargetException e) {
            CallersPlugin.logError("ooo", e);
        } catch (InterruptedException e) {
            CallersPlugin.logError("ooo", e);
        }

        if (exception[0] != null) {
            throw exception[0];
        }

        int flags = JavaElementLabelProvider.SHOW_DEFAULT;
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell,
                new JavaElementLabelProvider(flags));
        dialog.setIgnoreCase(false);
        dialog.setElements(packageList.toArray()); // XXX inefficient

        return dialog;
    }

    void addPackage() {
        Shell shell = getShell();
        ElementListSelectionDialog dialog = null;

        try {
            dialog = createAllPackagesDialog(shell, null, true);
        } catch (JavaModelException jme) {
            String title = "Add package to package filters"; //$NON-NLS-1$
            String message = "Could not open package selection dialog for package filters"; //$NON-NLS-1$
            CallersPlugin.logError(title + message, jme);

            return;
        }

        dialog.setTitle("Add package to package filters"); //$NON-NLS-1$
        dialog.setMessage("Select a package to filter for Call Hierarchy"); //$NON-NLS-1$
        dialog.setMultipleSelection(true);

        if (dialog.open() == IDialogConstants.CANCEL_ID) {
            return;
        }

        Object[] packages = dialog.getResult();

        if (packages != null) {
            for (int i = 0; i < packages.length; i++) {
                IJavaElement pkg = (IJavaElement) packages[i];

                String filter = pkg.getElementName();

                if (filter.length() < 1) {
                    filter = "(default package)"; //$NON-NLS-1$
                } else {
                    filter += ".*"; //$NON-NLS-1$
                }

                fStepFilterContentProvider.addFilter(filter, true);
            }
        }
    }

    void removeFilters() {
        IStructuredSelection selection = (IStructuredSelection) fFilterViewer.getSelection();
        fStepFilterContentProvider.removeFilters(selection.toArray());
    }

    /**
 * @see IPreferencePage#performOk()
 */
    public boolean performOk() {
        fStepFilterContentProvider.saveFilters();
        CallersPlugin.getDefault().savePluginPreferences();

        return true;
    }

    /**
 * Sets the default preferences.
 * @see PreferencePage#performDefaults()
 */
    protected void performDefaults() {
        setDefaultValues();
        super.performDefaults();
    }

    private void setDefaultValues() {
        fStepFilterContentProvider.setDefaults();
    }

    /**
 * Parses the comma separated string into an array of strings
 *
 * @return list
 */
    public static String[] parseList(String listString) {
        List list = new ArrayList(10);
        StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            list.add(token);
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    /**
 * Serializes the array of strings into one comma
 * separated string.
 *
 * @param list array of strings
 * @return a single string composed of the given list
 */
    public static String serializeList(String[] list) {
        if (list == null) {
            return ""; //$NON-NLS-1$
        }

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < list.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }

            buffer.append(list[i]);
        }

        return buffer.toString();
    }

    protected void updateActions() {
        if (fEnableAllButton != null) {
            boolean enabled = fFilterViewer.getTable().getItemCount() > 0;
            fEnableAllButton.setEnabled(enabled);
            fDisableAllButton.setEnabled(enabled);
        }
    }

    /**
 * Content provider for the table.  Content consists of instances of StepFilter.
 */
    protected class FilterContentProvider implements IStructuredContentProvider {
        private CheckboxTableViewer fViewer;
        private List fFilters;

        public FilterContentProvider(CheckboxTableViewer viewer) {
            fViewer = viewer;

            List active = createActiveStepFiltersList();
            List inactive = createInactiveStepFiltersList();
            List defaultlist = createDefaultStepFiltersList();
            populateFilters(active, inactive, defaultlist);
            updateActions();
        }

        public void setDefaults() {
            fViewer.remove(fFilters.toArray());

            List active = createActiveStepFiltersList();
            List inactive = createInactiveStepFiltersList();
            List defaultlist = createDefaultStepFiltersList();
            populateFilters(active, inactive, defaultlist);

            boolean useStepFilters = getPreferenceStore().getBoolean(CallersConstants.PREF_USE_FILTERS);
            fUseFiltersCheckbox.setSelection(useStepFilters);
            toggleFilterWidgetsEnabled(useStepFilters);
        }

        protected void populateFilters(List activeList, List inactiveList,
            List defaultlist) {
            fFilters = new ArrayList(activeList.size() + inactiveList.size() +
                    defaultlist.size());
            populateList(inactiveList, false);
            populateList(activeList, true);
            populateList(defaultlist, true);
        }

        protected void populateList(List list, boolean checked) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                String name = (String) iterator.next();
                addFilter(name, checked);
            }
        }

        /**
 * Returns a list of active step filters.
 *
 * @return list
 */
        protected List createActiveStepFiltersList() {
            String[] strings = parseList(getPreferenceStore().getString(CallersConstants.PREF_ACTIVE_FILTERS_LIST));

            return Arrays.asList(strings);
        }

        /**
 * Returns a list of active step filters.
 *
 * @return list
 */
        protected List createDefaultStepFiltersList() {
            String[] strings = parseList(getPreferenceStore().getDefaultString(CallersConstants.PREF_ACTIVE_FILTERS_LIST));

            return Arrays.asList(strings);
        }

        /**
 * Returns a list of active step filters.
 *
 * @return list
 */
        protected List createInactiveStepFiltersList() {
            String[] strings = parseList(getPreferenceStore().getString(CallersConstants.PREF_INACTIVE_FILTERS_LIST));

            return Arrays.asList(strings);
        }

        public Filter addFilter(String name, boolean checked) {
            Filter filter = new Filter(name, checked);

            if (!fFilters.contains(filter)) {
                fFilters.add(filter);
                fViewer.add(filter);
                fViewer.setChecked(filter, checked);
            }

            updateActions();

            return filter;
        }

        public void saveFilters() {
            getPreferenceStore().setValue(CallersConstants.PREF_USE_FILTERS,
                fUseFiltersCheckbox.getSelection());

            List active = new ArrayList(fFilters.size());
            List inactive = new ArrayList(fFilters.size());
            Iterator iterator = fFilters.iterator();

            while (iterator.hasNext()) {
                Filter filter = (Filter) iterator.next();
                String name = filter.getName();

                if (filter.isChecked()) {
                    active.add(name);
                } else {
                    inactive.add(name);
                }
            }

            String pref = serializeList((String[]) active.toArray(
                        new String[active.size()]));
            getPreferenceStore().setValue(CallersConstants.PREF_ACTIVE_FILTERS_LIST, pref);
            pref = serializeList((String[]) inactive.toArray(new String[inactive.size()]));
            getPreferenceStore().setValue(CallersConstants.PREF_INACTIVE_FILTERS_LIST,
                pref);
        }

        public void removeFilters(Object[] filters) {
            for (int i = 0; i < filters.length; i++) {
                Filter filter = (Filter) filters[i];
                fFilters.remove(filter);
            }

            fViewer.remove(filters);
            updateActions();
        }

        public void toggleFilter(Filter filter) {
            boolean newState = !filter.isChecked();
            filter.setChecked(newState);
            fViewer.setChecked(filter, newState);
        }

        /**
 * @see IStructuredContentProvider#getElements(Object)
 */
        public Object[] getElements(Object inputElement) {
            return fFilters.toArray();
        }

        /**
 * @see IContentProvider#inputChanged(Viewer, Object, Object)
 */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        /**
 * @see IContentProvider#dispose()
 */
        public void dispose() {
        }
    }
}


/**
 * Model object that represents a single entry in a filter table.
 */
class Filter {
    private String fName;
    private boolean fChecked;

    public Filter(String name, boolean checked) {
        setName(name);
        setChecked(checked);
    }

    public String getName() {
        return fName;
    }

    public void setName(String name) {
        fName = name;
    }

    public boolean isChecked() {
        return fChecked;
    }

    public void setChecked(boolean checked) {
        fChecked = checked;
    }

    public boolean equals(Object o) {
        if (o instanceof Filter) {
            Filter other = (Filter) o;

            if (getName().equals(other.getName())) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        return getName().hashCode();
    }
}


/**
 * Label provider for Filter model objects
 */
class FilterLabelProvider extends LabelProvider implements ITableLabelProvider {
    private static final Image IMG_PKG = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);

    /**
 * @see ITableLabelProvider#getColumnText(Object, int)
 */
    public String getColumnText(Object object, int column) {
        if (column == 0) {
            return ((Filter) object).getName();
        }

        return ""; //$NON-NLS-1$
    }

    /**
 * @see ILabelProvider#getText(Object)
 */
    public String getText(Object element) {
        return ((Filter) element).getName();
    }

    /**
 * @see ITableLabelProvider#getColumnImage(Object, int)
 */
    public Image getColumnImage(Object object, int column) {
        return IMG_PKG;
    }
}


class FilterViewerSorter extends WorkbenchViewerSorter {
    public int compare(Viewer viewer, Object e1, Object e2) {
        ILabelProvider lprov = (ILabelProvider) ((ContentViewer) viewer).getLabelProvider();
        String name1 = lprov.getText(e1);
        String name2 = lprov.getText(e2);

        if (name1 == null) {
            name1 = ""; //$NON-NLS-1$
        }

        if (name2 == null) {
            name2 = ""; //$NON-NLS-1$
        }

        if ((name1.length() > 0) && (name2.length() > 0)) {
            char char1 = name1.charAt(name1.length() - 1);
            char char2 = name2.charAt(name2.length() - 1);

            if ((char1 == '*') && (char1 != char2)) {
                return -1;
            }

            if ((char2 == '*') && (char2 != char1)) {
                return 1;
            }
        }

        return name1.compareTo(name2);
    }
}
