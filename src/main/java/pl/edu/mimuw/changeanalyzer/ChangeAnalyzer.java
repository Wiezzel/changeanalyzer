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
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import weka.filters.unsupervised.attribute.RemoveType;


/**
 * ChangeAnalyzer is the main class of this project. It provides the 
 * following functions:
 * <ul>
 * 		<li>Extract data from a repository</li>
 * 		<li>Read extracted data from an ARFF file</li>
 * 		<li>Save extracted data to an ARFF file</li>
 * 		<li>Classify instances (methods) with missing class attrribute</li>
 * </ul>
 * Classification is printed in the form of tab-separated records containing
 * method headers and bug-proneness scores.
 * 
 * @author Adam Wierzbicki
 */
public class ChangeAnalyzer {

	private DataSetProvider provider;
	private Map<String, Double> results;
	private Classifier classifier;
	
	/**
	 * Construct a new ChangeAnalyzer.
	 * 
	 * @param provider		Data set provider to be used by this analyzer
	 * @param classifier	Classifier to be used by this analyzer
	 */
	public ChangeAnalyzer(DataSetProvider provider, Classifier classifier) {
		this.provider = provider;
		this.results = new HashMap<String, Double>();
		this.wrapClassifier(classifier);
	}
	
	/**
	 * Wrap the given classifier in a {@link FilteredClassifier}. This
	 * removes all String attributes from instances before putting them
	 * into the wrapped classifier.
	 * 
	 * @param classifier Classifier to be wrapped
	 */
	private void wrapClassifier(Classifier classifier) {
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setClassifier(classifier);
		filteredClassifier.setFilter(new RemoveType());
		this.classifier = filteredClassifier;
	}
	
	/**
	 * Extract data from a Git repository.
	 * 
	 * @param repoDir Directory with the repository to extract data from
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public void extractData(File repoDir) throws IOException, ChangeAnalyzerException {
		this.provider.extractDataFromRepository(repoDir);
	}
	
	/**
	 * Read previously extracted data from a file.
	 * @param dataFile File to read data from
	 * @throws IOException
	 */
	public void readData(File dataFile) throws IOException {
		this.provider.readDataFromFile(dataFile, false);
	}
	
	/**
	 * Save extracted data to a file.
	 * 
	 * @param dataFile File to save data into
	 * @throws IOException
	 */
	public void saveData(File dataFile) throws IOException {
		if (!this.provider.isDataReady()) {
			throw new IllegalStateException("No data to save");
		}
		Saver saver = new ArffSaver();
		saver.setFile(dataFile);
		saver.setInstances(this.provider.getAllInstances());
		saver.writeBatch();
	}
	
	/**
	 * Classify methods lacking class attribute (from the previously etcracted
	 * or read data). Prior to calling this method, {@link #extractData(File)}
	 * or {@link #readData(File)} should be invoked, otherwise an
	 * {@link IllegalStateException} will be thrown.
	 * 
	 * @throws ChangeAnalyzerException
	 */
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
	
	/**
	 * Save classification results to a file.
	 * 
	 * @param resultFile File to save classification into
	 * @throws FileNotFoundException
	 */
	public void saveResults(File resultFile) throws FileNotFoundException {
		PrintStream printStream = new PrintStream(resultFile);
		for (Map.Entry<String, Double> entry: this.results.entrySet()) {
			printStream.println(entry.getKey() + "\t" + entry.getValue());
		}
		printStream.close();
	}
	
	/**
	 * Get classification results.
	 * 
	 * @return	Map containing mehtod headers as keys and bug-proneness scores
	 * 			as values.
	 */
	public Map<String, Double> getResults() {
		return Collections.unmodifiableMap(this.results);
	}

	/**
	 * Main method creates a new ChangeAnalyzer and performs operations specified
	 * in arguments. To see usage, run it with "--help" argument.
	 * 
	 * @param args Command-line arguments
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		ChangeAnalyzerOptionParser parser = new ChangeAnalyzerOptionParser();
		parser.parse(args);
		if (!parser.isParsed()) {
			return;
		}
		
		DataSetProvider provider = parser.hasExtractOption()
				? StandardDataSetProvider.getInstance(parser.getMeasure())
				: new ReadOnlyDataSetProvider();
		Classifier classifier = parser.getClassifier();
		ChangeAnalyzer analyzer = new ChangeAnalyzer(provider, classifier);
		
		if (parser.hasExtractOption()) {
			analyzer.extractData(parser.getExtractDir());
		} else {
			analyzer.readData(parser.getReadFile());
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
