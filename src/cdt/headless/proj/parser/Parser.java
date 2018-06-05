package cdt.headless.proj.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;


public class Parser {

	public static final String WORKSPACE_NAME = "tempWS";

	public static final String DEFAULT_PROJECT_NAME = "tempProj";

	private static final String SOURCE_ROOT_DIR = "src";

	/**
	 * Directory where the project to analyze is located
	 */
	private String userProjectDir;

	/**
	 * Default include paths for Linux
	 */
    public static final String[] LINUX_DEFAULT_INCLUDE = new String[] {
			 "/usr/include" ,
			 "/usr/local/include"
    };

    /**
     * Name of a file containing list of include dirs
     */
    protected String includeConfigFile;

	/**
	 * Temporary variable to gather include paths from the command line.
	 */
	private List<String> argIncludes;

	/**
	 * Temporary variable to gather macros defined from the command line
	 */
	private Map<String,String> argDefined;

	/**
	 * Whether this is a "windows" project.
	 * "Windows" project have file names where the case is not significant,
	 * thus AFile.c is the same as AFILE.c or aFILE.c
	 */
	private boolean windows;

	/**
	 * Eclipse CDT indexer
	 */
	private IIndex index = null;

	/**
	 * flag telling whether we need to look for all possible include dir
	 */
	private boolean autoinclude;

	/**
	 * flag telling whether we want to create a C or a C++ model.
	 * Defaults to C++ (cModel == false)
	 */
	private boolean cModel;
	
	/**
	 * Prefix to remove from file names
	 */
	protected String projectPrefix = null;

	/**
	 * Dictionary used to create all entities. Contains a Famix repository
	 */
//	private CDictionary dico;

	/**
	 * whether to add a .h extension to the includes that do not specify one (e.g. <code>#include &lt;string&gt;</code>).
	 * The idea is to help the include resolver
	 */
	private boolean forceIncludeH;

	public Parser() {
		this.argIncludes = new ArrayList<String>();
		this.argDefined = new HashMap<String,String>();
		this.forceIncludeH = false;
		this.autoinclude = false;
		this.windows = false;
		this.cModel = false;
		this.includeConfigFile = null;
		this.userProjectDir = null;
	}

	public boolean parse() {
		ICProject cproject = createEclipseProject("ecpro", "/Users/zhangxiaodong10/test/cpptest");
		if (cproject == null) {
        	// could not create the project :-(
        	return false;
        }
        projectPrefix = cproject.getLocationURI().getPath() + File.separator + (windows ? SOURCE_ROOT_DIR.toLowerCase() : SOURCE_ROOT_DIR) + File.separator;

        configIndexer(cproject);
		computeIndex(cproject);

		System.out.println("getAllFiles");
		try {
			IIndexFile[] iIndexFiles = index.getAllFiles();
			for (int i = 0; i < iIndexFiles.length; i++) {
				System.out.println(iIndexFiles[i].toString());
			}
		} catch (CoreException e) {
			
		}
		
		System.out.println("getFilesWithUnresolvedIncludes");
		
		try {
			IIndexFile[] iIndexFiles = index.getFilesWithUnresolvedIncludes();
			for (int i = 0; i < iIndexFiles.length; i++) {
				System.out.println(iIndexFiles[i].toString());
			}
		} catch (CoreException e) {
			
		}
		
		System.out.println("getInlineNamespaces");
		try {
			IScope[] iScopes = index.getInlineNamespaces();
			for (int i = 0; i < iScopes.length; i++) {
				System.out.println(iScopes[i].toString());
			}
		} catch (CoreException e) {
			// TODO: handle exception
		}
		
		
		try {
			cproject.accept(new MyVisitor());
		} catch (CoreException e) {
			// TODO: handle exception
		}
		
        return true;
	}


	private void configWorkspace(IWorkspace workspace) {
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		workspaceDesc.setAutoBuilding(false); // we do not want the workspace to rebuild the project every time a new resource is added
		try {
			workspace.setDescription(workspaceDesc);
		} catch (CoreException exc) {
			System.out.println("Error trying to set workspace description: " + exc.getMessage());
		}

	}

	public ICProject createEclipseProject(String projName, String sourcePath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		configWorkspace(workspace);
		IWorkspaceRoot root = workspace.getRoot();
		// we make a directory at the workspace root to copy source files
		IPath eclipseProjPath = root.getRawLocation().removeLastSegments(1).append(WORKSPACE_NAME).append(projName);
		eclipseProjPath.toFile().mkdirs();
		//
		System.out.println(eclipseProjPath.toString());

		final IProject project = root.getProject(projName);
		try {
			// delete content if the project exists
			if (project.exists()) {
				project.delete(/*deleteContent*/true, /*force*/true, Constants.NULL_PROGRESS_MONITOR);
				project.refreshLocal(IResource.DEPTH_INFINITE, Constants.NULL_PROGRESS_MONITOR);
			}
		} catch (Exception exc) {
			System.out.println("Exception is here 1");
//			exc.printStackTrace();
		}

		IProjectDescription eclipseProjDesc = workspace.newProjectDescription(project.getName());
		eclipseProjDesc.setLocation(eclipseProjPath);

		try {
			project.create(eclipseProjDesc, Constants.NULL_PROGRESS_MONITOR);
			project.open(Constants.NULL_PROGRESS_MONITOR);
		} catch (CoreException e1) {
			System.out.println("Exception is here 2");
//			e1.printStackTrace();
		}

		try {
			// now we make it a C project
			CCorePlugin.getDefault().createCProject(eclipseProjDesc, project, Constants.NULL_PROGRESS_MONITOR, project.getName());
			if (!project.isOpen()) {
				project.open(Constants.NULL_PROGRESS_MONITOR);
			}
		} catch (Exception exc) {
			System.out.println("Exception is here 3");
//			exc.printStackTrace();
		}

		ICProjectDescription cProjectDesc = CoreModel.getDefault().getProjectDescription(project, true);
		cProjectDesc.setCdtProjectCreated();

		File projSrc = new File(sourcePath);
		if (! projSrc.exists()) {
			return null;
		}
		FileUtil.copySourceFilesInProject(project, SOURCE_ROOT_DIR, projSrc, /*toLowerCase*/windows, /*addHExtension*/forceIncludeH);
		ICProjectDescriptionManager descManager = CoreModel.getDefault().getProjectDescriptionManager();
        try {
			descManager.updateProjectDescriptions(new IProject[] { project }, Constants.NULL_PROGRESS_MONITOR);
		} catch (CoreException e) {
			System.out.println("Exception is here 4");
//			e.printStackTrace();
		}

        return CoreModel.getDefault().getCModel().getCProject(project.getName());
	}

	/**
	 * sets include path (system, given by user) and macros into the project
	 */
	private void configIndexer(ICProject proj) {
		IPath projPath = proj.getPath();
		List<String> includeFromConf=new ArrayList<>();
		IPathEntry[] oldEntries=null;
		try {			
			oldEntries = proj.getRawPathEntries();
		} catch (CModelException e) {
			e.printStackTrace();
			return;
		}

		if (includeConfigFile != null) {
			readIncludeConf(includeConfigFile, includeFromConf);
		}
		
		IPathEntry[] newEntries = new IPathEntry[
		                                         oldEntries.length +
		                                         LINUX_DEFAULT_INCLUDE.length +
		                                         argIncludes.size() +
		                                         includeFromConf.size() +
		                                         argDefined.size()];
		int i;

		/* include paths */
		for (i=0; i < oldEntries.length; i++) {
			newEntries[i] = oldEntries[i];
		}
		/* include paths */
		for (String path : LINUX_DEFAULT_INCLUDE) {
			newEntries[i] = CoreModel.newIncludeEntry(projPath, null, new Path(path), /*isSystemInclude*/true);
			i++;
		}
		/* include paths */
		for (String path : argIncludes) {
			newEntries[i] = CoreModel.newIncludeEntry(projPath, null, new Path(path), /*isSystemInclude*/false);
			i++;
		}
		/* include paths */
		for (String path : includeFromConf) {
			newEntries[i] = CoreModel.newIncludeEntry(projPath, null, new Path(path), /*isSystemInclude*/false);
			i++;
		}
		/* macros  defined */
		for (Map.Entry<String, String> macro : argDefined.entrySet()) {
			newEntries[i] = CoreModel.newMacroEntry(projPath, macro.getKey(), macro.getValue());
		}

		try {			
			proj.setRawPathEntries(newEntries, Constants.NULL_PROGRESS_MONITOR);

		} catch (CModelException e) {
			e.printStackTrace();
		}
	}

	private void readIncludeConf(String confFileName, List<String> lines) {
		BufferedReader read = null;
		try {
			read = new BufferedReader( new FileReader(confFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (read != null) {
			String line;
			try {
				while ( (line=read.readLine()) != null ) {
					lines.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
				lines = new ArrayList<>();
			}
		}

		try {
			read.close();
		} catch (IOException e) {
			// ignore
		}
	}

	private void computeIndex(ICProject cproject) {
		IIndexManager imanager = CCorePlugin.getIndexManager();
		imanager.setIndexerId(cproject, "org.eclipse.cdt.core.fastIndexer");
        imanager.reindex(cproject);
        imanager.joinIndexer(IIndexManager.FOREVER, Constants.NULL_PROGRESS_MONITOR );
		try {
			this.index = imanager.getIndex(cproject);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}


	public void setOptions(String[] args) {
		int i = 0;
		while (i < args.length && args[i].trim().startsWith("-")) {
		    String arg = args[i++].trim();

			if (arg.equals("-h")) {
			}
			else if (arg.startsWith("-c")) {
				cModel = true;
			}
			else if (arg.equals("-autoinclude")) {
				autoinclude = true;
			}
			else if (arg.equals("-includeconf")) {
				includeConfigFile = args[i++].trim();
			}
			else if (arg.equals("-forceincludeH")) {
				forceIncludeH = true;
			}
			else if (arg.startsWith("-I")) {
				argIncludes.add(arg.substring(2));
			}
			else if (arg.startsWith("-D")) {
				parseMacroDefinition(arg);
			}
			else if (arg.equals("-windows")) {
				windows = true;
			}
			else {
				System.out.println("** Unrecognized option: " + arg);
			}
		}

		for ( ; i < args.length; i++) {
			userProjectDir = args[i];
			
			if (autoinclude) {
				for (String inc : FileUtil.gatherIncludeDirs(args[i])) {
					argIncludes.add(inc);					
				}
			}
		}
	}

	private void modelComment(String title, Iterable<String> values) {
		/* TODO deactivated for now:
		String cmt = title; 
		for (String v : values) {
			cmt += " " + v;
		}
		dico.createFamixComment(cmt);
		 */
	}

	private void parseMacroDefinition(String arg) {
		int i;
		String macro;
		String value;

		i = arg.indexOf('=');
		if (i < 0) {
			macro=arg.substring(2);  // remove '-D' at the beginning
			value = "";
		}
		else {
			macro = arg.substring(2, i);
			value = arg.substring(i+1);
		}
		argDefined.put(macro, value);
	}

}
