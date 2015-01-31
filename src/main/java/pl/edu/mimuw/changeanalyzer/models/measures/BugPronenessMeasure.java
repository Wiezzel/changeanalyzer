package pl.edu.mimuw.changeanalyzer.models.measures;

import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * Bug-proneness measure is an interface for assigning bug-proneness
 * scores to model instances. It could be state-aware in a sense that each
 * assigned score does not depend solely on properties of a signle
 * method version, but of a whole chunk. Therefore, before processing
 * a new chunk the startNewChunk method should be called.
 * 
 * @author Adam Wierzbicki
 */
public interface BugPronenessMeasure {
	
	/**
	 * Start a new chunk. This method should be invoked before processing
	 * each chunk of commits, otherwise an undesired behavior can occurr.
	 * 
	 * @param chunk
	 */
	public void startNewChunk(List<StructureEntityVersion> chunk);
	
	/**
	 * Get the bug-proneness score of a commit.
	 * 
	 * @param index Index of a commit in its chunks (numbered from 0) 
	 * @return Bug-proneness of the commit with the given index
	 */
	public double getBugProneness(int index);
	
	/**
	 * Get the name of this measure.
	 * 
	 * @return Name of this measure
	 */
	public String getName();

}
