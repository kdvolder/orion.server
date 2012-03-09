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
package org.eclipse.orion.server.git.cli;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.pgm.TextBuiltin;
import org.eclipse.jgit.pgm.opt.CmdLineParser;
import org.eclipse.jgit.pgm.opt.SubcommandHandler;
import org.kohsuke.args4j.Argument;

public class CLIGitCommand {
	@Argument(index = 0, metaVar = "metaVar_command", required = true, handler = SubcommandHandler.class)
	private TextBuiltin subcommand;

	@Argument(index = 1, metaVar = "metaVar_arg")
	private List<String> arguments = new ArrayList<String>();

	public TextBuiltin getSubcommand() {
		return subcommand;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public static String execute(String[] argv, Repository db) throws Exception {

		CLIGitCommand bean = new CLIGitCommand();
		final CmdLineParser clp = new CmdLineParser(bean);
		clp.parseArgument(argv);

		final TextBuiltin cmd = bean.getSubcommand();
		if (cmd.requiresRepository())
			cmd.init(db, null);
		else
			cmd.init(null, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cmd.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(baos)));
		try {
			cmd.execute(bean.getArguments().toArray(new String[bean.getArguments().size()]));
		} finally {
			if (cmd.out != null)
				cmd.out.flush();
		}

		return baos.toString();
	}
}
