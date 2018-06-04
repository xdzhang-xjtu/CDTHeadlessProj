package cdt.headless.proj;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import cdt.headless.proj.parser.*;

public class CDTApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Parser verveine = new Parser();

		String[] Args = {"-I /Users/zhangxiaodong10/test/cpptest", "/Users/zhangxiaodong10/test/cpptest"};
		verveine.setOptions(Args);
		System.out.print("Normal");
		if (verveine.parse()) {
			System.out.print("work");
		}
		else {
			System.out.print("Error in model creation, aborting");
		}
		return null;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
