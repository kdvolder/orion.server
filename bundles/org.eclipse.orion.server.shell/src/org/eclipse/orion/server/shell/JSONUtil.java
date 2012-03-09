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

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtil {

	public static boolean getBoolean(JSONObject arguments, String key) {
		if (arguments.has(key) && !arguments.isNull(key)) {
			try {
				return arguments.getBoolean(key);
			} catch (JSONException e) {
			}
		}
		return false;
	}

	public static String getString(JSONObject arguments, String key) {
		if (arguments!=null && arguments.has(key) && !arguments.isNull(key)) {
			try {
				return arguments.getString(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
