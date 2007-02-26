/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2006 Chris Eldredge
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
package net.sourceforge.vulcan.core.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sourceforge.vulcan.core.Store;
import net.sourceforge.vulcan.dto.ProjectStatusDto;
import net.sourceforge.vulcan.exception.StoreException;
import net.sourceforge.vulcan.metadata.SvnRevision;

import org.apache.commons.collections.map.LRUMap;

@SvnRevision(id="$Id$", url="$HeadURL$")
public class BuildOutcomeCache {
	private final Lock readLock;
	private final Lock writeLock;
	
	final Map<String, UUID> latestOutcomes = new HashMap<String, UUID>();
	final Map<String, UUID> latestOutcomesReadonly = Collections.unmodifiableMap(latestOutcomes);
	
	int cacheSize;
	
	Map<UUID, ProjectStatusDto> outcomes;
	
	/**
	 * Mapping of project name to all build outcome ids in store, chronological.
	 */
	Map<String, List<UUID>> outcomeIDs;
	
	/**
	 * Mapping of UUID to projectName.
	 */
	final Map<UUID, String> idToProjects = new HashMap<UUID, String>();
	
	Store store;
	
	public BuildOutcomeCache() {
		final ReadWriteLock lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
	}
	
	@SuppressWarnings("unchecked")
	public void init() {
		if (cacheSize <= 0) {
			throw new IllegalStateException("Must set cacheSize > 0.");
		}
		outcomes = new LRUMap(cacheSize);
		
		writeLock.lock();
		
		try {
			outcomeIDs = store.getBuildOutcomeIDs();
			
			for (Entry<String, List<UUID>> e : outcomeIDs.entrySet()) {
				final List<UUID> ids = e.getValue();
				if (ids.isEmpty()) {
					continue;
				}
				latestOutcomes.put(e.getKey(), ids.get(ids.size()-1));
				
				for (UUID id : ids) {
					idToProjects.put(id, e.getKey());
				}
			}
		} finally { 
			writeLock.unlock();
		}
	}
	
	/**
	 * Saves a new build outcome.
	 */
	public void store(ProjectStatusDto statusDto) throws StoreException {
		writeLock.lock();
		try {
			store(statusDto.getName(), statusDto);
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @return Mapping of project name to latest build outcome.
	 */
	public Map<String, ProjectStatusDto> getLatestOutcomes() {
		final Map<String, ProjectStatusDto> map = new HashMap<String, ProjectStatusDto>();
		final Set<Entry<String, UUID>> outcomes;
		
		readLock.lock();
		try {
			outcomes = new HashSet<Entry<String, UUID>>(latestOutcomes.entrySet());
		} finally {
			readLock.unlock();
		}
		
		for (Entry<String, UUID> e : outcomes) {
			final String name = e.getKey();
			map.put(name, getOutcome(e.getValue()));
		}
		return map;
	}

	public UUID getLatestOutcomeId(String projectName) {
		try {
			readLock.lock();
			return latestOutcomes.get(projectName);
		} finally {
			readLock.unlock();
		}
	}
	
	public List<UUID> getOutcomeIds(String projectName) {
		try {
			readLock.lock();
			return outcomeIDs.get(projectName);
		} finally {
			readLock.unlock();
		}
	}

	public ProjectStatusDto getLatestOutcome(String projectName) {
		final UUID uuid;
		
		try {
			readLock.lock();
			uuid = latestOutcomes.get(projectName);
			if (uuid == null) {
				return null;
			}
		} finally {
			readLock.unlock();
		}
		
		return getOutcome(uuid);
	}
	
	public ProjectStatusDto getOutcome(UUID id) {
		readLock.lock();
		
		try {
			if (outcomes.containsKey(id)) {
				return outcomes.get(id);
			}
		} finally {
			readLock.unlock();
		}
		
		writeLock.lock();
		
		try {
			final String projectName = idToProjects.get(id);
			final ProjectStatusDto status = store.loadBuildOutcome(projectName, id);
			
			if (status.getBuildNumber() == null) {
				// Legacy: build number was not introduced until several
				// instances went live.
				Integer index = outcomeIDs.get(projectName).indexOf(id);
				
				status.setBuildNumber(index);
			}
			
			// In case project was renamed, force status to use current name
			status.setName(projectName);
			
			outcomes.put(id, status);
			return status;
		} catch (StoreException e) {
			return null;
		} finally {
			writeLock.unlock();
		}
	}
	
	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public int getCacheSize() {
		return cacheSize;
	}
	
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	void mergeOutcomes(Map<String, ProjectStatusDto> outcomes) throws StoreException {
		writeLock.lock();
		try {
			for (Entry<String, ProjectStatusDto> e : outcomes.entrySet()) {
				store(e.getKey(), e.getValue());
			}
		} finally {
			writeLock.unlock();
		}
	}
	
	private void store(String name, ProjectStatusDto statusDto) throws StoreException {
		final UUID id = store.storeBuildOutcome(statusDto);
		latestOutcomes.put(name, id);
		outcomes.put(id, statusDto);
		
		final List<UUID> ids;
		
		if (outcomeIDs.containsKey(name)) {
			ids = outcomeIDs.get(name);
		} else {
			ids = new ArrayList<UUID>();
			outcomeIDs.put(name, ids);
		}
		
		ids.add(id);
		statusDto.setId(id);
	}
}
