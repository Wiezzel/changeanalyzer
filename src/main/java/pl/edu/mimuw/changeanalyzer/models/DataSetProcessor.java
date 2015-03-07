package pl.edu.mimuw.changeanalyzer.models;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Instances;


public interface DataSetProcessor {
	
	public abstract Instances processDataSet(Instances dataSet) throws ProcessingException;
	public abstract void setClassAttribute(Instances dataSet) throws ProcessingException;

}
