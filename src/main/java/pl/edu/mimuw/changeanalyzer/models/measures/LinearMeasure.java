package pl.edu.mimuw.changeanalyzer.models.measures;

import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * This measure assigns bug-proneness score of 1.0 to the last commit
 * of a chunk and linearly decreasing scores to the previous ones.
 * 
 * @author Adam Wierzbicki
 */
public class LinearMeasure implements BugPronenessMeasure {

	private double initialProneness;
	private int chunkSize;
	
	/**
	 * Construct a new LinearMeasure.
	 * 
	 * @param initialProneness Desired bug-proneness of the first commit in a chunk
	 */
	public LinearMeasure(double initialProneness) {
		this.initialProneness = initialProneness;
	}

	@Override
	public void startNewChunk(List<StructureEntityVersion> chunk) {
		this.chunkSize = chunk.size();
	}

	@Override
	public double getBugProneness(int index) {
		return this.initialProneness + (1.0 - this.initialProneness) * (index + 1) / this.chunkSize;
	}

	@Override
	public String getName() {
		return "linBugProneness" + this.initialProneness;
	}

}
