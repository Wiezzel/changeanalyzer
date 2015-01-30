package pl.edu.mimuw.changeanalyzer.models.measures;

import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


public class WeightedMeasure implements BugPronenessMeasure {
	
	private int[] numChanges;
	private int totalNumChanges;

	@Override
	public void startNewChunk(List<StructureEntityVersion> chunk) {
		this.numChanges = new int[chunk.size()];
		int i = 0;
		for (StructureEntityVersion version: chunk) {
			this.totalNumChanges += version.getSourceCodeChanges().size();
			this.numChanges[i++] = this.totalNumChanges;
		}
	}

	@Override
	public double getBugProneness(int index) {
		return (double) this.numChanges[index] / this.totalNumChanges;
	}

	@Override
	public String getName() {
		return "weightBugProneness";
	}

}
