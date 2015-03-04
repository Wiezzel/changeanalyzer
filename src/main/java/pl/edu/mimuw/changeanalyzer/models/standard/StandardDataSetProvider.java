package pl.edu.mimuw.changeanalyzer.models.standard;

import pl.edu.mimuw.changeanalyzer.models.DataSetBuilder;
import pl.edu.mimuw.changeanalyzer.models.DataSetProcessor;
import pl.edu.mimuw.changeanalyzer.models.DataSetProvider;
import pl.edu.mimuw.changeanalyzer.models.attributes.Attributes;
import pl.edu.mimuw.changeanalyzer.models.measures.BugPronenessMeasure;


public class StandardDataSetProvider extends DataSetProvider {

	private StandardDataSetProvider(DataSetBuilder builder, DataSetProcessor processor) {
		super(builder, processor);
	}
	
	public static StandardDataSetProvider getInstance(BugPronenessMeasure measure) {
		DataSetBuilder builder = new StandardDataSetBuilder().addMeasure(measure);
		Attributes attributes = builder.getAttributes();
		String classAttrName = measure.getName();
		DataSetProcessor processor = new StandardDataSetProcessor(attributes, classAttrName);
		return new StandardDataSetProvider(builder, processor);
	}

}
