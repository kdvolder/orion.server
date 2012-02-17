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
package org.eclipse.orion.server.shell;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
//import org.eclipse.jetty.websocket.WebSocket;
//import org.eclipse.jetty.websocket.WebSocketServlet;
import org.eclipse.orion.server.servlets.OrionServlet;
import org.eclipse.orion.server.shell.mvn.ServletMvnHandler;
import org.eclipse.orion.server.shell.npm.ServletNPMHandler;
import org.eclipse.orion.server.shell.process.ServletExternalCommandHandler;
import org.eclipse.orion.server.shell.roo.ServletROOHandler;
import org.eclipse.orion.server.shell.vmc.ServletVMCHandler;

public class ShellServlet extends OrionServlet {

	private static final long serialVersionUID = 1L;

	public static final String SHELL_URI = "/shellapi"; //$NON-NLS-1$

	private Map<String, ServletExternalCommandHandler> commandHandlers = new HashMap<String, ServletExternalCommandHandler>();

	public ShellServlet() {
		addServlet(new ServletNPMHandler());
		addServlet(new ServletVMCHandler());
		addServlet(new ServletROOHandler());
		addServlet(new ServletMvnHandler());
	}

	private void addServlet(ServletExternalCommandHandler servletCommandHandler) {
		commandHandlers.put(servletCommandHandler.getName(), servletCommandHandler);
	}

//	@Override
//	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
//		return null;
//	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (handleRequest(req, resp, pathInfo))
			return;
		// finally invoke super to return an error for requests we don't know how to handle
		super.doGet(req, resp);
	}

	private boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws ServletException {
		try {
			IPath path = new Path(pathInfo);
			String commandName = path.segment(0); // URI: /<commandName>/... 
			ServletExternalCommandHandler handler = commandHandlers.get(commandName);
			if (handler!=null) {
				return handler.handleRequest(req, resp.getOutputStream(), pathInfo);
			}
		} catch (IOException e) {
			throw new ServletException(e);
		}
		return false;
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
