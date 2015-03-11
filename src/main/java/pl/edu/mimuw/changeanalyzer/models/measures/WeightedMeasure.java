package pl.edu.mimuw.changeanalyzer.models.measures;

import java.util.List;

import pl.edu.mimuw.changeanalyzer.models.standard.StandardDataSetBuilder;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * This measure assigns bug-proneness score propoportionally to the
 * number of fine-grained changes since the last bug-fix. It assigns
 * each group of commits (see {@link StandardDataSetBuilder}) a score
 * equal to the ratio of number of changes in this group to the total
 * number of changes in the whole chunk.
 * 
 * @author Adam Wierzbicki
 */
public class WeightedMeasure implements BugPronenessMeasure {
	
	private int[] numChanges;
	private int totalNumChanges;

	@Override
	public void startNewChunk(List<StructureEntityVersion> chunk) {
		this.totalNumChanges = 0;
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
