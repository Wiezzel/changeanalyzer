package pl.edu.mimuw.changeanalyzer.models;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.extraction.CommitInfo;
import pl.edu.mimuw.changeanalyzer.extraction.CommitInfoExtractor;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;


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
	}
	
	protected Map<String, CommitInfo> commits;
	protected CommitInfoExtractor extractor;
	
	/**
	 * Default constructor.
	 */
	public DataSetBuilder() {
		this.commits = new HashMap<String, CommitInfo>();
		this.extractor = new CommitInfoExtractor();
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
	 * Read commits & store information needed for processing method histories.
	 * This method should be called before invoking buildInstances or buildDataSet,
	 * otherwise an error will occur. 
	 * 
	 * @param commits An iterable of commits to be read
	 * @return This object
	 */
	public DataSetBuilder readCommits(Iterable<RevCommit> commits) {
		this.commits.clear();
		for (RevCommit commit: commits) {
			CommitInfo commitInfo = this.extractor.extractCommitInfo(commit);
			this.commits.put(commitInfo.getId(), commitInfo);
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
	 */
	public abstract Iterable<Instance> buildInstances(MethodHistory history);
	
	/**
	 * Build model instances from given method histories. Prior to calling this
	 * method, builder should be supplied with all commits referenced by this
	 * methods' histories.
	 * 
	 * @param histories Method histories to build instaces from
	 * @return An iterable of model instances
	 */
	public Iterable<Instance> buildInstances(Iterable<MethodHistory> histories) {
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
	 * @param name Name for the data set
	 * @param histories Method histories to build instaces from
	 * @return Data set containing instaces built from the given method histories
	 */
	public Instances buildDataSet(String name, Iterable<MethodHistory> histories) {
		Instances dataSet = new Instances(name, this.getAttributes(), 0);
		for (Instance instance: this.buildInstances(histories)) {
			dataSet.add(instance);
		}
		return dataSet;
	}

}
