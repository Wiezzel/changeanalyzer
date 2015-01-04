package pl.edu.mimuw.changeanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.extraction.ClassHistoryWrapper;
import pl.edu.mimuw.changeanalyzer.extraction.RepoHistoryExtractor;
import pl.edu.mimuw.changeanalyzer.models.GDSCDataSetBuilder;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


public class ExtractAndSave {
	
	public static final double INITIAL_PRONENESS = 0.7;
	public static final double DEREASE_RATIO = 0.2;
	
	private static void extractAndSave(String repoPath, String resultPath)
			throws IOException, ChangeAnalyzerException {
		
		RepoHistoryExtractor extractor = new RepoHistoryExtractor(repoPath);
		GDSCDataSetBuilder builder = new GDSCDataSetBuilder(INITIAL_PRONENESS, DEREASE_RATIO, false);
		
		long startTime = System.currentTimeMillis();
		
		Map<String, ClassHistory> map =  extractor.extractClassHistories();
		ClassHistoryWrapper wrapper = new ClassHistoryWrapper(map.values());
		Iterable<RevCommit> commits = extractor.extractRelevantCommits();
		Instances dataSet = builder.readCommits(commits).buildDataSet("changes", wrapper);
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(new File(resultPath));
		saver.writeBatch();
		
		long endTime = System.currentTimeMillis();
		double execTime = ((double) (endTime - startTime)) / 1000;
		System.out.printf("Extracted data from %s into %s. Execution time: %.2f s\n", repoPath, resultPath, execTime);
	}
	
	private static void safeExtractAndSave(String repoPath, String resultPath) {
		try {
			extractAndSave(repoPath, resultPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i += 2) {
			safeExtractAndSave(args[i], args[i+1]);
		}
	}

}
