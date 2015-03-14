package pl.edu.mimuw.changeanalyzer.models.attributes;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Instances;


/**
 * Interface for manipulating attributes of a data set.
 * 
 * @author Adam Wierzbicki
 */
public interface AttributeProcessor {
	
	/**
	 * Process a data set.
	 *  
	 * @param data	Input data set to be processed
	 * @return		Processed data set
	 * @throws ProcessingException
	 */
	public abstract Instances processAttributes(Instances data) throws ProcessingException;

}
