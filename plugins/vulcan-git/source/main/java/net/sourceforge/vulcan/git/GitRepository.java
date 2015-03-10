/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2012 Chris Eldredge
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.sourceforge.vulcan.git;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vulcan.RepositoryAdaptor;
import net.sourceforge.vulcan.core.BuildDetailCallback;
import net.sourceforge.vulcan.core.support.FileSystem;
import net.sourceforge.vulcan.core.support.FileSystemImpl;
import net.sourceforge.vulcan.dto.ChangeLogDto;
import net.sourceforge.vulcan.dto.ProjectConfigDto;
import net.sourceforge.vulcan.dto.ProjectStatusDto;
import net.sourceforge.vulcan.dto.RepositoryTagDto;
import net.sourceforge.vulcan.dto.RevisionTokenDto;
import net.sourceforge.vulcan.exception.RepositoryException;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GitRepository implements RepositoryAdaptor {
	protected static enum Command {
		fetch(true),
		clone(true),
		status(true),
		pull(true),
		log(true),
		diff(false),
		purge(false),
		tags(false),
		checkout(false),
		clean(false),
		branch(true),
		tag(true);
		
		private final boolean remote;
 
		Command(boolean remote) {
			this.remote = remote;
		}
		
		public boolean isRemote() {
			return remote;
		}
	}
	
	private static final Log LOG = LogFactory.getLog(GitRepository.class);
		
	private final ProjectConfigDto projectConfig;
	private final GitProjectConfig settings;
	private final GitConfig globals;
	
	private FileSystem fileSystem = new FileSystemImpl();
	private String changeSetLogFormat = "--pretty=\"%x1e%h%x1f%H%x1f%cn%x1f%ce%x1f%ci%x1f%d%x1f%B%x1f\"";
	
	public GitRepository(ProjectConfigDto projectConfig, GitConfig globals) {
		this.projectConfig = projectConfig;
		this.settings = (GitProjectConfig)projectConfig.getRepositoryAdaptorConfig().copy();
		this.globals = globals;
	}

	public boolean hasIncomingChanges(ProjectStatusDto mostRecentBuildInSameWorkDir) throws RepositoryException {
		try {
			final RevisionTokenDto latestRevision = getLatestRevision(mostRecentBuildInSameWorkDir.getRevision());
			
			if (!latestRevision.equals(mostRecentBuildInSameWorkDir.getRevision())) {
				return true;
			}
			
			return hasIncomingChangesFromRemote();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void prepareRepository(BuildDetailCallback buildDetailCallback) throws RepositoryException, InterruptedException {
		final File workDir = getLocalRepositoryPath();
		
		if (!isWorkingCopy()) {
			clone(workDir, buildDetailCallback);
		} else {
			pull(buildDetailCallback);
		}
	}

	public void clone(File workDir, BuildDetailCallback buildDetailCallback) throws RepositoryException {
		if (!isRemoteRepositoryConfigured()) {
			throw new RepositoryException("git.errors.no.repo.and.no.remote", null, workDir);
		}

		buildDetailCallback.setDetailMessage("git.activity.clone", null);
		
		List<String> args = new ArrayList<String>();
		args.add("--no-checkout");
		args.add(settings.getRemoteRepositoryUrl());
		args.add(".");

		tryInvoke(Command.clone, args.toArray(new String[args.size()]));
	}
	
	public void pull(BuildDetailCallback buildDetailCallback) throws RepositoryException {
		if (!isRemoteRepositoryConfigured()) {
			return;
		}

		buildDetailCallback.setDetailMessage("git.activity.pull", null);
		
		tryInvoke(Command.fetch, "--all");
	}
	
	public boolean isWorkingCopy() throws RepositoryException {
		if (!fileSystem.directoryExists(getLocalRepositoryPath())) {
			return false;
		}
		
		try {
			String[] args = { "-sb" };
			final Invoker invoker = createInvoker();
			InvocationResult result = invoker.invoke("status", getLocalRepositoryPath(), args);
			return result.isSuccess();
		} catch (IOException ignore) {
			return false;
		}
	}

	public RevisionTokenDto getLatestRevision(RevisionTokenDto previousRevision) throws RepositoryException, InterruptedException {
		InvocationResult result = tryInvoke(Command.log, "-1", getSelectedRemoteBranch(), "--pretty=format:%h:%H");
		
		final String output = result.getOutput().trim();

		String[] parts = output.split(":", 2);
		
		if (parts.length != 2) {
			throw new RepositoryException("Expected log output: " + output, (Throwable)null);
		}
		
		return new RevisionTokenDto(parts[0], output);
	}
	
	boolean hasIncomingChangesFromRemote() throws RepositoryException, InterruptedException {
		if (!isRemoteRepositoryConfigured()) {
			return false;
		}
		final String branchRange = getSelectedBranch() + ".." + getSelectedRemoteBranch();
		
		tryInvoke(Command.fetch, "--all");
		
		InvocationResult result = tryInvoke(Command.log, "-1", branchRange, "--format=oneline");
		final String output = result.getOutput().trim();
		final boolean rtn = output.length() > 1;
		return rtn;
	}

	boolean isRemoteRepositoryConfigured() {
		return !StringUtils.isBlank(settings.getRemoteRepositoryUrl());
	}

	String getSelectedBranch() {
		if (StringUtils.isBlank(settings.getBranch())) {
			return "master";
		}
		
		return settings.getBranch();
	}
	
	String getSelectedRemote() {
		if (StringUtils.isBlank(settings.getRemoteName())) {
			return "origin";
		}
		String remote = settings.getRemoteName();
		if (remote.endsWith("/"))
			return remote.substring(0, remote.length()-1);
		
		return remote;
	}
	
	String getSelectedRemoteBranch() {
		String branch = getSelectedBranch();
		String remote = getSelectedRemote();
		
		return remote + "/" +  branch;
	}

	File getLocalRepositoryPath() {
		return new File(projectConfig.getWorkDir());
	}
	
	public void createPristineWorkingCopy(BuildDetailCallback buildDetailCallback) throws RepositoryException, InterruptedException {
		buildDetailCallback.setDetailMessage("git.activity.clean", null);
		tryInvoke(Command.clean, "-f", "-d", "-x");

		buildDetailCallback.setDetailMessage("git.activity.checkout", null);
		tryInvoke(Command.checkout, "-f", "-B", getSelectedBranch(), getSelectedRemoteBranch());
	}

	public void updateWorkingCopy(BuildDetailCallback buildDetailCallback) throws RepositoryException {
		tryInvoke(Command.checkout, "-f", "-B", getSelectedBranch(), getSelectedRemoteBranch());
	}

	public ChangeLogDto getChangeLog(RevisionTokenDto previousRevision,	RevisionTokenDto currentRevision, OutputStream diffOutputStream) throws RepositoryException, InterruptedException {
		final String revRange = previousRevision.getRevision() + ".." + currentRevision.getRevision();
		
		getDiff(revRange, diffOutputStream);
		
		ChangeLogDto changes = getChangeSets(revRange);
		
		changes.removeChangeSet(previousRevision);
		
		return changes;
	}

	private void getDiff(String revisionRange, OutputStream diffOutputStream) throws RepositoryException {
		if (diffOutputStream == null) {
			return;
		}
		
		try {
			tryInvokeWithStream(Command.diff, diffOutputStream, revisionRange);
		} finally {
			try {
				diffOutputStream.close();
			} catch (IOException e) {
				throw new RepositoryException(e);
			}
		}
	}

	private ChangeLogDto getChangeSets(final String revisionRange) throws RepositoryException {
		final InvocationResult result = tryInvoke(Command.log, "--name-status", changeSetLogFormat, revisionRange);
		
		final CommitLogParser parser = new CommitLogParser();
		
		try {
			return parser.parse(result.getOutput());
		} catch (Exception e) {
			throw new RepositoryException("git.errors.parse", e, e.getMessage());
		}
	}
	
	public List<RepositoryTagDto> getAvailableTagsAndBranches() throws RepositoryException {
		final List<RepositoryTagDto> results = new ArrayList<RepositoryTagDto>();
		final List<String> namedRevisions = new ArrayList<String>();
		
		getBranchTagItems(Command.branch, new String[] { "-r" }, results, namedRevisions);
		getBranchTagItems(Command.tag, new String[] { "-l" }, results, namedRevisions);
		
		return results;
	}
		
	private void getBranchTagItems(Command command, String[] args, List<RepositoryTagDto> results, List<String> namedRevisions) throws RepositoryException {
		final InvocationResult result = tryInvoke(command, args);
		final String output = result.getOutput().trim();
		final String[] lines = output.split("\n");
		String name;
		final int linesLength = lines.length;
		
		for(int i = 0; i < linesLength; i++) {
			if (lines[i].length() == 0)
				continue;
			
			if (command == Command.branch && lines[i].contains("->"))
				continue;
			
			name = lines[i].trim().replaceFirst(getSelectedRemote() + "/", "");
			
			results.add(new RepositoryTagDto(name, name));
			namedRevisions.add(name);
		}
	}
	
	protected InvocationResult tryInvoke(Command command, String... args) throws RepositoryException {
		return tryInvokeWithStream(command, null, args);
	}
	
	protected InvocationResult tryInvokeWithStream(Command command, OutputStream output, String... args) throws RepositoryException {
		final File workDir = getLocalRepositoryPath();
		
		if (!fileSystem.directoryExists(workDir)) {
			try {
				fileSystem.createDirectory(workDir);
			} catch (IOException e) {
				throw new RepositoryException("git.errors.mkdir", e, workDir);
			}
		}

		final Invoker invoker = createInvoker();
		
		if (output != null) {
			invoker.setOutputStream(output);
		}

		try {
			return invoker.invoke(command.name(), workDir, args);
		} catch (IOException e) {
			final String errorText = invoker.getErrorText();
			
			StringBuilder sb = new StringBuilder();
			sb.append("Unexpected exception invoking git: stderr: ");
			sb.append(errorText);
			if (output == null) {
				final String stdout = invoker.getOutputText();
				if (StringUtils.isNotBlank(stdout)) {
					sb.append("\nstdout: ");
					sb.append(stdout);
				}
			}
			
			LOG.error(sb.toString(), e);
			
			throw new RepositoryException("git.errors.invocation", e, errorText, invoker.getExitCode());
		}
	}
	
	protected Invoker createInvoker() {
		final ProcessInvoker invoker = new ProcessInvoker();
		
		invoker.setExecutable(globals.getExecutable());
		
		return invoker;
	}

	public String getRepositoryUrl() {
		return settings.getRemoteRepositoryUrl();
	}

	public String getTagOrBranch() {
		return getSelectedBranch();
	}

	public void setTagOrBranch(String tagName) {
		settings.setBranch(tagName);
	}
	
	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}
		
	protected ProjectConfigDto getProjectConfig() {
		return projectConfig;
	}
	
	protected GitProjectConfig getSettings() {
		return settings;
	}
	
	protected GitConfig getGlobals() {
		return globals;
	}
}
