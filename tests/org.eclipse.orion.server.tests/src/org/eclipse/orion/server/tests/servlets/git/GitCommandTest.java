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
package org.eclipse.orion.server.tests.servlets.git;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;

import org.eclipse.orion.internal.server.core.IOUtilities;
import org.eclipse.orion.internal.server.servlets.ProtocolConstants;
import org.eclipse.orion.server.git.GitConstants;
import org.eclipse.orion.server.git.objects.Command;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class GitCommandTest extends GitTest {

	@Test
	public void command() throws Exception {
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), gitDir.toString());
		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitCommandUri = gitSection.getString(GitConstants.KEY_COMMAND);

		WebRequest request = getPosGitCommandRequest(gitCommandUri, new String[] {"branch"});
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		String commandResult = response.getText();

		String expectedResult = "* master\r\n";
		assertEquals(expectedResult, commandResult);
	}

	@Test
	public void commandWithArgs() throws Exception {
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), gitDir.toString());
		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitCommandUri = gitSection.getString(GitConstants.KEY_COMMAND);

		WebRequest request = getPosGitCommandRequest(gitCommandUri, new String[] {"commit", "--amend", "-m", "'Amended commit'"});
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		String commandResult = response.getText();

		String expectedResult = "[master ecf5161e231e3f798ec9ae0910264bd5c85a4c37] 'Amended commit'\r\n";
		assertEquals(expectedResult, commandResult);
	}

	@Test
	public void notImplementedCommand() throws Exception {
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), gitDir.toString());
		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitCommandUri = gitSection.getString(GitConstants.KEY_COMMAND);

		WebRequest request = getPosGitCommandRequest(gitCommandUri, new String[] {"status"});
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponseCode());
	}

	private static WebRequest getPosGitCommandRequest(String location, String[] args) throws UnsupportedEncodingException, JSONException {
		String requestURI;
		if (location.startsWith("http://"))
			requestURI = location;
		else if (location.startsWith("/"))
			requestURI = SERVER_LOCATION + location;
		else
			requestURI = SERVER_LOCATION + GIT_SERVLET_LOCATION + Command.RESOURCE + location;
		JSONObject body = new JSONObject();
		body.put(GitConstants.KEY_COMMAND_ARGS, Arrays.asList(args));

		WebRequest request = new PostMethodWebRequest(requestURI, IOUtilities.toInputStream(body.toString()), "application/json");
		request.setHeaderField(ProtocolConstants.HEADER_ORION_VERSION, "1");
		setAuthentication(request);
		return request;
	}
}
