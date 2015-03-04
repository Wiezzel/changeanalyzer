package pl.edu.mimuw.changeanalyzer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.exceptions.PredictionException;
import pl.edu.mimuw.changeanalyzer.models.DataSetProvider;
import pl.edu.mimuw.changeanalyzer.models.measures.BugPronenessMeasure;
import pl.edu.mimuw.changeanalyzer.models.measures.LinearMeasure;
import pl.edu.mimuw.changeanalyzer.models.standard.StandardDataSetProvider;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
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
	
	public void extractData(String repoPath) throws IOException, ChangeAnalyzerException {
		this.provider.extractDataFromRepository(repoPath);
	}
	
	public void readData(String dataPath) throws IOException {
		this.provider.readDataFromFile(dataPath);
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
	
	public void saveResults(String resultPath) {
		// TODO
	}
	
	public Map<String, Double> getResults() {
		return Collections.unmodifiableMap(this.results);
	}

	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		BugPronenessMeasure measure = new LinearMeasure(0.0);
		DataSetProvider provider = StandardDataSetProvider.getInstance(measure);
		Classifier classifier = new RandomForest();
		ChangeAnalyzer analyzer = new ChangeAnalyzer(provider, classifier);
		
		analyzer.readData(args[0]);
		analyzer.classifyMethods();
		for (Map.Entry<String, Double> entry: analyzer.getResults().entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}

}
