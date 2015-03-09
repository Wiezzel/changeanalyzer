package pl.edu.mimuw.changeanalyzer.models.attributes;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddExpression;


/**
 * Attribute processor which adds to the data set a new attribute being
 * the sum the processor's source attributes.
 * 
 * @author Adam Wierzbicki
 */
public class SumAttributes extends AttributeProcessor {
	
	private AddExpression filter;

	/**
	 * Construct a new SumAttributes processor.
	 * 
	 * @param sourceAttributeIndices	Indices of source attributes used for computation of a new one
	 * 									(length of this array should be at least 1)
	 * @param resultAttribute			New attribute to be computed
	 * @param inputFormat				Input format of the data to be processed
	 */
	public SumAttributes(int[] sourceAttributeIndices, Attribute resultAttribute, Instances inputFormat) {
		super(sourceAttributeIndices, resultAttribute);
		try {
			this.filter = new AddExpression();
			String expression = "a" + StringUtils.join(Arrays.stream(sourceAttributeIndices)
				.mapToObj(x -> String.valueOf(x + 1)).iterator(), "+a");
			this.filter.setExpression(expression);
			this.filter.setName(resultAttribute.name());
			filter.setInputFormat(inputFormat);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Instances processAttributes(Instances data) throws ProcessingException {
		try {
			return Filter.useFilter(data, this.filter);
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}

}
