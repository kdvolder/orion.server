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
package org.eclipse.orion.server.shell.mvn;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.orion.server.shell.process.ExternalCommand;
import org.eclipse.orion.server.shell.process.ExternalProcess;
import org.eclipse.orion.server.shell.process.ICommandContext;
import org.eclipse.orion.server.shell.process.ServletExternalCommandHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class ServletMvnHandler extends ServletExternalCommandHandler {
	
	protected static final String COMMAND = "mvn";
	
	public static class SimpleMvnCommandHandler extends CommandHandler {
		public SimpleMvnCommandHandler(String name, ServletExternalCommandHandler owner) {
			super(name, owner);
		}

		@Override
		protected boolean exec(HttpServletRequest request, JSONObject arguments, ICommandContext context, OutputStream out) throws ServletException {
			try {
				ExternalProcess process = new ExternalProcess(context, createCommand(arguments), out, out);
				return true;
			} catch (JSONException e) {
				throw new ServletException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new ServletException(e);
			} catch (InterruptedException e) {
				throw new ServletException(e);
			}
		}

		private ExternalCommand createCommand(JSONObject arguments) throws JSONException {
			List<String> cmdLine = new ArrayList<String>();
			cmdLine.add(COMMAND);
			cmdLine.add(getName());
			return new ExternalCommand(cmdLine.toArray(new String[cmdLine.size()]));
		}
	}

	private static final String[] SIMPLE_COMMANDS = {
		"assemble", "build", "compile", "package", "test" 
	};

	public ServletMvnHandler() {
		super("mvn");
		for (String name : SIMPLE_COMMANDS) {
			new SimpleMvnCommandHandler(name, this);
		}
	}
	
}
