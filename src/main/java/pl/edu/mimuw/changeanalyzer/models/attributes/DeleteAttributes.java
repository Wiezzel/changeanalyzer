package pl.edu.mimuw.changeanalyzer.models.attributes;

import java.util.Arrays;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Instances;


/**
 * Attribute processor class that removes the source attributes.
 * 
 * @author Adam Wierzbicki
 */
public class DeleteAttributes implements AttributeProcessor {

	private int[] attributeIndices;
	
	/**
	 * Construct a new DeleteAttributes processor.
	 * 
	 * @param attributeIndices Indices of attributes to be removed by this processor.
	 */
	public DeleteAttributes(int[] attributeIndices) {
		this.attributeIndices = attributeIndices;
		Arrays.sort(this.attributeIndices);
	}

	@Override
	public Instances processAttributes(Instances data) throws ProcessingException {
		Instances result = new Instances(data);
		for (int i = this.attributeIndices.length - 1; i >=0; --i) {
			result.deleteAttributeAt(this.attributeIndices[i]);
		}
		return result;
	}

}
