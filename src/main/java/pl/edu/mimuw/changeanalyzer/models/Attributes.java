package pl.edu.mimuw.changeanalyzer.models;

import java.util.HashMap;
import java.util.Map;

import weka.core.Attribute;
import weka.core.FastVector;


public class Attributes {
	
	private FastVector attributes;
	private Map<String, Integer> indices;
	
	public Attributes() {
		this.attributes = new FastVector();
		this.indices = new HashMap<String, Integer>();
	}
	
	public void addAttribute(Attribute attribute) {
		if (this.indices.containsKey(attribute.name())) {
			throw new IllegalArgumentException("Attribute " + attribute.name() + " already present");
		}
		this.indices.put(attribute.name(), this.attributes.size());
		this.attributes.addElement(attribute);
	}
	
	public FastVector getAttributesVector() {
		return (FastVector) this.attributes.copy();
	}
	
	public int getNumAttributes() {
		return this.attributes.size();
	}
	
	public int getAttributeIndex(String name) {
		if (!this.indices.containsKey(name)) {
			throw new IllegalArgumentException("Uknown attribute: " + name);
		}
		return this.indices.get(name);
	}
	
	public int getAttribtueIndex(Attribute attribute) {
		return this.getAttributeIndex(attribute.name());
	}
	
	public AttributeValues getNewValues() {
		return new AttributeValues();
	}
	
	public class AttributeValues {
		
		private double[] values;
		
		private AttributeValues() {
			this.values = new double[Attributes.this.attributes.size()];
		}
		
		public void setAttributeValue(int index, double value) {
			this.values[index] = value;
		}
		
		public void setAttributeValue(String name, double value) {
			int index = Attributes.this.getAttributeIndex(name);
			this.setAttributeValue(index, value);
		}
		
		public void setAttributeValue(Attribute attribute, double value) {
			int index = Attributes.this.getAttribtueIndex(attribute);
			this.setAttributeValue(index, value);
		}

		public double[] getValues() {
			return values.clone();
		}
	 
	}

}
