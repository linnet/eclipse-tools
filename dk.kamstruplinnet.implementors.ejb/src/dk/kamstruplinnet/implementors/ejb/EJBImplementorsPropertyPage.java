package dk.kamstruplinnet.implementors.ejb;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * @author jl
 */
public class EJBImplementorsPropertyPage extends PropertyPage {
    private final class EjbJarXmlVisitor implements IResourceVisitor {
        private Collection mExistingEjbJarXmlFiles;
        private Collection mEjbJarXmlFiles= new ArrayList();
        public EjbJarXmlVisitor(Collection existingEjbJarFiles) {
            mExistingEjbJarXmlFiles= existingEjbJarFiles;
        }

        public boolean visit(IResource resource) {
            if (resource.getType() == IResource.FILE) {
                if (isEjbJarXmlFile(resource.getProjectRelativePath()) && !mExistingEjbJarXmlFiles.contains(resource)) {
                    mEjbJarXmlFiles.add(resource);
                }
            }
            return true;
        }
        
        public Collection getEjbJarXmlFiles() {
            return mEjbJarXmlFiles;
        }
    }

    private final class FileListLabelProvider implements ILabelProvider {
        public Image getImage(Object element) {
            if (!(element instanceof IResource)) {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
            }
            return null;
        }

        public String getText(Object element) {
            if (element instanceof String) {
                return EJBImplementorsMessages.getFormattedString("EJBImplementorsPropertyPage.non_existent", element); //$NON-NLS-1$
            } else if (element instanceof IResource) {
                IResource resource = (IResource) element;
                String displayPath = resource.getFullPath().toString().substring(1);
                if (resource.exists()) {
                    return displayPath;
                } else {
                    return EJBImplementorsMessages.getFormattedString("EJBImplementorsPropertyPage.non_existent", displayPath); //$NON-NLS-1$
                }
            }
            return EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.unknown_class"); //$NON-NLS-1$
        }

        public void addListener(ILabelProviderListener listener) {
            // Do nothing...
        }

        public void dispose() {
            // Do nothing...
        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {
            // Do nothing...
        }
    }

    private final class FileListContentProvider implements IStructuredContentProvider {
        public void dispose() {
            // Do nothing...
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Do nothing...
        }

        public Object[] getElements(Object inputElement) {
            return ((Collection) inputElement).toArray(new Object[((Collection) inputElement).size()]);
        }
    }

    /**
     * Viewer filter for archive selection dialogs.
     * Archives are files with file extension 'jar' and 'zip'.
     * The filter is not case sensitive.
     */
    public class EjbJarXmlFileFilter extends ViewerFilter {

        private List fExcludes;
        private boolean fRecursive;
    
        /**
         * @param excludedFiles Excluded files will not pass the filter.
         * <code>null</code> is allowed if no files should be excluded. 
         * @param recusive Folders are only shown if, searched recursivly, contain
         * an archive
         */
        public EjbJarXmlFileFilter(IFile[] excludedFiles, boolean recusive) {
            if (excludedFiles != null) {
                fExcludes= Arrays.asList(excludedFiles);
            } else {
                fExcludes= null;
            }
            fRecursive= recusive;
        }
    
        /*
         * @see ViewerFilter#select
         */
        public boolean select(Viewer viewer, Object parent, Object element) {
            if (element instanceof IFile) {
                if (fExcludes != null && fExcludes.contains(element)) {
                    return false;
                }
                return isEjbJarXmlFile(((IFile)element).getFullPath());
            } else if (element instanceof IContainer) { // IProject, IFolder
                if (!fRecursive) {
                    return true;
                }
                try {
                    IResource[] resources= ((IContainer)element).members();
                    for (int i= 0; i < resources.length; i++) {
                        // recursive! Only show containers that contain an archive
                        if (select(viewer, parent, resources[i])) {
                            return true;
                        }
                    }
                } catch (CoreException e) {
                    EJBImplementorsPlugin.log(e);
                }               
            }
            return false;
        }
    }    

    private boolean mDirty;
    private Collection mPaths;
    private Button mEnabled;
    private ListViewer mListViewer;
    private EJBProjectProperties mProperties;

    private Composite fListComposite;
    private Button fAddResourceButton;
    private Button fRemoveResourceButton;
    private Button fAutoFindButton;

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        initializeDialogUnits(parent);

        mProperties = new EJBProjectProperties(getElementResource());

        Composite superComposite = new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns = 1;
        superComposite.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        superComposite.setLayoutData(gd);
        
        createTopLevel(superComposite);
        
        fListComposite= createTopLevelComposite(superComposite);
        
        createList(fListComposite);
        createButtonPanel(fListComposite);
        setListAndButtonsEnabled();

        setDirty(false);
        return superComposite;
    }

    private void createTopLevel(Composite superComposite) {
        Composite composite= new Composite(superComposite, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(2, false));
        
        createEnableButton(composite);
        createPreferenceButton(composite);
    }

    private void createEnableButton(Composite composite) {
        mPaths = mProperties.getPaths();

        mEnabled = new Button(composite, SWT.CHECK);
        mEnabled.setText(EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.enabled.label")); //$NON-NLS-1$
        mEnabled.setSelection(mProperties.isEJBEnabled());
        mEnabled.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                setListAndButtonsEnabled();
            }
        });
        
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        mEnabled.setLayoutData(gd);
    }

    /**
     * @param superComposite
     */
    private void createPreferenceButton(Composite composite) {
        Button prefsButton= new Button(composite, SWT.NONE);
        prefsButton.setText(EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.preferences.label")); //$NON-NLS-1$
        prefsButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                openPreferencesDialog();
            }
        });

        GridData gd= new GridData();
        gd.horizontalAlignment= GridData.HORIZONTAL_ALIGN_END;
        gd.verticalAlignment= GridData.VERTICAL_ALIGN_CENTER;
        prefsButton.setLayoutData(gd);
    }

    /**
     * 
     */
    protected void openPreferencesDialog() {
        EJBImplementorsPreferencesDialog dialog = new EJBImplementorsPreferencesDialog(getShell());
        dialog.open();
    }

    private IProject getProject() {
        return (IProject) getElement().getAdapter(IProject.class);
    }

    private IJavaProject getJavaProject() {
        IJavaProject javaProject = (IJavaProject) getElement().getAdapter(IJavaProject.class);
        if (javaProject != null) {
            return javaProject;
        }
        return JavaCore.create(getProject());
    }

    private Composite createTopLevelComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 4;
        composite.setLayout(layout);
        
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        composite.setLayoutData(gd);

        return composite;
    }

    protected void setListAndButtonsEnabled() {
        boolean enabled = mEnabled.getSelection();
        boolean elementsSelected = !mListViewer.getSelection().isEmpty();
        mListViewer.getControl().setEnabled(enabled);
        
        fAddResourceButton.setEnabled(enabled);
        fRemoveResourceButton.setEnabled(enabled && elementsSelected);
        fAutoFindButton.setEnabled(enabled);
    }
    
    private void createList(Composite composite) {
        mPaths = mProperties.getPaths();

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;

        mListViewer = new ListViewer(composite, SWT.BORDER | SWT.MULTI);
        mListViewer.setContentProvider(new FileListContentProvider());
        mListViewer.setLabelProvider(new FileListLabelProvider());
        mListViewer.setInput(mPaths);
        mListViewer.getControl().setLayoutData(gd);
        mListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            /* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
                setListAndButtonsEnabled();
			}
        });
    }

    private void createButtonPanel(Composite composite) {
        Composite buttonPanel = new Composite(composite, SWT.NONE);
        FillLayout layout= new FillLayout(SWT.VERTICAL);
        buttonPanel.setLayout(layout);

        fAddResourceButton= new Button(buttonPanel, SWT.NONE);
        fAddResourceButton.setText(EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.add.label")); //$NON-NLS-1$
        fAddResourceButton.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    addResource();
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                    // Do nothing...
                }
            });

        fRemoveResourceButton= new Button(buttonPanel, SWT.NONE);
        fRemoveResourceButton.setText(EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.remove.label")); //$NON-NLS-1$
        fRemoveResourceButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    removeSelectedElements();
                }
            });
            
        fAutoFindButton= new Button(buttonPanel, SWT.NONE);
        fAutoFindButton.setText(EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.autofind.label")); //$NON-NLS-1$
        fAutoFindButton.setToolTipText(EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.autofind.tooltip")); //$NON-NLS-1$
        fAutoFindButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                autoAddResources();                
            }
        });
    }

    /**
     * 
     */
    protected void autoAddResources() {
        try {
            EjbJarXmlVisitor visitor= new EjbJarXmlVisitor(mPaths);
            getElementResource().accept(visitor);
            Collection ejbJarXmlFiles = visitor.getEjbJarXmlFiles();
            if (ejbJarXmlFiles.size() > 0) {
            	mPaths.addAll(ejbJarXmlFiles);
            	setDirty(true);
            	mListViewer.refresh();
            }
        } catch (CoreException e) {
            EJBImplementorsPlugin.log(e);
        }
    }

    /**
     *
     */
    protected void addResource() {
        SelectionStatusDialog dialog = createResourceSelectionDialog();

        if (dialog.open() == IDialogConstants.CANCEL_ID) {
            return;
        }

        Object[] selectedResources = dialog.getResult();
        setDirty(true);

        for (int i = 0; i < selectedResources.length; i++) {
            IResource resource = (IResource) selectedResources[i];
            mPaths.add(resource);
        }

        mListViewer.refresh();
    }

    private SelectionStatusDialog createResourceSelectionDialog() {
        Class[] acceptedClasses= new Class[] { IFile.class };
        TypedElementSelectionValidator validator= new TypedElementSelectionValidator(acceptedClasses, true);
        ViewerFilter filter= new EjbJarXmlFileFilter(getExistingDescriptorFiles(), true);
        
        ILabelProvider lp= new WorkbenchLabelProvider();
        ITreeContentProvider cp= new WorkbenchContentProvider();

        String title= EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.add.dialog.title"); //$NON-NLS-1$
        String message= EJBImplementorsMessages.getString("EJBImplementorsPropertyPage.add.dialog.message"); //$NON-NLS-1$

        ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), lp, cp);
        dialog.setValidator(validator);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.addFilter(filter);
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
        dialog.setInitialSelection(getProject());     
        
        return dialog;
    }
    
    /**
     * @return
     */
    private IFile[] getExistingDescriptorFiles() {
        Collection files = new ArrayList();
        
        for (Iterator iter = mPaths.iterator(); iter.hasNext();) {
            Object element = iter.next();
            
            if (element instanceof IFile) {
                files.add(element);
            }
        }
        return (IFile[]) files.toArray(new IFile[files.size()]);
    }

    /**
     *
     */
    protected void removeSelectedElements() {
        IStructuredSelection selection = ((IStructuredSelection) mListViewer.getSelection());

        if (!selection.isEmpty()) {
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                Object pathOrResource = iter.next();
                mPaths.remove(pathOrResource);
            }

            setDirty(true);
            mListViewer.refresh();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        if (isDirty()) {
            mProperties.saveFilePaths(mPaths);
    
            EJBImplementorsPlugin.getInstance().getProjectMappings(getJavaProject()).updateMappings();
        }
        mProperties.setEJBEnabled(mEnabled.getSelection());
        return true;
    }

    /**
     * Returns true if the properties handled by this dialog have been changed so that they
     * should be saved.
     * 
     * @return True if the properties have been modified.
     */
    private boolean isDirty() {
        return mDirty;
    }

    /**
     * Sets the dirty flag.
     * 
     * @param b
     */
    private void setDirty(boolean dirty) {
        this.mDirty = dirty;
    }

    private IResource getElementResource() {
        return (IResource) getElement().getAdapter(IResource.class);
    }
    
    /**
     * @param path
     * @return
     */
    protected boolean isEjbJarXmlFile(IPath path) {
        StringMatcher mask= new StringMatcher(EJBImplementorsPlugin.getInstance().getDeploymentDescriptorMask(), true, false);
        return mask.match(path.lastSegment());
    }
    
}
