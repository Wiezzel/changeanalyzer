package pl.edu.mimuw.changeanalyzer.models;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * Simple class for counting code changes of different types. It holds
 * a separate, internal counter for each change type.
 * 
 * @author Adam Wierzbicki
 */
public class ChangeCounter {
	
	private Map<ChangeType, Integer> counters;
	
	/**
	 * Construct a new, zero-initialized counter.
	 */
	public ChangeCounter() {
		this.counters = new EnumMap<ChangeType, Integer>(ChangeType.class);
	}
	
	/**
	 * Reset this counter.
	 * 
	 * @return This counter
	 */
	public ChangeCounter reset() {
		this.counters.clear();
		return this;
	}
	
	/**
	 * Count a change (increment counter of the appropriate type).
	 * 
	 * @param change A change to be counted
	 * @return Total count of changes of this type
	 */
	public int countChange(SourceCodeChange change) {
		ChangeType changeType = change.getChangeType();
		this.counters.compute(changeType, (k, v) -> (v == null) ? 1 : v + 1);
		return this.getCount(changeType);
	}
	
	/**
	 * Count all changes in a method version.
	 * 
	 * @param version Method version containing changes to be counted
	 * @return Array containing total counts of all types of changes
	 */
	public int[] countChanges(StructureEntityVersion version) {
		for (SourceCodeChange change: version.getSourceCodeChanges()) {
			this.countChange(change);
		}
		return this.getCounts();
	}
	
	/**
	 * Get all counts of changes.
	 * 
	 * @return Array containing total counts of all types of changes
	 */
	public int[] getCounts() {
		Integer[] counts = Arrays.stream(ChangeType.values()).map(this::getCount).toArray(Integer[]::new);
		return ArrayUtils.toPrimitive(counts);
	}
	
	/**
	 * Get count of changes of a given type.
	 * 
	 * @param changeType Change type to get count
	 * @return Total count of changes of the given type
	 */
	public int getCount(ChangeType changeType) {
		return this.counters.getOrDefault(changeType, 0);
	}

}
