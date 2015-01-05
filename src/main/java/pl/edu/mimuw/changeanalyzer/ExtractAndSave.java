package pl.edu.mimuw.changeanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.extraction.ClassHistoryWrapper;
import pl.edu.mimuw.changeanalyzer.extraction.RepoHistoryExtractor;
import pl.edu.mimuw.changeanalyzer.models.DataSetBuilder;
import pl.edu.mimuw.changeanalyzer.models.GDCGDataSetBuilder;
import pl.edu.mimuw.changeanalyzer.models.GDSCDataSetBuilder;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


public class ExtractAndSave {
	
	public static final double SC_INITIAL_PRONENESS = 0.7;
	public static final double SC_DEREASE_RATIO = 0.2;
	
	public static final double CG_DECREASE_RATIO = 0.2;
	
	private static void extractAndSave(String repoPath, String scResultPath, String cgResultPath)
			throws IOException, ChangeAnalyzerException {
		
		RepoHistoryExtractor extractor = new RepoHistoryExtractor(repoPath);
		DataSetBuilder scBuilder = new GDSCDataSetBuilder(SC_INITIAL_PRONENESS, SC_DEREASE_RATIO, false);
		DataSetBuilder cgBuilder = new GDCGDataSetBuilder(CG_DECREASE_RATIO);
		
		long startTime = System.currentTimeMillis();
		
		Map<String, ClassHistory> map =  extractor.extractClassHistories();
		ClassHistoryWrapper wrapper = new ClassHistoryWrapper(map.values());
		Iterable<RevCommit> commits = extractor.extractRelevantCommits();
		
		Instances scDataSet = scBuilder.readCommits(commits).buildDataSet("changes", wrapper);
		Instances cgDataSet = cgBuilder.readCommits(commits).buildDataSet("changes", wrapper);
		
		saveDataSet(scDataSet, scResultPath);
		saveDataSet(cgDataSet, cgResultPath);
		
		long endTime = System.currentTimeMillis();
		double execTime = ((double) (endTime - startTime)) / 1000;
		System.out.printf("Extracted data from %s into %s and %s. Execution time: %.2f s\n", 
				repoPath, scResultPath, cgResultPath, execTime);
	}
	
	private static void saveDataSet(Instances dataSet, String path) throws IOException {
		Saver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(new File(path));
		saver.writeBatch();
	}
	
	private static void safeExtractAndSave(String repoPath, String scResultPath, String cgResultPath) {
		try {
			extractAndSave(repoPath, scResultPath, cgResultPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i += 3) {
			safeExtractAndSave(args[i], args[i+1], args[i+2]);
		}
	}

}
