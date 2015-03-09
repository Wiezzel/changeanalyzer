package pl.edu.mimuw.changeanalyzer.models;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.extraction.ClassHistoryWrapper;
import pl.edu.mimuw.changeanalyzer.extraction.RepoHistoryExtractor;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


/**
 * DataSetProvider is a top-level class reponsible for retrieving data sets
 * either by extracting them from reposiitories or by reading files with
 * previously extracted data. It wraps functionalities provided by
 * a {@link DataSetBuilder} and a {@link DataSetProcessor}.
 * 
 * @author Adam Wierzbicki
 */
public class DataSetProvider {
	
	private RepoHistoryExtractor extractor;
	private DataSetBuilder builder;
	private DataSetProcessor processor;
	private Instances dataSet;
	
	/**
	 * Construct a new DatSetProvider.
	 * 
	 * @param builder	Data set builder to be used for building new data sets
	 * @param processor Data set processor to be used for processing data sets 
	 */
	public DataSetProvider(DataSetBuilder builder, DataSetProcessor processor) {
		this.builder = builder;
		this.processor = processor;
	}

	/**
	 * Extract data from a Git repository.
	 * 
	 * @param repository Repository to extract data from
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public void extractDataFromRepository(Repository repository) throws IOException, ChangeAnalyzerException {
		this.extractor = new RepoHistoryExtractor(repository);
		this.extractData();
	}
	
	/**
	 * Extract data from a Git repository.
	 * 
	 * @param repoDir Directory with the repository to extract data from
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public void extractDataFromRepository(File repoDir) throws IOException, ChangeAnalyzerException {
		this.extractor = new RepoHistoryExtractor(repoDir);
		this.extractData();
	}
	
	/**
	 * Extract data from a Git repository.
	 * 
	 * @param repoPath Path to the repository to extract data from
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public void extractDataFromRepository(String repoPath) throws IOException, ChangeAnalyzerException {
		this.extractor = new RepoHistoryExtractor(repoPath);
		this.extractData();
	}
	
	/**
	 * Extract data from a Git repository using the current extractor
	 * (which embeds the repository).
	 * 
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	private void extractData() throws IOException, ChangeAnalyzerException {
		Map<String, ClassHistory> classHistoryMap = this.extractor.extractClassHistories();
		ClassHistoryWrapper histories = new ClassHistoryWrapper(classHistoryMap.values());
		Iterable<RevCommit> commits = this.extractor.extractCommits();
		
		this.dataSet = this.builder.readCommits(commits).buildDataSet("", histories);
		this.dataSet = this.processor.processDataSet(this.dataSet);
	}
	
	/**
	 * Read previously extracted data from a file.
	 * 
	 * @param dataPath	Path to the file to read data from
	 * @param raw		Is the data in the file unprocessed (and should therefore be processed
	 * 					after reading it)
	 * @throws IOException
	 */
	public void readDataFromFile(String dataPath, boolean raw) throws IOException {
		try {
			DataSource dataSource = new DataSource(dataPath);
			this.dataSet = dataSource.getDataSet();
			this.processor.setClassAttribute(this.dataSet);
			if (raw) {
				this.dataSet = this.processor.processDataSet(this.dataSet);
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Read previously extracted data from a file.
	 * 
	 * @param dataFile	File to read data from
	 * @param raw		Is the data in the file unprocessed (and should therefore be processed
	 * 					after reading it)
	 * @throws IOException
	 */
	public void readDataFromFile(File dataFile, boolean raw) throws IOException {
		this.readDataFromFile(dataFile.getAbsolutePath(), raw);
	}
	
	/**
	 * Check if this provider has read data and can yield it.
	 * @return True iff this provider has data
	 */
	public boolean isDataReady() {
		return this.dataSet != null;
	}
	
	/**
	 * Get all instances from this provider's data set.
	 * 
	 * @return All instances (null if data has not been read)
	 */
	public Instances getAllInstances() {
		if (!this.isDataReady()) {
			return null;
		}
		return new Instances(this.dataSet);
	}
	
	/**
	 * Get training instances from this provider's data set.
	 * (An instance belongs to the training set if it has the class attribute.)
	 * 
	 * @return Training instances (nulll if data has not been read)
	 */
	public Instances getTrainingInstances() {
		if (!this.isDataReady()) {
			return null;
		}
		Instances trainingInstances = new Instances(this.dataSet);
		Attribute classAttribute = this.dataSet.classAttribute();
		trainingInstances.deleteWithMissing(classAttribute);
		return trainingInstances;
	}
	
	/**
	 * Get test instances from this provider's data set.
	 * (An instance belongs to the test set if it lacks the class attribute.)
	 * 
	 * @return Test instance (null if data has not been read)
	 */
	public Instances getTestInstances() {
		if (!this.isDataReady()) {
			return null;
		}
		RemoveWithValues filter = new RemoveWithValues();
		int classIndex = this.dataSet.classIndex();
		filter.setAttributeIndex(String.valueOf(classIndex));
		filter.setMatchMissingValues(true);
		try {
			filter.setInputFormat(dataSet);
			Instances testInstances = Filter.useFilter(this.dataSet, filter);
			return testInstances;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
