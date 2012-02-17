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

/**
 * A command context represents information that specify the execution context for an external command.
 * @author kdvolder
 */
public interface ICommandContext {

	File getWorkingDir();
	File getHomeDir();
	
}
