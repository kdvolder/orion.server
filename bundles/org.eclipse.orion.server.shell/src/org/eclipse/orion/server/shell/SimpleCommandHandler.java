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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.orion.server.shell.process.ExternalCommand;
import org.eclipse.orion.server.shell.process.ExternalProcess;
import org.eclipse.orion.server.shell.process.ICommandContext;
import org.eclipse.orion.server.shell.process.ServletExternalCommandHandler;
import org.eclipse.orion.server.shell.process.ServletExternalCommandHandler.CommandHandler;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class SimpleCommandHandler extends CommandHandler {

	public SimpleCommandHandler(String commandPath, ServletExternalCommandHandler owner) {
		super(commandPath, owner);
	}

	@Override
	protected boolean exec(HttpServletRequest request, JSONObject arguments, ICommandContext context, OutputStream out)
			throws ServletException {
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

	protected abstract ExternalCommand createCommand(JSONObject arguments) throws JSONException;

}
