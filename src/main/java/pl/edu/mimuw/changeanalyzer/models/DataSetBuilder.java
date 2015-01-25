package pl.edu.mimuw.changeanalyzer.models;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.exceptions.DataSetBuilderException;
import pl.edu.mimuw.changeanalyzer.extraction.AuthorInfo;
import pl.edu.mimuw.changeanalyzer.extraction.AuthorInfoExtractor;
import pl.edu.mimuw.changeanalyzer.extraction.CommitInfo;
import pl.edu.mimuw.changeanalyzer.extraction.CommitInfoExtractor;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * Abstract base class for data set builders - classes tranforming method
 * histories into model instances. Each builder class defines a set of attributes
 * and provides methods to produce instances with this attributes. Created 
 * instaces may represent commits, groups of commits, method snapshots, etc.
 * depending on the model used by a concrete builder.
 * 
 * @author Adam Wierzbicki
 */
public abstract class DataSetBuilder {
	
	protected static final FastVector BASIC_ATTRS = new FastVector();
	
	protected static final Attribute METHOD_NAME = new Attribute("methodName", (FastVector) null);
	protected static final Attribute COMMIT_ID = new Attribute("commitId", (FastVector) null);
	protected static final Attribute BUG_PRONENESS = new Attribute("bugProneness");
	
	static {
		BASIC_ATTRS.addElement(METHOD_NAME);
		BASIC_ATTRS.addElement(COMMIT_ID);
		BASIC_ATTRS.addElement(BUG_PRONENESS);
		for (ChangeType changeType: ChangeType.values()) {
			BASIC_ATTRS.addElement(new Attribute(changeType.name()));
		}
	}
	
	protected CommitInfoExtractor commitExtractor;
	protected AuthorInfoExtractor authorExtractor;
	protected ChangeCounter changeCounter;
	
	/**
	 * Default constructor.
	 */
	public DataSetBuilder() {
		this.commitExtractor = new CommitInfoExtractor();
		this.authorExtractor = new AuthorInfoExtractor();
		this.changeCounter = new ChangeCounter();
	}
	
	/**
	 * Get attributes of instances produced by this builder.
	 * 
	 * @return A vector with attributes
	 */
	public FastVector getAttributes() {
		return BASIC_ATTRS;
	}
	
	/**
	 * Get the number of attributes of instances produced by this builder.
	 * 
	 * @return Number of attributes
	 */
	public int getNumAttrs() {
		return this.getAttributes().size();
	}
	
	/**
	 * Read commits & store information needed for processing method histories.
	 * This method should be called before invoking buildInstances or buildDataSet,
	 * otherwise an error will occur. 
	 * 
	 * @param commits An iterable of commits to be read
	 * @return This object
	 */
	public DataSetBuilder readCommits(Iterable<RevCommit> commits) {
		for (RevCommit commit: commits) {
			this.commitExtractor.extractCommitInfo(commit);
		}
		return this;
	}
	
	/**
	 * Build model instances from a given method history. Prior to calling this
	 * method, builder should be supplied with all commits referenced by this
	 * method's history.
	 * 
	 * @param history Method history to build instaces from
	 * @return An iterable of model instances
	 * @throws DataSetBuilderException If a commit referenced in the method history is not found
	 */
	public abstract Iterable<Instance> buildInstances(MethodHistory history) throws DataSetBuilderException;
	
	/**
	 * Build model instances from given method histories. Prior to calling this
	 * method, builder should be supplied with all commits referenced by this
	 * methods' histories.
	 * 
	 * @param histories Method histories to build instaces from
	 * @return An iterable of model instances
	 * @throws DataSetBuilderException If a commit referenced in the method history is not found
	 */
	public Iterable<Instance> buildInstances(Iterable<MethodHistory> histories) throws DataSetBuilderException {
		for (MethodHistory history: histories) {
			for (StructureEntityVersion version: history.getVersions()) {
				this.commitExtractor.updateNumChanges(version);
			}
		}
		
		for (CommitInfo commitInfo: this.commitExtractor.getAllCommitInfos()) {
			this.authorExtractor.updateAuthorInfo(commitInfo);
		}
		
		List<Instance> instances = new LinkedList<Instance>();
		for (MethodHistory history: histories) {
			for (Instance instance: this.buildInstances(history)) {
				instances.add(instance);
			}
		}
		return instances;
		
		// TODO: Lazy evaluation
	}
	
	/**
	 * Build Weka-compatible data set containing model instances created from given 
	 * method histories. Prior to calling this method, builder should be supplied with 
	 * all commits referenced by this methods' histories.
	 * 
	 * @param name		Name for the data set
	 * @param histories	Method histories to build instaces from
	 * @return Data set containing instaces built from the given method histories
	 * @throws DataSetBuilderException If a commit referenced in the method history is not found
	 */
	public Instances buildDataSet(String name, Iterable<MethodHistory> histories) throws DataSetBuilderException {
		Instances dataSet = new Instances(name, this.getAttributes(), 0);
		for (Instance instance: this.buildInstances(histories)) {
			dataSet.add(instance);
		}
		return dataSet;
	}
	
	/**
	 * Get an array of attribute values for an instance.
	 * 
	 * @param version		Method version object to extract name & commit ID from
	 * @param bugProneness	Bug proneness of the instance
	 * @param changeCounts	Array with counts of changes of different types
	 * @return Array with the given attribute values
	 */
	protected double[] getAttrValues(StructureEntityVersion version, double bugProneness, int[] changeCounts) {
		double[] values = new double[this.getNumAttrs()];
		values[0] = METHOD_NAME.addStringValue(version.getUniqueName());
		values[1] = COMMIT_ID.addStringValue(version.getVersion());
		values[2] = bugProneness;
		
		for (int i = 0; i < changeCounts.length; ++i) {
			values[i+3] = changeCounts[i];
		}
		
		return values;
	}
	
	/**
	 * Create a single model instance. 
	 * 
	 * @param version		Method version object to extract name & commit ID from
	 * @param bugProneness	Bug proneness of the instance
	 * @param changeCounts	Array with counts of changes of different types
	 * @return Instance with the given attribute values
	 */
	protected Instance createInstance(StructureEntityVersion version, double bugProneness, int[] changeCounts) {
		double[] attrValues = this.getAttrValues(version, bugProneness, changeCounts);
		return new Instance(1.0, attrValues);
	}
	
	/**
	 * Get extracted information about commit containing a given method version.
	 * 
	 * @param version Method version to find commit information about
	 * @return Information about the commit containing the given method version
	 */
	protected CommitInfo getCommitInfo(StructureEntityVersion version) {
		return this.commitExtractor.getCommitInfo(version.getVersion());
	}
	
	/**
	 * Get extracted information about a given author.
	 * 
	 * @param name Name of the author to get information about
	 * @return Informationa about the author with the given name
	 */
	protected AuthorInfo getAuthorInfo(String name) {
		return this.authorExtractor.getAuthorInfo(name);
	}

}
