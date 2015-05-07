package pl.edu.mimuw.changeanalyzer;

import java.io.File;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.cli2.validation.FileValidator;
import org.apache.commons.cli2.validation.NumberValidator;

import pl.edu.mimuw.changeanalyzer.models.measures.BugPronenessMeasure;
import pl.edu.mimuw.changeanalyzer.models.measures.GeometricMeasure;
import pl.edu.mimuw.changeanalyzer.models.measures.LinearMeasure;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.RandomForest;
import weka.core.SelectedTag;


/**
 * Parser of command-line options for {@link ChangeAnalyzer#main(String[])}.
 * 
 * @author Adam Wierzbicki
 */
public class ChangeAnalyzerOptionParser {
	
	/**
	 * Default initial bug-proneness for {@link LinearMeasure}
	 */
	public static final double DEFAULT_INIT_PRONENESS = 0.0;
	
	/**
	 * Default bug-proneness decrease ratio for {@link GeometricMeasure}
	 */
	public static final double DEFAULT_DECR_RATIO = 0.7;
	
	private Option extract;
	private Option read;
	private Option save;
	private Option classify;
	private Option linMeasure;
	private Option geomMeasure;
	private Option decisionTree;
	private Option randomForest;
	private Option svm;
	private Option neuralNet;
	
	private Parser parser;
	private CommandLine commandLine;

	/**
	 * Construct a new ChangeAnalyzerOptionParser.
	 */
	public ChangeAnalyzerOptionParser() {
		DefaultOptionBuilder optBuilder = new DefaultOptionBuilder();
		ArgumentBuilder argBuilder = new ArgumentBuilder();
		GroupBuilder groupBuilder = new GroupBuilder();
		
		FileValidator extractPathValidator = FileValidator.getExistingDirectoryInstance();
		FileValidator readPathValidator = FileValidator.getExistingFileInstance();
		readPathValidator.setReadable(true);
		FileValidator outputPathValidator = new FileValidator();
		NumberValidator numberValidator = NumberValidator.getNumberInstance();
		numberValidator.setMinimum(0.0);
		numberValidator.setMaximum(1.0);
		
		Argument extractPath = argBuilder
				.withName("REPO_PATH")
				.withDescription("path to repository")
				.withValidator(extractPathValidator)
				.withMinimum(1)
				.withMaximum(1)
				.create();
		Argument readPath = argBuilder
				.withName("DATA_PATH")
				.withDescription("path to data file")
				.withValidator(readPathValidator)
				.withMinimum(1)
				.withMaximum(1)
				.create();
		Argument savePath = argBuilder
				.withName("DATA_PATH")
				.withDescription("path to save extracted data")
				.withValidator(outputPathValidator)
				.withMinimum(1)
				.withMaximum(1)
				.create();
		Argument classPath = argBuilder
				.withName("RESULT_PATH")
				.withDescription("path to save results")
				.withValidator(outputPathValidator)
				.withMinimum(1)
				.withMaximum(1)
				.create();
		Argument initBugProneness = argBuilder
				.withName("INIT_PRONENESS")
				.withDescription("initial bug-proneness")
				.withValidator(numberValidator)
				.withMinimum(0)
				.withMaximum(1)
				.create();
		Argument decreaseRatio = argBuilder
				.withName("DECR_RATIO")
				.withDescription("bug-proneness decrease ratio")
				.withValidator(numberValidator)
				.withMinimum(0)
				.withMaximum(1)
				.create();
		
		this.linMeasure = optBuilder
				.withLongName("linear-measure")
				.withShortName("l")
				.withDescription("Use linear bug-proneness measure")
				.withArgument(initBugProneness)
				.create();
		this.geomMeasure = optBuilder
				.withLongName("geometric-measure")
				.withShortName("g")
				.withDescription("Use geometric bug-proneness measure")
				.withArgument(decreaseRatio)
				.create();
		Group measures = groupBuilder
				.withOption(this.linMeasure)
				.withOption(this.geomMeasure)
				.withMinimum(1)
				.withMaximum(1)
				.withRequired(true)
				.create();
		
		this.extract = optBuilder
				.withLongName("extract")
				.withShortName("e")
				.withDescription("Extract data from a Git repository")
				.withArgument(extractPath)
				.withChildren(measures)
				.create();
		this.read = optBuilder
				.withLongName("read")
				.withShortName("r")
				.withDescription("Read data from an ARFF file")
				.withArgument(readPath)
				.create();
		Group inputOptions = groupBuilder
				.withOption(this.extract)
				.withOption(this.read)
				.withMinimum(1)
				.withMaximum(1)
				.withRequired(true)
				.create();
		
		this.decisionTree = optBuilder
				.withLongName("decision-tree")
				.withShortName("d")
				.withDescription("Use decision tree for classification")
				.create();
		this.randomForest = optBuilder
				.withLongName("random-forest")
				.withShortName("f")
				.withDescription("Use random forest for classification")
				.create();
		this.svm = optBuilder
				.withLongName("svm")
				.withShortName("v")
				.withDescription("Use SVM for classification")
				.create();
		this.neuralNet = optBuilder
				.withLongName("neural-net")
				.withShortName("n")
				.withDescription("Use neural net for classification")
				.create();
		Group classifiers = groupBuilder
				.withOption(this.decisionTree)
				.withOption(this.randomForest)
				.withOption(this.svm)
				.withOption(this.neuralNet)
				.withMinimum(1)
				.withMaximum(1)
				.withRequired(true)
				.create();
		
		this.save = optBuilder
				.withLongName("save")
				.withShortName("s")
				.withDescription("Save extracted data to an ARFF file")
				.withArgument(savePath)
				.create();
		this.classify = optBuilder
				.withLongName("classify")
				.withShortName("c")
				.withDescription("Classify methods and save results")
				.withArgument(classPath)
				.withChildren(classifiers)
				.create();
		Group outputOptions = groupBuilder
				.withOption(this.save)
				.withOption(this.classify)
				.withMinimum(1)
				.withMaximum(2)
				.withRequired(true)
				.create();
		
		Option help = optBuilder
				.withLongName("help")
				.withShortName("h")
				.create();
		
		Group options = groupBuilder
				.withOption(inputOptions)
				.withOption(outputOptions)
				.withOption(help)
				.create();
		
		this.parser = new Parser();
		this.parser.setGroup(options);
		this.parser.setHelpFormatter(new HelpFormatter());
		this.parser.setHelpOption(help);
	}
	
	/**
	 * Parse command-line arguments.
	 * 
	 * @param args Arguments to be parsed
	 */
	public void parse(String[] args) {
		this.commandLine = this.parser.parseAndHelp(args);
	}
	
	/**
	 * Check whether this parser has succesfully parsed arguments.
	 * 
	 * @return True iff this parser has parsed arguments
	 */
	public boolean isParsed() {
		return this.commandLine != null;
	}
	
	/**
	 * Check whether the "--extract" option has been provided in arguments
	 * parsed by this parser. 
	 * 
	 * @return True iff the "--extract" option has been provided
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public boolean hasExtractOption() {
		this.assertParsed();
		return this.commandLine.hasOption(this.extract);
	}
	
	/**
	 * Get the directory given as source for data extraction in arguments
	 * parsed by this parser.
	 * 
	 * @return Directory to extract data from
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public File getExtractDir() {
		this.assertParsed();
		return (File) this.commandLine.getValue(this.extract);
	}
	
	/**
	 * Check whether the "--read" option has been provided in arguments
	 * parsed by this parser. 
	 * 
	 * @return True iff the "--read" option has been provided
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public boolean hasReadOption() {
		this.assertParsed();
		return this.commandLine.hasOption(this.read);
	}
	
	/**
	 * Get the file given as source for reading data in arguments parsed 
	 * by this parser.
	 * 
	 * @return File to read data from
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public File getReadFile() {
		this.assertParsed();
		return (File) this.commandLine.getValue(this.read);
	}
	
	/**
	 * Check whether the "--save" option has been provided in arguments
	 * parsed by this parser. 
	 * 
	 * @return True iff the "--save" option has been provided
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public boolean hasSaveOption() {
		this.assertParsed();
		return this.commandLine.hasOption(this.save);
	}
	
	/**
	 * Get the file given as target for saving data in arguments parsed 
	 * by this parser.
	 * 
	 * @return File to save extracted data into
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public File getSaveFile() {
		this.assertParsed();
		return (File) this.commandLine.getValue(this.save);
	}
	
	/**
	 * Check whether the "--classify" option has been provided in arguments
	 * parsed by this parser. 
	 * 
	 * @return True iff the "--classify" option has been provided
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public boolean hasClassifyOption() {
		this.assertParsed();
		return this.commandLine.hasOption(this.classify);
	}
	
	/**
	 * Get the file given as target for saving method classification results
	 * in arguments parsed by this parser.
	 * 
	 * @return File to save extracted data into
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public File getResultFile() {
		this.assertParsed();
		return (File) this.commandLine.getValue(this.classify);
	}
	
	/**
	 * Get the bug-proneness measure specified in arguments parsed by this parser.
	 * 
	 * @return Specified bug-proneness measure
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public BugPronenessMeasure getMeasure() {
		this.assertParsed();
		if (this.commandLine.hasOption(this.geomMeasure)) {
			Number decreaseRatio = (Number) this.commandLine.getValue(this.geomMeasure, DEFAULT_DECR_RATIO);
			return new GeometricMeasure(decreaseRatio.doubleValue());
		}
		Number initBugProneness = (Number) this.commandLine.getValue(this.linMeasure, DEFAULT_INIT_PRONENESS);
		return new LinearMeasure(initBugProneness.doubleValue());
	}
	
	/**
	 * Get the classifier specified in arguments parsed by this parser.
	 * 
	 * @return Specified classifier
	 * @throws IllegalStateException if arguments have not been parsed
	 */
	public Classifier getClassifier() {
		this.assertParsed();
		if (this.commandLine.hasOption(this.decisionTree)) {
			return new M5P();
		} else if (this.commandLine.hasOption(this.randomForest)) {
			return new RandomForest();
		} else if (this.commandLine.hasOption(this.svm)) {
			LibSVM svm = new LibSVM();
			svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR, LibSVM.TAGS_SVMTYPE));
			return svm;
		} else if (this.commandLine.hasOption(this.neuralNet)) {
			return new MultilayerPerceptron();
		}
		return null;
	}

	/**
	 * Assert that arguments have been parsed.
	 */
	private void assertParsed() {
		if (!this.isParsed())
			throw new IllegalStateException("Arguments are not parsed");
	}
	
}
