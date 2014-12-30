package pl.edu.mimuw.changeanalyzer.models;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


public class ChangeCounter {
	
	private Map<ChangeType, Integer> counters;
	
	public ChangeCounter() {
		this.counters = new EnumMap<ChangeType, Integer>(ChangeType.class);
	}
	
	public int addChange(SourceCodeChange change) {
		ChangeType changeType = change.getChangeType();
		this.counters.compute(changeType, (k, v) -> (v == null) ? 1 : v + 1);
		return this.getCount(changeType);
	}
	
	public int[] countChanges(StructureEntityVersion version) {
		for (SourceCodeChange change: version.getSourceCodeChanges()) {
			this.addChange(change);
		}
		return this.getCounts();
	}
	
	public int[] getCounts() {
		Integer[] counts = Arrays.stream(ChangeType.values()).map(this::getCount).toArray(Integer[]::new);
		return ArrayUtils.toPrimitive(counts);
	}
	
	public int getCount(ChangeType changeType) {
		return this.counters.getOrDefault(changeType, 0);
	}

}
