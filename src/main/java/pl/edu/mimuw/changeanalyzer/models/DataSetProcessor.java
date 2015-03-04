package pl.edu.mimuw.changeanalyzer.models;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import pl.edu.mimuw.changeanalyzer.models.attributes.Attributes;
import weka.core.Attribute;
import weka.core.Instances;


public abstract class DataSetProcessor {
	
	protected Attributes attributes;
	protected int classIndex;
	
	protected DataSetProcessor(Attributes attributes, int classIndex) {
		this.attributes = attributes;
		this.classIndex = classIndex;
	}
	
	protected DataSetProcessor(Attributes attributes, Attribute classAttr) {
		this.attributes = attributes;
		this.classIndex = attributes.getAttributeIndex(classAttr);
	}

	protected DataSetProcessor(Attributes attributes, String classAttrName) {
		this.attributes = attributes;
		this.classIndex = attributes.getAttributeIndex(classAttrName);
	}
	
	public abstract Instances processDataSet(Instances dataSet) throws ProcessingException;

}
