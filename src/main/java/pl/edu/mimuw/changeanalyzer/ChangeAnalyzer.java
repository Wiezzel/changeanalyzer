package pl.edu.mimuw.changeanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.exceptions.PredictionException;
import pl.edu.mimuw.changeanalyzer.models.DataSetProvider;
import pl.edu.mimuw.changeanalyzer.models.ReadOnlyDataSetProvider;
import pl.edu.mimuw.changeanalyzer.models.standard.StandardDataSetProvider;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import weka.filters.unsupervised.attribute.RemoveType;


public class ChangeAnalyzer {

	private DataSetProvider provider;
	private Map<String, Double> results;
	private Classifier classifier;
	
	public ChangeAnalyzer(DataSetProvider provider, Classifier classifier) {
		this.provider = provider;
		this.results = new HashMap<String, Double>();
		this.wrapClassifier(classifier);
	}
	
	private void wrapClassifier(Classifier classifier) {
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setClassifier(classifier);
		filteredClassifier.setFilter(new RemoveType());
		this.classifier = filteredClassifier;
	}
	
	public void extractData(File repoDir) throws IOException, ChangeAnalyzerException {
		this.provider.extractDataFromRepository(repoDir);
	}
	
	public void readData(File dataFile, boolean raw) throws IOException {
		this.provider.readDataFromFile(dataFile, raw);
	}
	
	public void saveData(File dataFile) throws IOException {
		if (!this.provider.isDataReady()) {
			throw new IllegalStateException("No data to save");
		}
		Saver saver = new ArffSaver();
		saver.setFile(dataFile);
		saver.setInstances(this.provider.getAllInstances());
		saver.writeBatch();
	}
	
	public void classifyMethods() throws ChangeAnalyzerException {
		if (!this.provider.isDataReady()) {
			throw new IllegalStateException("Data not loaded");
		}
		
		Instances trainingInstances = this.provider.getTrainingInstances();
		Instances testInstances = this.provider.getTestInstances();
		try {
			this.classifier.buildClassifier(trainingInstances);
			this.results.clear();
			for (Instance instance: testInstances) {
				String methodName = instance.stringValue(0);
				double bugProneness = this.classifier.classifyInstance(instance);
				this.results.put(methodName, bugProneness);
			}
		} catch (Exception e) {
			throw new PredictionException(e);
		}
	}
	
	public void saveResults(File resultFile) throws FileNotFoundException {
		PrintStream printStream = new PrintStream(resultFile);
		for (Map.Entry<String, Double> entry: this.results.entrySet()) {
			printStream.println(entry.getKey() + "\t" + entry.getValue());
		}
		printStream.close();
	}
	
	public Map<String, Double> getResults() {
		return Collections.unmodifiableMap(this.results);
	}

	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		ChangeAnalyzerOptionParser parser = new ChangeAnalyzerOptionParser();
		parser.parse(args);
		if (!parser.isParsed()) {
			return;
		}
		
		DataSetProvider provider = parser.hasExtractOption()
				? StandardDataSetProvider.getInstance(parser.getMeasure())
				: new ReadOnlyDataSetProvider();
		Classifier classifier = new RandomForest();
		ChangeAnalyzer analyzer = new ChangeAnalyzer(provider, classifier);
		
		if (parser.hasExtractOption()) {
			analyzer.extractData(parser.getExtractDir());
		} else {
			analyzer.readData(parser.getReadFile(), false);
		}
		
		if (parser.hasSaveOption()) {
			analyzer.saveData(parser.getSaveFile());
		}
		if (parser.hasClassifyOption()) {
			analyzer.classifyMethods();
			analyzer.saveResults(parser.getResultFile());
		}
	}

}
