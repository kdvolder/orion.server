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
package org.eclipse.orion.server.shell.npm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.orion.server.shell.JSONUtil;
import org.eclipse.orion.server.shell.SimpleCommandHandler;
import org.eclipse.orion.server.shell.process.ExternalCommand;
import org.eclipse.orion.server.shell.process.ServletExternalCommandHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServletNPMHandler extends ServletExternalCommandHandler {
	
	public ServletNPMHandler() {
		super("npm");
		new SimpleCommandHandler("install", this) {
			@Override
			protected ExternalCommand createCommand(JSONObject arguments) throws JSONException {
				List<String> cmdLine = new ArrayList<String>();
				cmdLine.add("npm");
				cmdLine.add("install");
				if (JSONUtil.getBoolean(arguments, "force")) {
					cmdLine.add("--force");
				}
				JSONArray packages = arguments.getJSONArray("packages");
				int length = packages.length();
				for (int i = 0; i < length; i++) {
					cmdLine.add(packages.getString(i));
				}
				return new ExternalCommand(cmdLine.toArray(new String[cmdLine.size()]));
			}
		};
		new SimpleCommandHandler("config/list", this) {
			@Override
			protected ExternalCommand createCommand(JSONObject arguments) throws JSONException {
				List<String> cmdLine = new ArrayList<String>();
				cmdLine.add("npm");
				cmdLine.add("config");
				cmdLine.add("list");
				if (JSONUtil.getBoolean(arguments, "long")) {
					cmdLine.add("--long");
				}
				return new ExternalCommand(cmdLine.toArray(new String[cmdLine.size()]));
			}
		};
		new SimpleCommandHandler("config/get", this) {
			@Override
			protected ExternalCommand createCommand(JSONObject arguments) throws JSONException {
				List<String> cmdLine = new ArrayList<String>();
				cmdLine.add("npm");
				cmdLine.add("config");
				cmdLine.add("get");
				String key = JSONUtil.getString(arguments, "key");
				Assert.isNotNull(key);
				cmdLine.add(key);
				return new ExternalCommand(cmdLine.toArray(new String[cmdLine.size()]));
			}
		};
		new SimpleCommandHandler("config/set", this) {
			@Override
			protected ExternalCommand createCommand(JSONObject arguments) throws JSONException {
				List<String> cmdLine = new ArrayList<String>();
				cmdLine.add("npm");
				cmdLine.add("config");
				cmdLine.add("set");
				String key = JSONUtil.getString(arguments, "key");
				cmdLine.add(key);
				String value = JSONUtil.getString(arguments, "value");
				if (value!=null) {
					cmdLine.add(value);
				} else {
					cmdLine.add("true");
				}
				return new ExternalCommand(cmdLine.toArray(new String[cmdLine.size()]));
			}
		};
		new SimpleCommandHandler("config/delete", this) {
			@Override
			protected ExternalCommand createCommand(JSONObject arguments) throws JSONException {
				List<String> cmdLine = new ArrayList<String>();
				cmdLine.add("npm");
				cmdLine.add("config");
				cmdLine.add("delete");
				String key = JSONUtil.getString(arguments, "key");
				cmdLine.add(key);
				return new ExternalCommand(cmdLine.toArray(new String[cmdLine.size()]));
			}
		};
	}
	
}
