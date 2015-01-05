package pl.edu.mimuw.changeanalyzer.models;

import java.util.LinkedList;
import java.util.List;

import pl.edu.mimuw.changeanalyzer.exceptions.DataSetBuilderException;
import pl.edu.mimuw.changeanalyzer.extraction.CommitInfo;
import weka.core.Instance;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

/**
 * Data set builder that assumes geometric decrease (GD) of bug-proneness
 * of commits with their distance from the next bug-fix. It processes commits
 * in chunks separated by bug-fixes. The last commit in a chunk is considered
 * most bug-prone and every previous one is assigned a lower value.
 * 
 * @author Adam Wierzbicki
 */
public abstract class GDDataSetBuilder extends DataSetBuilder {

	private final double initialLevel;
	private final double ratio;
	private final boolean bugfixesIncluded;
	
	private List<Instance> resultBuffer;
	
	/**
	 * Construct a new GD data set builder.
	 * 
	 * @param initialLevel		Bug-proneness of the last commit in a chunk
	 * @param ratio				Bug-proneness decrease ratio
	 * @param bugfixesIncluded	Should the created bulder include bugfix changes in produced instances
	 */
	protected GDDataSetBuilder(double initialLevel, double ratio, boolean bugfixesIncluded) {
		super();
		this.initialLevel = initialLevel;
		this.ratio = ratio;
		this.bugfixesIncluded = bugfixesIncluded;
		this.resultBuffer = new LinkedList<Instance>();
	}
	
	@Override
	public Iterable<Instance> buildInstances(MethodHistory history) throws DataSetBuilderException {
		List<StructureEntityVersion> workingBuffer = new LinkedList<StructureEntityVersion>();
		
		for (StructureEntityVersion version: history.getVersions()) {
			CommitInfo commitInfo = this.getCommitInfo(version);
			if (commitInfo == null) {
				throw new DataSetBuilderException("Commit " + version.getVersion() + " not found");
			}
			if (!commitInfo.isFix() || this.bugfixesIncluded){
				workingBuffer.add(version);
			}
			if (commitInfo.isFix()) {
				this.processChunk(workingBuffer, true);
				workingBuffer = new LinkedList<StructureEntityVersion>();
			}
		}
		
		if (!workingBuffer.isEmpty()) {
			this.processChunk(workingBuffer, false);
		}
		
		return this.flushResultBuffer();
	}
	
	/**
	 * Flush this builder's result buffer.
	 * 
	 * @return Flushed contents of the buffer.
	 */
	private List<Instance> flushResultBuffer() {
		List<Instance> resultBuffer = this.resultBuffer;
		this.resultBuffer = new LinkedList<Instance>();
		return resultBuffer;
	}
	
	/**
	 * Get the bug-proneness value of a commit.
	 * 
	 * @param commitNum Number of a commit in its chunk (0 means last, not first)
	 * @return Bug-proneness of the commit
	 */
	protected double getBugProneness(int commitNum) {
		return this.initialLevel * Math.pow(this.ratio, commitNum);
	}
	
	/**
	 * Check whether this builder includes bugfix changes in produced instances.
	 * 
	 * @return True iff this builder includes bugfixes
	 */
	protected boolean bugfixesIncluded() {
		return this.bugfixesIncluded;
	}

	/**
	 * Append the given instance to the result buffer.
	 * 
	 * @param instance
	 */
	protected void addToResult(Instance instance) {
		this.resultBuffer.add(instance);
	}
	
	/**
	 * Process a chunk of method versions.
	 * 
	 * @param versions	Method versions to be processed
	 * @param isFixed	Is this chunk ended with a bug-fixing commit
	 */
	protected abstract void processChunk(List<StructureEntityVersion> versions, boolean isFixed);
	
}
