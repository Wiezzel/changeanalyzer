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


public class ChangeAnalyzerOptionParser {
	
	public static final double DEFAULT_INIT_PRONENESS = 0.0;
	public static final double DEFAULT_DECR_RATIO = 0.7;
	
	private Option extract;
	private Option read;
	private Option save;
	private Option classify;
	private Option linMeasure;
	private Option geomMeasure;
	
	private Parser parser;
	private CommandLine commandLine;

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
	
	public void parse(String[] args) {
		this.commandLine = this.parser.parseAndHelp(args);
	}
	
	public boolean isParsed() {
		return this.commandLine != null;
	}
	
	public boolean hasExtractOption() {
		this.assertParsed();
		return this.commandLine.hasOption(this.extract);
	}
	
	public File getExtractDir() {
		this.assertParsed();
		return (File) this.commandLine.getValue(this.extract);
	}
	
	public boolean hasReadOption() {
		this.assertParsed();
		return this.commandLine.hasOption(this.read);
	}
	
	public File getReadFile() {
		this.assertParsed();
		return (File) this.commandLine.getValue(this.read);
	}
	
	public boolean hasSaveOption() {
		this.assertParsed();
		return this.commandLine.hasOption(this.save);
	}
	
	public File getSaveFile() {
		this.assertParsed();
		return (File) this.commandLine.getValue(this.save);
	}
	
	public boolean hasClassifyOption() {
		this.assertParsed();
		return this.commandLine.hasOption(this.classify);
	}
	
	public File getResultFile() {
		this.assertParsed();
		return (File) this.commandLine.getValue(this.classify);
	}
	
	public BugPronenessMeasure getMeasure() {
		this.assertParsed();
		if (this.commandLine.hasOption(this.geomMeasure)) {
			Number decreaseRatio = (Number) this.commandLine.getValue(this.geomMeasure, DEFAULT_DECR_RATIO);
			return new GeometricMeasure(decreaseRatio.doubleValue());
		}
		Number initBugProneness = (Number) this.commandLine.getValue(this.linMeasure, DEFAULT_INIT_PRONENESS);
		return new LinearMeasure(initBugProneness.doubleValue());
	}

	private void assertParsed() {
		if (!this.isParsed())
			throw new IllegalStateException("Arguments are not parsed");
	}
	
}
