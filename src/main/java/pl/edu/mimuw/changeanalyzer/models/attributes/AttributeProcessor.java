package pl.edu.mimuw.changeanalyzer.models.attributes;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Attribute;
import weka.core.Instances;


/**
 * Abstract class for manipulating attributes of a data set.
 * 
 * @author Adam Wierzbicki
 */
public abstract class AttributeProcessor {
	
	protected int[] sourceAttributeIndices;
	protected Attribute resultAttribute;

	/**
	 * Construct a new AttributeProcessor.
	 * 
	 * @param sourceAttributeIndices	Indices of source attributes used by this attribute processor
	 * 									(length of this array should be at least 1)
	 * @param resultAttribute			New attribute to be computed
	 */
	protected AttributeProcessor(int[] sourceAttributeIndices, Attribute resultAttribute) {
		this.sourceAttributeIndices = sourceAttributeIndices;
		this.resultAttribute = resultAttribute;

		if (sourceAttributeIndices.length < 1) {
			throw new IllegalArgumentException("Attribute processor requires at least one argument");
		}
	}
	
	/**
	 * Get the result attribute of this processor.
	 * 
	 * @return The result attribute of this processor
	 */
	public Attribute getResultAttribute() {
		return this.resultAttribute;
	}
	
	/**
	 * Process a data set.
	 *  
	 * @param data	Input data set to be processed
	 * @return		Processed data set
	 * @throws ProcessingException
	 */
	public abstract Instances processAttributes(Instances data) throws ProcessingException;

}
