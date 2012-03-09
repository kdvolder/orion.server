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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("restriction")
public class ServletExternalCommandHandler {

	private String name;
	private Map<String, CommandHandler> commandHandlers = new HashMap<String, CommandHandler>();

	public ServletExternalCommandHandler(String name) {
		this.name = name;
	}

	public static class DefaultCommandHandler extends CommandHandler {
		public DefaultCommandHandler(String name,
				ServletExternalCommandHandler owner) {
			super(name, owner);
		}
	}

	public static abstract class CommandHandler {
		private String name;
		private String ownerName;

		public String getName() {
			return name;
		}

		public CommandHandler(String name, ServletExternalCommandHandler owner) {
			this.ownerName = owner.name;
			this.name = name;
			owner.put(name, this);
		}

		@Override
		public String toString() {
			return "CommandHandler("+ownerName+"/" + name + ")";
		}

		/**
		 * Default implementation of a command. It just shows some information
		 * about the command but doesn't actually do anything. Subclass is
		 * supposed to override to implement command.
		 * 
		 * @param request
		 * @param arguments
		 *            whatever information got created by gcli, representing the
		 *            arguments of the command.
		 * @param file
		 *            working directory. This is a url as used by the orion file
		 *            Client.
		 * @param out
		 *            writing onto out will send the result of the command to
		 *            the client.
		 * @return true if the command was handled succesfully by the handler
		 * @throws ServletException
		 *             if something went wrong.
		 */
		protected boolean exec(HttpServletRequest request,
				JSONObject arguments, ICommandContext context, OutputStream _out)
				throws ServletException {
			PrintWriter out = new PrintWriter(_out);
			out.println("You were trying to execute an " + ownerName
					+ " command");
			out.println("  command   = " + name);
			out.println("  user.home = " + context.getHomeDir());
			out.println("  workingDir= " + context.getWorkingDir());
			out.println("  arguments = " + arguments);
			out.flush();
			return true;
		}
	}

	public boolean handleRequest(HttpServletRequest request, OutputStream out,
			String pathStr) throws ServletException {
		try {
			IPath path = new Path(pathStr); // path: /npm/<command>/???more
											// stuff???
			Assert.isTrue(path.segment(0).equals(name));
			Assert.isTrue(path.segmentCount() >= 2);
			String command = path.removeFirstSegments(1).toString();
			JSONObject arguments = new JSONObject(
					request.getParameter("arguments"));
			CommandHandler commandHandler = commandHandlers.get(command);
			CommandContext context = new CommandContext(request);
			if (commandHandler != null && context.getHomeDir()!=null && context.getWorkingDir()!=null) {
				// For now we will assume that command handlers require file (to
				// determine the working directory where the command should
				// execute).
				return commandHandler.exec(request, arguments, context, out);
			} else {
				// If we don't have a handler or a location to execute at...
				// We do something by default... typically just dump some info
				// for diagnostic purposes.
				return new DefaultCommandHandler(command, this).exec(request,
						arguments, context, out);
			}
		} catch (JSONException e) {
			e.printStackTrace(new PrintStream(out));
			throw new ServletException(e);
		} catch (CoreException e) {
			e.printStackTrace(new PrintStream(out));
			throw new ServletException(e);
		} catch (ServletException e) {
			Throwable cause = e.getCause();
			if (cause == null) {
				cause = e;
			}
			cause.printStackTrace(new PrintStream(out));
			throw e;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// We tried nothing else we can do...
				}
			}
		}
	}

	public void put(String name, CommandHandler commandHandler) {
		this.commandHandlers.put(name, commandHandler);
	}

	public String getName() {
		return name;
	}
}
