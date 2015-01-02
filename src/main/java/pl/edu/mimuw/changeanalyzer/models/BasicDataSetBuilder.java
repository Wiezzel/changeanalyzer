package pl.edu.mimuw.changeanalyzer.models;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pl.edu.mimuw.changeanalyzer.extraction.CommitInfo;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

/**
 * Basic data set builder. It uses single commit as model and includes numbers
 * of changes by type as features (this means one feature for each change type
 * defined in {@link ChangeType}). Bug proneness is assigned in the following
 * way: last commit before a bug-fix is assumed to be 50% bug prone, previous 
 * one 25%, etc.
 * 
 * @author Adam Wierzbicki
 */
public class BasicDataSetBuilder extends DataSetBuilder {
	
	/**
	 * Bug-proneness of the last commit before fix.
	 */
	public static final double LATEST_COMMIT_RELEVANCE = 0.5;
	
	private static final FastVector ATTRIBUTES = new FastVector();
	
	static {
		ATTRIBUTES.appendElements(BASIC_ATTRS);
		for (ChangeType changeType: ChangeType.values()) {
			ATTRIBUTES.addElement(new Attribute(changeType.name()));
		}
	}

	/**
	 * Create a basic data set builder.
	 */
	public BasicDataSetBuilder() {
		super();
	}
	
	@Override
	public Iterable<Instance> buildInstances(MethodHistory history) {
		List<Instance> result = new LinkedList<Instance>();
		Queue<StructureEntityVersion> buffer = new LinkedList<StructureEntityVersion>();
		
		for (StructureEntityVersion version: history.getVersions()) {
			CommitInfo commitInfo = this.commits.get(version.getVersion());
			if (commitInfo == null) {
				throw new IllegalStateException("Commit " + version.getVersion() + " not found");
			}
			
			if (commitInfo.isFix()) {
				int n = buffer.size() - 1;
				for (int i = n; i >= 0; --i) {
					StructureEntityVersion version1 = buffer.remove();
					double bugProneness = Math.pow(1.0 - LATEST_COMMIT_RELEVANCE, i)
							* (i < n ? LATEST_COMMIT_RELEVANCE : 1.0);
					Instance instance = this.createInstance(version1, bugProneness);
					result.add(instance);
				}
				
				// Bugfix is considered to be non-bug prone
				Instance instance = this.createInstance(version, 0.0);
				result.add(instance);
			}
			else {
				buffer.add(version);
			}
		}
		
		while (!buffer.isEmpty()) {
			StructureEntityVersion version = buffer.remove();
			Instance instance = this.createInstance(version, Instance.missingValue());
			result.add(instance);
		}
		
		return result;
	}
	
	private Instance createInstance(StructureEntityVersion version, double bugProneness) {
		double[] values = new double[ATTRIBUTES.size()];
		values[0] = METHOD_NAME.addStringValue(version.getUniqueName());
		values[1] = COMMIT_ID.addStringValue(version.getVersion());
		values[2] = bugProneness;
		
		ChangeCounter counter = new ChangeCounter();
		int[] counts = counter.countChanges(version);
		for (int i = 0; i < counts.length; ++i) {
			values[i+3] = counts[i];
		}
		
		return new Instance(1, values);
	}
	
}
