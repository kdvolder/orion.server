/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.git.objects;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.jgit.lib.Repository;

public class Command extends GitObject {

	public static final String RESOURCE = "command"; //$NON-NLS-1$
	public static final String TYPE = "Command"; //$NON-NLS-1$

	Command(URI cloneLocation, Repository db) {
		super(cloneLocation, db);
	}

	@Override
	protected String getType() {
		return TYPE;
	}

	@Override
	protected URI getLocation() throws URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

}
