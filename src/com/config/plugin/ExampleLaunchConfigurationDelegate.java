package com.config.plugin;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class ExampleLaunchConfigurationDelegate  implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		String attribute = configuration.getAttribute(LaunchConfigurationAttribute.CONSOLE_TEXT, "goPaddle \"RUN!\"");
	    System.out.println(attribute);
		
	}

}
