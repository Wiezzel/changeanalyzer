package pl.edu.mimuw.changeanalyzer.models;

import java.util.List;

import weka.core.Instance;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

/**
 * This data set builder class assumes geometric decrease (GD) of bug-proneness
 * (see {@link GDDataSetBuilder}) and uses single commit (SC) as model (each 
 * produced instance represents one commit).
 * 
 * @author Adam Wierzbicki
 */
public class GDSCDataSetBuilder extends GDDataSetBuilder {
	
	/**
	 * Construct a new GDSC data set builder.
	 * 
	 * @param initialLevel		Bug-proneness of the last commit in a chunk
	 * @param ratio				Bug-proneness decrease ratio
	 * @param bugfixesIncluded	Should the created bulder include bugfix changes in produced instances
	 */
	public GDSCDataSetBuilder(double initialLevel, double ratio, boolean bugfixesIncluded) {
		super(initialLevel, ratio, bugfixesIncluded);
	}
	

	@Override
	protected void processChunk(List<StructureEntityVersion> versions, boolean isFixed) {
		int i = versions.size() - 2 + (this.bugfixesIncluded() ? 0 : 1);
		
		for (StructureEntityVersion version: versions) {
			double bugProneness = isFixed ? (i < 0 ? 0.0 : this.getBugProneness(i)) : Instance.missingValue();
			int[] changeCounts = this.changeCounter.reset().countChanges(version);
			Instance instance = this.createInstance(version, bugProneness, changeCounts);
			this.addToResult(instance);
			--i;
		}
	}
	
}
