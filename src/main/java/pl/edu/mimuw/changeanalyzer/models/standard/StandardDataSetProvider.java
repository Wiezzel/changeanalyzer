package pl.edu.mimuw.changeanalyzer.models.standard;

import pl.edu.mimuw.changeanalyzer.models.DataSetProvider;
import pl.edu.mimuw.changeanalyzer.models.attributes.Attributes;
import pl.edu.mimuw.changeanalyzer.models.measures.BugPronenessMeasure;


/**
 * Satndard data set provider uses a {@link StandardDataSetBuilder}
 * and a {@link StandardDataSetProcessor} to create & process data sets.
 * 
 * @author Adam Wierzbicki
 */
public class StandardDataSetProvider extends DataSetProvider {

	/**
	 * Cosntruct a new SatndardDataSetProvider.
	 * 
	 * @param builder	Data set builder to be used by this provider
	 * @param processor	Data set processor to be used by this provider
	 */
	private StandardDataSetProvider(StandardDataSetBuilder builder, StandardDataSetProcessor processor) {
		super(builder, processor);
	}
	
	/**
	 * Get a new StandardDataSetProvider instance (factory method).
	 * 
	 * @param measure Bug-proneness measure to be used by the provider
	 * @return A new StandardDataSetProvder using the given measure
	 */
	public static StandardDataSetProvider getInstance(BugPronenessMeasure measure) {
		StandardDataSetBuilder builder = new StandardDataSetBuilder().addMeasure(measure);
		Attributes attributes = builder.getAttributes();
		String classAttrName = measure.getName();
		StandardDataSetProcessor processor = new StandardDataSetProcessor(attributes, classAttrName);
		return new StandardDataSetProvider(builder, processor);
	}

}
