package pl.edu.mimuw.changeanalyzer.models;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Instances;


/**
 * Interface for objects processing data sets (that means modyfing or removing
 * existing features as well as computing new ones).
 * 
 * @author Adam Wierzbicki
 */
public interface DataSetProcessor {
	
	/**
	 * Process a data set.
	 * 
	 * @param dataSet Data set to be processed
	 * @return Processed data set
	 * @throws ProcessingException
	 */
	public abstract Instances processDataSet(Instances dataSet) throws ProcessingException;
	
	/**
	 * Set the class attribute of a data set.
	 * 
	 * @param dataSet Data set to set the class attributes
	 * @throws ProcessingException
	 */
	public abstract void setClassAttribute(Instances dataSet) throws ProcessingException;

}
