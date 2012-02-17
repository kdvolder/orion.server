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
package org.eclipse.orion.server.shell.roo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.runtime.Assert;
import org.eclipse.orion.server.shell.process.ExternalCommand;
import org.eclipse.orion.server.shell.process.ExternalProcess;
import org.eclipse.orion.server.shell.process.ICommandContext;
import org.eclipse.orion.server.shell.process.ServletExternalCommandHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class ServletROOHandler extends ServletExternalCommandHandler {
	
	protected static final String ROO_COMMAND = "/opt/spring-roo-1.2.1.RELEASE/bin/roo.sh";

	public ServletROOHandler() {
		super("roo");
		new CommandHandler("script", this) {
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
				cmdLine.add(ROO_COMMAND);
				cmdLine.add("script");
				String scriptName = arguments.getString("script");
				Assert.isNotNull(scriptName, "The name of a script must be provided");
				cmdLine.add(scriptName);
				return new ExternalCommand(cmdLine.toArray(new String[cmdLine.size()]));
			}
		};	
	}
	
}
