package pl.edu.mimuw.changeanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * This class groups fine-grained code changes by method name & commit ID
 * and combines each group into a single record.
 */
public class DataSetBuilder {
	
	private static FastVector attributes;
	
	private static Attribute methodName = new Attribute("methodName", (FastVector) null);
	private static Attribute commitId = new Attribute("commitId", (FastVector) null);
	private static Attribute bugProneness = new Attribute("bugProneness");
	
	private static final double LATEST_COMMIT_RELEVANCE = 0.5;
	
	static {
		attributes = new FastVector();
		attributes.addElement(methodName);
		attributes.addElement(commitId);
		attributes.addElement(bugProneness);
		for (ChangeType changeType: ChangeType.values()) {
			attributes.addElement(new Attribute(changeType.name()));
		}
	}

	private Map<String, CommitInfo> commits;
	private CommitInfoExtractor extractor;
	
	public DataSetBuilder() {
		this.commits = new HashMap<String, CommitInfo>();
		this.extractor = new CommitInfoExtractor();
	}
	
	private void readCommits(Iterable<RevCommit> commits) {
		this.commits.clear();
		for (RevCommit commit: commits) {
			CommitInfo commitInfo = this.extractor.extractCommitInfo(commit);
			this.commits.put(commitInfo.getId(), commitInfo);
		}
	}
	
	private Iterable<Instance> readMethodHistory(MethodHistory history) {
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
		double[] values = new double[attributes.size()];
		values[0] = methodName.addStringValue(version.getUniqueName());
		values[1] = commitId.addStringValue(version.getVersion());
		values[2] = bugProneness;
		
		ChangeCounter counter = new ChangeCounter();
		int[] counts = counter.countChanges(version);
		for (int i = 0; i < counts.length; ++i) {
			values[i+3] = counts[i];
		}
		
		return new Instance(1, values);
	}
	
	public Instances buildDataSet(Iterable<RevCommit> commits, Iterable<MethodHistory> histories) {
		this.readCommits(commits);
		Instances dataSet = new Instances("changes", attributes, 0);
		for (MethodHistory history: histories) {
			for (Instance instance: this.readMethodHistory(history)) {
				dataSet.add(instance);
			}
		}
		return dataSet;
	}
	
	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		RepoHistoryExtractor extractor = new RepoHistoryExtractor("C:\\jgit");
		DataSetBuilder builder = new DataSetBuilder();
		
		long startTime = System.currentTimeMillis();
		
		Map<String, ClassHistory> map =  extractor.extractClassHistories();
		ClassHistoryWrapper wrapper = new ClassHistoryWrapper(map.values());
		Iterable<RevCommit> commits = extractor.extractRelevantCommits();
		Instances dataSet = builder.buildDataSet(commits, wrapper);
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(new File("jgit_data.arff"));
		saver.writeBatch();
		
		long endTime = System.currentTimeMillis();
		double execTime = ((double) (endTime - startTime)) / 1000;
		System.out.println("Execution time: " + execTime + " s");
	}
	
}
