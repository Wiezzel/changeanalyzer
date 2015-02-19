package pl.edu.mimuw.changeanalyzer.models;

import java.util.LinkedList;
import java.util.List;

import pl.edu.mimuw.changeanalyzer.exceptions.DataSetBuilderException;
import pl.edu.mimuw.changeanalyzer.extraction.CommitInfo;
import pl.edu.mimuw.changeanalyzer.models.Attributes.AttributeValues;
import pl.edu.mimuw.changeanalyzer.models.measures.BugPronenessMeasure;
import weka.core.Attribute;
import weka.core.Instance;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

/**
 * Data set builder that processes commits in chunks separated by bug-fixes. 
 * The last commit in a chunk is considered most bug-prone and every previous
 * one is assigned a lower value. Providing multiple bug-proneness scores is
 * possible by adding multiple bug-proneness measures to the builder.
 * 
 * @author Adam Wierzbicki
 */
public abstract class ChunkDataSetBuilder extends DataSetBuilder {

	private final boolean bugfixesIncluded;
	private List<Instance> resultBuffer;
	private List<BugPronenessMeasure> measures;
	
	/**
	 * Construct a new chunk data set builder.
	 * 
	 * @param bugfixesIncluded	Should the created bulder include bugfix changes in produced instances
	 */
	protected ChunkDataSetBuilder(boolean bugfixesIncluded) {
		super();
		this.bugfixesIncluded = bugfixesIncluded;
		this.resultBuffer = new LinkedList<Instance>();
		this.measures = new LinkedList<BugPronenessMeasure>();
	}
	
	@Override
	public Iterable<Instance> buildInstances(MethodHistory history) throws DataSetBuilderException {
		List<StructureEntityVersion> workingBuffer = new LinkedList<StructureEntityVersion>();
		
		CommitInfo lastFix = null;
		for (StructureEntityVersion version: history.getVersions()) {
			CommitInfo commitInfo = this.getCommitInfo(version);
			if (commitInfo == null) {
				throw new DataSetBuilderException("Commit " + version.getVersion() + " not found");
			}
			if (!commitInfo.isFix() || this.bugfixesIncluded){
				workingBuffer.add(version);
			}
			if (commitInfo.isFix()) {
				this.resetMeasures(workingBuffer);
				this.processChunk(workingBuffer, lastFix, true);
				workingBuffer = new LinkedList<StructureEntityVersion>();
				lastFix = commitInfo;
			}
		}
		
		if (!workingBuffer.isEmpty()) {
			this.resetMeasures(workingBuffer);
			this.processChunk(workingBuffer, lastFix, false);
		}
		
		return this.flushResultBuffer();
	}
	
	/**
	 * Get attribute values of a new instance.
	 * 
	 * @param version		Method version object to extract name & commit ID from
	 * @param index			Index of the given version in its chunk
	 * @param chunkIsFixed	Flag indicating if the chunk containing the given version
	 * 						is followed by a bugfix commit
	 * @return Attribute values of a new instace
	 */
	protected AttributeValues getAttrValues(StructureEntityVersion version, int index, boolean chunkIsFixed) {
		AttributeValues values = this.getAttrValues(version);
		for (BugPronenessMeasure measure: this.measures) {
			double bugProneness = chunkIsFixed ? measure.getBugProneness(index) : Instance.missingValue();
			values.setAttributeValue(measure.getName(), bugProneness);
		}
		return values;
	}
	
	/**
	 * Add new bug-proneness measure to this data set builder.
	 * 
	 * @param measure Measure to be added
	 */
	public ChunkDataSetBuilder addMeasure(BugPronenessMeasure measure) {
		this.measures.add(measure);
		this.attributes.addAttribute(new Attribute(measure.getName()));
		return this;
	}
	
	/**
	 * Notify all bug-proneness measures that processing of a new chunk begins.
	 * 
	 * @param chunk New chunk to be processed
	 */
	protected void resetMeasures(List<StructureEntityVersion> chunk) {
		for (BugPronenessMeasure measure: this.measures) {
			measure.startNewChunk(chunk);
		}
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
	 * @param lastFix	Information about last fix before this chunk
	 * 					(null if the given chunk is the fist one in a method history)
	 * @param isFixed	Is this chunk ended with a bug-fixing commit
	 */
	protected abstract void processChunk(List<StructureEntityVersion> versions, CommitInfo lastFix, boolean isFixed);
	
}
