/*******************************************************************************
 * Copyright (c) 2012 VMWare and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.shell.process;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.orion.internal.server.servlets.file.NewFileServlet;

@SuppressWarnings("restriction")
public class CommandContext implements ICommandContext {
	
	private File workingDir;
	private File homeDir;

	public CommandContext(HttpServletRequest request) throws CoreException {
		workingDir = getFile(request, "location");
		homeDir = getFile(request, "user.home");
	}

	@Override
	public File getWorkingDir() {
		return workingDir;
	}

	@Override
	public File getHomeDir() {
		return homeDir;
	}

	/**
	 * Gets a request parameter as a local File reference. The assumption is that the parameter is set by the requestor
	 * to a orion file service URL and that this file service URL corresponds to a local file or directory.
	 * <p>
	 * If the parameter is not set or if it doesn't correspond to a local file, then this method will return null.
	 */
	public static File getFile(HttpServletRequest request, String paramName) throws CoreException {
		String location = request.getParameter(paramName);
		if (location!=null) {
			IPath filePath = new Path(location).removeFirstSegments(1); // first part of this path is typically 'file' as it designates the 'file' service. 
			IFileStore fileStore = NewFileServlet.getFileStore(filePath);
			File file = fileStore.toLocalFile(EFS.NONE, null);
			return file;
		} 
		return null;
	}
	
}
