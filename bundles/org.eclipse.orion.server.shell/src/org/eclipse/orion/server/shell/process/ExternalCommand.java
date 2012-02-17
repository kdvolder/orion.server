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
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information about an 'external' command that can be run through the OS. 
 * <p>
 * This is a simplistic implementation. A more sophisticate implementation should allow for
 * different OS's (commands may return different information depending on the OS).
 * 
 * @author Kris De Volder
 */
public class ExternalCommand {

	private final String[] command;
	private File homeDir;

	public ExternalCommand(String... command) {
		ArrayList<String> pieces = new ArrayList<String>(command.length);
		for (String piece : command) {
			if (piece!=null) {
				pieces.add(piece);
			}
		}
		this.command = pieces.toArray(new String[pieces.size()]);
	}
	
	public ExternalCommand(List<String> cmdLine) {
		this(cmdLine.toArray(new String[cmdLine.size()]));
	}

	public String[] getProgramAndArgs() {
		return command;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (String piece : command) {
			if (!first) {
				buf.append(" ");
			}
			buf.append(piece);
			first = false;
		}
		return buf.toString();
	}
}
