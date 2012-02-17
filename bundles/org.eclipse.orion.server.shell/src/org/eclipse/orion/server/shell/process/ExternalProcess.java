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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Convenient wrapper around a {@link Process}. Simplifies the synchronous execution of external
 * commands by handling reading from out and error streams and either buffering the result output 
 * for later retrieval, or forwarding the to output to designated streams. 
 * 
 * @author Kris De Volder
 */
public class ExternalProcess {

	private static final long MAX_DEAD_TIME = 15000;
	
	private long lastSignOfLife = System.currentTimeMillis();
	
	/**
	 * ProcessKiller thread is a watch dog that ensures the process will be killed
	 * afer a certain amount of inactive time (where inactive means no output or
	 * error output was received from the process).
	 */
	private class ProcessKiller extends Thread {
		
		private static final long MIN_SLEEP_TIME = 500;

		public ProcessKiller() {
			super("ProcessKiller");
			start();
		}
		
		@Override
		public void run() {
			while (isRunning()) {
				if (!wasRecentlyActive()) {
					process.destroy();
				}
				try {
					sleep(getSleepTime());
				} catch (InterruptedException e) {
				}
			}
		}

		private boolean wasRecentlyActive() {
			return getDeadTime()<MAX_DEAD_TIME;
		}

		/**
		 * Compute sleep time so that we sleep as long as possible and only wake up around the
		 * earliest time our timeout could have been exceeded, but no sooner.
		 */
		private long getSleepTime() {
			long deadTimeRemaining = getDeadTime() - MAX_DEAD_TIME;
			if (deadTimeRemaining > MIN_SLEEP_TIME) {
				return deadTimeRemaining;
			} else {
				return MIN_SLEEP_TIME;
			}
		}

		private long getDeadTime() {
			return System.currentTimeMillis() - lastSignOfLife;
		}

		private boolean isRunning() {
			try {
				process.exitValue();
				return false;
			} catch (IllegalThreadStateException e) {
				return true;
			}
		}
	}
	
	/**
	 * A thread that keeps reading input from a Stream until the end is reached
	 * or there's some error reading the Stream.
	 */
	public class StreamGobler extends Thread {
		
		private final OutputStream echo;
		private InputStream toRead; //Stream to read. This is nulled after all input has been consumed.
		
		/**
		 * Creates a StreamGobler that reads input from an input stream
		 * and buffers up all input it has read for later retrieval via
		 * the getOut() method.
		 */
		public StreamGobler(InputStream toRead) {
			this(toRead, new ByteArrayOutputStream());
		}

		/**
		 * Creates a StreamGobler that reads input from an input stream
		 * and writes it out to an outputstream.
		 */
		public StreamGobler(InputStream toRead, OutputStream forwardTo) {
			this.toRead = toRead;
			this.echo = forwardTo;
			start();
		}

		@Override
		public void run() {
			byte[] buf = new byte[256];
			while (toRead!=null) {
				try {
					int i = toRead.read(buf);
					if (i==-1) {
						//EOF
						toRead = null; //Done!
					} else {
						append(buf, i);
					}
				} catch (IOException e) {
					toRead = null;
					ByteArrayOutputStream errMsg = new ByteArrayOutputStream();
					e.printStackTrace(new PrintStream(errMsg));
					append(errMsg.toByteArray());
				}
			}
		}
		
		private void append(byte[] buf) {
			append(buf, buf.length);
		}

		private void append(byte[] buf, int len) {
			touch();
			System.out.print(new String(buf, 0, len));
			if (echo!=null) {
				try {
					echo.write(buf, 0, len);
				} catch (IOException e) {
				}
			}
		}

		public String getContents() throws InterruptedException {
			try {
				this.join();
				if (echo instanceof ByteArrayOutputStream) {
					return ((ByteArrayOutputStream)echo).toString();
				} else {
					return null;
				}
			} finally {
				toRead = null;
			}
		}
	}

	private Process process;
	private StreamGobler err; // Standard error is to be read from here
	private StreamGobler out; // Standard out is to be read from here
	private int exitValue = -9999;
	private ExternalCommand cmd;
	
	private void touch() {
		lastSignOfLife = System.currentTimeMillis();
	}
	
	/**
	 * Creates an external process and waits for it to terminate. The output and error streams
	 * will be read and forwarded to System.out and System.err (for debug purposes). Also the
	 * contents of those Streams is saved and can be retrieved later getOut and getErr
	 */
	public ExternalProcess(ICommandContext context, ExternalCommand cmd) throws IOException, InterruptedException {
		this(context, cmd, System.out, System.err);
	}
	
	/**
	 * Creates an external process and waits for it to terminate. The output and error streams
	 * will be read and forwarded to System.out and System.err (for debug purposes). Also the
	 * contents of those Streams is saved and can be retrieved later getOut and getErr
	 */
	public ExternalProcess(ICommandContext context, ExternalCommand cmd, OutputStream out, OutputStream err) throws IOException, InterruptedException {
		this.cmd = cmd; 
		ProcessBuilder processBuilder = new ProcessBuilder(cmd.getProgramAndArgs());
		processBuilder.directory(ensureDirExists(context.getWorkingDir()));
		File homeDir = context.getHomeDir();
		if (homeDir!=null) {
			homeDir = ensureDirExists(homeDir);
			processBuilder.environment().put("HOME", homeDir.getAbsolutePath());
		}
		process = processBuilder.start();
		this.err = new StreamGobler(process.getErrorStream(), out);
		this.out = new StreamGobler(process.getInputStream(), err);
		new ProcessKiller();
		exitValue = process.waitFor();
	}
	
	private File ensureDirExists(File dir) throws IOException {
		if (dir.exists() || dir.mkdirs()) {
			return dir;
		} else {
			throw new IOException("Couldn't find or create: "+dir);
		}
	}

	public String getOut() throws InterruptedException {
		return out.getContents();
	}
	
	public String getErr() throws InterruptedException {
		return err.getContents();
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		try  {
			process.exitValue();
			result.append(">>>> ExternalProcess: ");
			result.append(cmd+"\n");
			result.append("exitValue = "+exitValue+"\n");
			String strOut = getOut();
			if (strOut!=null) {
				result.append("\n------- System.out -------\n");
				result.append(strOut);
			}
			String strErr = getErr();
			if (strErr!=null) {
				result.append("\n------- System.err -------\n");
				result.append(getOut());
			}
			result.append("<<<< ExternalProcess");
			return result.toString();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return result.toString();
		} catch (IllegalThreadStateException e) {
			return "ExternalProcess(RUNNING, "+cmd+")";
		}
	}

	public int getExitValue() {
		return exitValue;
	}

}
