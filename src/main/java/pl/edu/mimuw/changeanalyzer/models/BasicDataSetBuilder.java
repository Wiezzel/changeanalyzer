package pl.edu.mimuw.changeanalyzer.models;

import java.util.List;

import weka.core.Instance;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

/**
 * Basic data set builder. Each built instance represents one commit.
 * Last commit in a chunk is assumed to be 50% bug-prone and this values
 * decreases geometrically with 50% ratio in preceding commits.  
 * 
 * @author Adam Wierzbicki
 */
public class BasicDataSetBuilder extends GDDataSetBuilder {
	
	public static final double LAST_COMMIT_PRONENESS = 0.5;
	public static final double DECREASE_RATIO = 0.5;

	/**
	 * Create a basic data set builder.
	 */
	public BasicDataSetBuilder() {
		super(LAST_COMMIT_PRONENESS, DECREASE_RATIO, true);
	}
	

	@Override
	protected void processChunk(List<StructureEntityVersion> versions, boolean isFixed) {
		if (isFixed) {
			int i = versions.size() - 2;
			for (StructureEntityVersion version: versions) {
				double bugProneness = i < 0 ? 0.0 : this.getBugProneness(i);
				int[] changeCounts = this.changeCounter.reset().countChanges(version);
				Instance instance = this.createInstance(version, bugProneness, changeCounts);
				this.addToResult(instance);
				--i;
			}
		}
		else {
			for (StructureEntityVersion version: versions) {
				int[] changeCounts = this.changeCounter.reset().countChanges(version);
				Instance instance = this.createInstance(version, Instance.missingValue(), changeCounts);
				this.addToResult(instance);
			}
		}
	}
	
}
