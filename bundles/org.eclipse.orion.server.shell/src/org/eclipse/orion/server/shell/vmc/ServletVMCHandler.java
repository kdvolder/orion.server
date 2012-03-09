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
package org.eclipse.orion.server.shell.vmc;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.orion.server.shell.JSONUtil;
import org.eclipse.orion.server.shell.process.ExternalCommand;
import org.eclipse.orion.server.shell.process.ExternalProcess;
import org.eclipse.orion.server.shell.process.ICommandContext;
import org.eclipse.orion.server.shell.process.ServletExternalCommandHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class ServletVMCHandler extends ServletExternalCommandHandler {

	/**
	 * Simple template implementation for a basic VMC command that expects as 
	 * single 'app-name' argument. Examples of such commands: 'start', 'stop', 'restart', ...
	 */
	private static class SimpleVMCCommandHandler extends CommandHandler {

		public SimpleVMCCommandHandler(String name,
				ServletExternalCommandHandler owner) {
			super(name, owner);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected boolean exec(HttpServletRequest request, JSONObject arguments, ICommandContext context, OutputStream out) throws ServletException {
			try {
				ExternalProcess process = new ExternalProcess(context, createCommand(request, arguments), out, out);
				return true;
			} catch (JSONException e) {
				throw new ServletException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new ServletException(e);
			} catch (InterruptedException e) {
				throw new ServletException(e);
			} catch (CoreException e) {
				throw new ServletException(e);
			}
		}

		private ExternalCommand createCommand(HttpServletRequest request, JSONObject arguments) throws JSONException, CoreException, IOException {
			List<String> cmdLine = new ArrayList<String>();
			cmdLine.add("vmc");
			cmdLine.add(getName());
			defaultOptions(cmdLine);
			String appName = arguments.getString("app-name");
			Assert.isNotNull(appName, "command "+getName()+" requires an app-name parameter");
			cmdLine.add(appName);
			ExternalCommand cmd = new ExternalCommand(cmdLine);
			return cmd;
		}
	}

	private static final String[] SIMPLE_COMMANDS = {
		"start", "stop", "restart", "delete"
	};
	
	private ExternalCommand createTargetCommand(JSONObject arguments) throws JSONException {
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add("vmc");
		cmdLine.add("target");
		defaultOptions(cmdLine);
		if (arguments.has("target")) {
			cmdLine.add(arguments.getString("target"));
		}
		return new ExternalCommand(cmdLine);
	}
	
	private static void defaultOptions(List<String> cmdLine) {
		cmdLine.add("-n");
	}

	public ServletVMCHandler() {
		super("vmc");
		for (String name : SIMPLE_COMMANDS) {
			new SimpleVMCCommandHandler(name, this);
		}
		
		new CommandHandler("get-target", this) {
			@Override
			protected boolean exec(HttpServletRequest request, JSONObject arguments, ICommandContext context, OutputStream out) throws ServletException {
				try {
					ExternalProcess process = new ExternalProcess(context, createTargetCommand(arguments), out, out);
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
		};
		new CommandHandler("set-target", this) {
			@Override
			protected boolean exec(HttpServletRequest request, JSONObject arguments, ICommandContext context, OutputStream out) throws ServletException {
				try {
					ExternalProcess process = new ExternalProcess(context, createTargetCommand(arguments), out, out);
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
		};
		new CommandHandler("login", this) {
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
				cmdLine.add("vmc");
				cmdLine.add("login");
				defaultOptions(cmdLine);
				String email = arguments.getString("email");
				String passwd = arguments.getString("passwd");
				Assert.isLegal(email!=null && passwd!=null);
				cmdLine.add("--email");
				cmdLine.add(email);
				cmdLine.add("--passwd");
				cmdLine.add(passwd);
				return new ExternalCommand(cmdLine);
			}
		};
		
		new CommandHandler("apps", this) {
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
				cmdLine.add("vmc");
				cmdLine.add("apps");
				defaultOptions(cmdLine);
				return new ExternalCommand(cmdLine);
			}
		};
		
		
		new CommandHandler("push", this) {
			
			@Override
			protected boolean exec(HttpServletRequest request, JSONObject arguments, ICommandContext context, OutputStream out) throws ServletException {
				try {
					ExternalProcess process = new ExternalProcess(context, createCommand(context, arguments), out, out);
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

			private ExternalCommand createCommand(ICommandContext context, JSONObject arguments) throws JSONException {
				return createPushNodeAppCommand(context.getWorkingDir(), arguments);
//				if (isNodeApp(workingDir)) {
//					return createPushNodeAppCommand(workingDir, arguments);
//				} else { 
//					File warFile = getWarFile(workingDir);
//					if (warFile!=null) {
//						return createPushWarFileCommand(workingDir, warFile, arguments);
//					}
//				}
			}

//			private ExternalCommand createPushWarFileCommand(File workingDir, File warFile, JSONObject arguments) {
//				List<String> cmdLine = new ArrayList<String>();
//				cmdLine.add("vmc");
//				cmdLine.add("push");
//				defaultOptions(cmdLine);
//				
//			}

			private File getWarFile(File workingDir) {
				File targetDir = new File(workingDir, "target");
				if (targetDir.isDirectory()) {
					File[] warFiles = targetDir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".war");
						}
					});
					if (warFiles!=null && warFiles.length==1) {
						return warFiles[0];
					}
				}
				return null;
			}

			private boolean isNodeApp(File workingDir) {
				return new File(workingDir, "package.json").exists()
					|| new File(workingDir, "node_mdules").exists(); 
			}

			private ExternalCommand createPushNodeAppCommand(File workingDir, JSONObject arguments) throws JSONException {
				// Sample arguments = {"appname":"cloudy","mem":null,"instances":null,"url":null,"no-start":false}
				List<String> cmdLine = new ArrayList<String>();
				cmdLine.add("vmc");
				cmdLine.add("push");
				defaultOptions(cmdLine);
				cmdLine.add("--path");
				cmdLine.add(workingDir.getAbsolutePath());
				String appName = arguments.getString("appname");
				String mem = JSONUtil.getString(arguments, "mem");
				String instances = JSONUtil.getString(arguments, "instances");
				String url = JSONUtil.getString(arguments, "url");
				boolean noStart = JSONUtil.getBoolean(arguments, "no-start");
				Assert.isLegal(appName!=null);
				cmdLine.add(appName);
				addOpt(cmdLine, "--mem", mem);
				addOpt(cmdLine, "--url", url);
				addOpt(cmdLine, "--no-start", noStart);
				addOpt(cmdLine, "--instances", instances);
				return new ExternalCommand(cmdLine);
			}

			private void addOpt(List<String> cmdLine, String key, boolean val)  {
				if (val) {
					cmdLine.add(key);
				}
			}

			private void addOpt(List<String> cmdLine, String key, String val) {
				if (val!=null) {
					cmdLine.add(key);
					cmdLine.add(val.toString());
				}
			}
		};
		
	}

	
}
