/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.git.servlets;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.core.runtime.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.orion.internal.server.servlets.ServletResourceHandler;
import org.eclipse.orion.server.core.ServerStatus;
import org.eclipse.orion.server.git.GitConstants;
import org.eclipse.orion.server.git.cli.CLIGitCommand;
import org.eclipse.orion.server.servlets.OrionServlet;
import org.eclipse.osgi.util.NLS;
import org.json.*;
import org.kohsuke.args4j.CmdLineException;

public class GitCommandHandlerV1 extends ServletResourceHandler<String> {
	private ServletResourceHandler<IStatus> statusHandler;

	public GitCommandHandlerV1(ServletResourceHandler<IStatus> statusHandler) {
		this.statusHandler = statusHandler;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public boolean handleRequest(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException {
		try {
			switch (getMethod(request)) {
				case POST :
					return handlePost(request, response, path);
			}
		} catch (Exception e) {
			String msg = NLS.bind("Failed to handle 'command' request for {0}", path);
			return statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, e));
		}
		return false;
	}

	private boolean handlePost(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException {
		IPath p = new Path(path);
		try {
			File gitDir = GitUtils.getGitDir(p);
			Repository db = new FileRepository(gitDir);
			String args[] = readArgs(request);
			String result = CLIGitCommand.execute(args, db);
			writeTextResponse(request, response, result);
			return true;
		} catch (CmdLineException e) {
			return statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_BAD_REQUEST, NLS.bind("The command cannot be executed: {0}", e.getMessage()), e));
		} catch (Exception e) {
			return statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occured when procesing command.", e));
		}
	}

	private String[] readArgs(final HttpServletRequest request) throws IOException, JSONException {
		JSONObject commandArgs = OrionServlet.readJSONRequest(request);
		JSONArray jsonArray = commandArgs.getJSONArray(GitConstants.KEY_COMMAND_ARGS);
		String[] args = new String[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			args[i] = jsonArray.getString(i);
		}
		return args;
	}

	private static void writeTextResponse(HttpServletRequest req, HttpServletResponse resp, String result) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
		resp.setHeader("Cache-Control", "no-store"); //$NON-NLS-1$ //$NON-NLS-2$
		//TODO look at accept header and chose appropriate response representation
		resp.setContentType("text/html;charset=UTF-8" /* ProtocolConstants.CONTENT_TYPE_TEXT */); //$NON-NLS-1$
		resp.getWriter().print(result);
	}

}
