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
	 * Create a GDSC data set builder.
	 */
	public GDSCDataSetBuilder(double initialLevel, double ratio, boolean bugfixesIncluded) {
		super(initialLevel, ratio, bugfixesIncluded);
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
