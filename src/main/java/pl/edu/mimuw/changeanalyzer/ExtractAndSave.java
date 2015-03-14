package pl.edu.mimuw.changeanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.extraction.ClassHistoryWrapper;
import pl.edu.mimuw.changeanalyzer.extraction.RepoHistoryExtractor;
import pl.edu.mimuw.changeanalyzer.models.measures.GeometricMeasure;
import pl.edu.mimuw.changeanalyzer.models.measures.LinearMeasure;
import pl.edu.mimuw.changeanalyzer.models.measures.WeightedMeasure;
import pl.edu.mimuw.changeanalyzer.models.standard.StandardDataSetBuilder;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


/**
 * Class for performing classifier evaluation experiments. It extracts
 * from repositories and saves unprocessed data sets.
 * 
 * @author Adam Wierzbicki
 */
public class ExtractAndSave {
	
	/**
	 * Extract data from a repository and save it under the given path.
	 * 
	 * @param repository Directory to extract data from
	 * @param resultPath Path to save the extracted data
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	private static void extractAndSave(File repository, String resultPath)
			throws IOException, ChangeAnalyzerException {
		
		long startTime = System.currentTimeMillis();
		
		RepoHistoryExtractor extractor = new RepoHistoryExtractor(repository);
		Map<String, ClassHistory> map =  extractor.extractClassHistories();
		ClassHistoryWrapper wrapper = new ClassHistoryWrapper(map.values());
		Iterable<RevCommit> commits = extractor.extractCommits();
		
		StandardDataSetBuilder builder = new StandardDataSetBuilder();
		Instances cgDataSet = builder
				.addMeasure(new GeometricMeasure(0.5))
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
	
	/**
	 * Save a data set.
	 * 
	 * @param dataSet	Data set to be saved
	 * @param path		Path to save the data det
	 * @throws IOException
	 */
	private static void saveDataSet(Instances dataSet, String path) throws IOException {
		Saver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(new File(path));
		saver.writeBatch();
	}
	
	/**
	 * Run {@link #extractAndSave(File, String)} method, catching all exceptions
	 * and printing the to the stderr.
	 * 
	 * @param repository
	 * @param resultPath
	 */
	private static void safeExtractAndSave(File repository, String resultPath) {
		try {
			extractAndSave(repository, resultPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Extract data from repositories and save it. Extract data contains
	 * three separate bug-proneness measures: linBugProneness0.0, geomBugProneness0.5
	 * and weightBugProneness. For each repository the extracted data will saved to
	 * file with the same name as repository main folder's name (with .arff extension).
	 *  
	 * @param args Paths to repositories
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		for (int i = 0; i < args.length; ++i) {
			File repository = new File(args[i]);
			String resultPath = repository.getName() + ".arff";
			safeExtractAndSave(repository, resultPath);
		}
	}

}
