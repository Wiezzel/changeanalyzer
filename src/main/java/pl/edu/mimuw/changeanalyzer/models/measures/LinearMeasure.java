package pl.edu.mimuw.changeanalyzer.models.measures;

import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


public class LinearMeasure implements BugPronenessMeasure {

	private double initialProneness;
	private int chunkSize;
	
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
