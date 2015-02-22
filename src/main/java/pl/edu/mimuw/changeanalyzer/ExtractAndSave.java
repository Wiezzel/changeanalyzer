package pl.edu.mimuw.changeanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.extraction.ClassHistoryWrapper;
import pl.edu.mimuw.changeanalyzer.extraction.RepoHistoryExtractor;
import pl.edu.mimuw.changeanalyzer.models.GroupDataSetBuilder;
import pl.edu.mimuw.changeanalyzer.models.measures.GeometricMeasure;
import pl.edu.mimuw.changeanalyzer.models.measures.LinearMeasure;
import pl.edu.mimuw.changeanalyzer.models.measures.WeightedMeasure;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


public class ExtractAndSave {
	
	private static void extractAndSave(File repository, String resultPath)
			throws IOException, ChangeAnalyzerException {
		
		long startTime = System.currentTimeMillis();
		
		RepoHistoryExtractor extractor = new RepoHistoryExtractor(repository);
		Map<String, ClassHistory> map =  extractor.extractClassHistories();
		ClassHistoryWrapper wrapper = new ClassHistoryWrapper(map.values());
		Iterable<RevCommit> commits = extractor.extractCommits();
		
		GroupDataSetBuilder builder = new GroupDataSetBuilder();
		Instances cgDataSet = builder
				.addMeasure(new GeometricMeasure(0.7))
				.addMeasure(new LinearMeasure(0.0))
				.addMeasure(new WeightedMeasure())
				.readCommits(commits)
				.buildDataSet("changes", wrapper);
		saveDataSet(cgDataSet, resultPath);
		
		long endTime = System.currentTimeMillis();
		double execTime = ((double) (endTime - startTime)) / 1000;
		System.out.printf("Extracted data from %s into %s. Execution time: %.2f s%n", 
				repository.getAbsolutePath(), resultPath, execTime);
	}
	
	private static void saveDataSet(Instances dataSet, String path) throws IOException {
		Saver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(new File(path));
		saver.writeBatch();
	}
	
	private static void safeExtractAndSave(File repository, String resultPath) {
		try {
			extractAndSave(repository, resultPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		for (int i = 0; i < args.length; ++i) {
			File repository = new File(args[i]);
			String resultPath = repository.getName() + ".arff";
			safeExtractAndSave(repository, resultPath);
		}
	}

}
