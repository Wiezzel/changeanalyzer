package pl.edu.mimuw.changeanalyzer.models.attributes;

import java.util.Arrays;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Instances;


/**
 * Attribute processor class that removes the source attributes.
 * 
 * @author Adam Wierzbicki
 */
public class DeleteAttributes extends AttributeProcessor {

	/**
	 * Construct a new DeleteAttributes processor.
	 * 
	 * @param sourceAttributeIndices Indices of attributes to be removed by this processor.
	 */
	public DeleteAttributes(int[] sourceAttributeIndices) {
		super(sourceAttributeIndices, null);
		Arrays.sort(this.sourceAttributeIndices);
	}

	@Override
	public Instances processAttributes(Instances data) throws ProcessingException {
		Instances result = new Instances(data);
		for (int i = this.sourceAttributeIndices.length - 1; i >=0; --i) {
			result.deleteAttributeAt(this.sourceAttributeIndices[i]);
		}
		return result;
	}

}
