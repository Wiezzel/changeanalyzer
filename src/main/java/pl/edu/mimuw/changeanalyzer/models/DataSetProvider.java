package pl.edu.mimuw.changeanalyzer.models;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import pl.edu.mimuw.changeanalyzer.extraction.ClassHistoryWrapper;
import pl.edu.mimuw.changeanalyzer.extraction.RepoHistoryExtractor;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


public class DataSetProvider {
	
	private RepoHistoryExtractor extractor;
	private DataSetBuilder builder;
	private DataSetProcessor processor;
	private Instances dataSet;
	
	public DataSetProvider(DataSetBuilder builder, DataSetProcessor processor) {
		this.builder = builder;
		this.processor = processor;
	}

	public void extractDataFromRepository(Repository repository) throws IOException, ChangeAnalyzerException {
		this.extractor = new RepoHistoryExtractor(repository);
		this.extractData();
	}
	
	public void extractDataFromRepository(File repoDir) throws IOException, ChangeAnalyzerException {
		this.extractor = new RepoHistoryExtractor(repoDir);
		this.extractData();
	}
	
	public void extractDataFromRepository(String repoPath) throws IOException, ChangeAnalyzerException {
		this.extractor = new RepoHistoryExtractor(repoPath);
		this.extractData();
	}
	
	private void extractData() throws IOException, ChangeAnalyzerException {
		Map<String, ClassHistory> classHistoryMap = this.extractor.extractClassHistories();
		ClassHistoryWrapper histories = new ClassHistoryWrapper(classHistoryMap.values());
		Iterable<RevCommit> commits = this.extractor.extractCommits();
		
		this.dataSet = this.builder.readCommits(commits).buildDataSet("", histories);
		this.dataSet = this.processor.processDataSet(this.dataSet);
	}
	
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
	
	public void readDataFromFile(File dataFile, boolean raw) throws IOException {
		this.readDataFromFile(dataFile.getAbsolutePath(), raw);
	}
	
	public boolean isDataReady() {
		return this.dataSet != null;
	}
	
	public Instances getAllInstances() {
		return new Instances(this.dataSet);
	}
	
	public Instances getTrainingInstances() {
		if (!this.isDataReady()) {
			return null;
		}
		Instances trainingInstances = new Instances(this.dataSet);
		Attribute classAttribute = this.dataSet.classAttribute();
		trainingInstances.deleteWithMissing(classAttribute);
		return trainingInstances;
	}
	
	public Instances getTestInstances() throws ProcessingException {
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
			throw new ProcessingException(e);
		}
	}

}
