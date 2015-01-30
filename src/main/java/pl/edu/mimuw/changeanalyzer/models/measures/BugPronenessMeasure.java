package pl.edu.mimuw.changeanalyzer.models.measures;

import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


public interface BugPronenessMeasure {
	
	public void startNewChunk(List<StructureEntityVersion> chunk);
	
	public double getBugProneness(int index);
	
	public String getName();

}
