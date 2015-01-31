package pl.edu.mimuw.changeanalyzer.models.measures;

import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * This measure assigns bug-proneness score of 1.0 to the last commit
 * of a chunk and geometrically decreasing scores to the previous ones.
 * 
 * @author Adam Wierzbicki
 */
public class GeometricMeasure implements BugPronenessMeasure {
	
	private final double ratio;
	private double[] bugProneness;
	
	/**
	 * Construct a new GeometricMeasure.
	 * 
	 * @param ratio Ratio of the geometric decrease
	 */
	public GeometricMeasure(double ratio) {
		this.ratio = ratio;
	}

	@Override
	public void startNewChunk(List<StructureEntityVersion> chunk) {
		this.bugProneness = new double[chunk.size()];
		double bugProneness = 1.0;
		for (int i = chunk.size() - 1; i >= 0; --i) {
			this.bugProneness[i] = bugProneness;
			bugProneness *= this.ratio;
		}
	}

	@Override
	public double getBugProneness(int index) {
		return this.bugProneness[index];
	}

	@Override
	public String getName() {
		return "geomBugProneness" + this.ratio;
	}

}
