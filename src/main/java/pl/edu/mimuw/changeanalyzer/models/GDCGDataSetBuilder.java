package pl.edu.mimuw.changeanalyzer.models;

import java.util.List;

import weka.core.Instance;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * This data set builder class assumes geometric decrease (GD) of bug-proneness
 * (see {@link GDDataSetBuilder}) and uses commit group (CG) as model. Each 
 * produced instance represents a group of subsequent commits starting with
 * a commit directly succeeding a bug-fix. Groups contain from one commit
 * up to a whole chunk.
 * This being so, initialLevel corresponds to the bug-proneness of the full chunk,
 * rather than bug-proneness of the last commit (and hence should default to 1.0).
 * 
 * @author Adam Wierzbicki
 */
public class GDCGDataSetBuilder extends GDDataSetBuilder {

	/**
	 * Construct a new GDCG data set builder.
	 * 
	 * @param initialLevel		Bug-proneness the whole chunk
	 * @param ratio				Bug-proneness decrease ratio
	 * @param bugfixesIncluded	Should the created bulder include bugfix changes in produced instances
	 */
	public GDCGDataSetBuilder(double initialLevel, double ratio, boolean bugfixesIncluded) {
		super(initialLevel, ratio, bugfixesIncluded);
	}
	
	/**
	 * Construct a new GDCG data set builder (inital level = 1.0, bugfixes not included).
	 * 
	 * @param ratio Bug-proneness decrease ratio
	 */
	public GDCGDataSetBuilder(double ratio) {
		this(1.0, ratio, false);
	}
	
	@Override
	protected void processChunk(List<StructureEntityVersion> versions, boolean isFixed) {
		this.changeCounter.reset();
		int i = versions.size() - 2 + (this.bugfixesIncluded() ? 0 : 1);
		
		for (StructureEntityVersion version: versions) {
			double bugProneness = isFixed ? (i < 0 ? 0.0 : this.getBugProneness(i)) : Instance.missingValue();
			int[] changeCounts = this.changeCounter.countChanges(version);
			Instance instance = this.createInstance(version, bugProneness, changeCounts);
			this.addToResult(instance);
			--i;
		}
	}

}
