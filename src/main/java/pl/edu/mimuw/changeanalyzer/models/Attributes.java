package pl.edu.mimuw.changeanalyzer.models;

import java.util.HashMap;
import java.util.Map;

import weka.core.Attribute;
import weka.core.FastVector;


/**
 * Class representing a set of model attributes (see {@link Attribute}).
 * 
 * @author Adam Wierzbicki
 */
public class Attributes {
	
	private FastVector attributes;
	private Map<String, Integer> indices;
	
	/**
	 * Create new Attributes.
	 */
	public Attributes() {
		this.attributes = new FastVector();
		this.indices = new HashMap<String, Integer>();
	}
	
	/**
	 * Add a new attribute to this set. This method will throw an exception
	 * if the given attribute is already present.
	 * 
	 * @param attribute Attribute to be added
	 */
	public void addAttribute(Attribute attribute) {
		if (this.indices.containsKey(attribute.name())) {
			throw new IllegalArgumentException("Attribute " + attribute.name() + " already present");
		}
		this.indices.put(attribute.name(), this.attributes.size());
		this.attributes.addElement(attribute);
	}
	
	/**
	 * Get a vector with all the attributes in this set.
	 * 
	 * @return Fast vector with attributes 
	 */
	public FastVector getAttributesVector() {
		return (FastVector) this.attributes.copy();
	}
	
	/**
	 * Get the number of attributes in this set
	 * 
	 * @return Number of attributes
	 */
	public int getNumAttributes() {
		return this.attributes.size();
	}
	
	/**
	 * Get the numeric index of an attribute.
	 * 
	 * @param name Name of the attribute to get index of
	 * @return Index of the attribute with the given name
	 */
	public int getAttributeIndex(String name) {
		if (!this.indices.containsKey(name)) {
			throw new IllegalArgumentException("Uknown attribute: " + name);
		}
		return this.indices.get(name);
	}
	
	/**
	 * Get the numeric index of an attribute.
	 * 
	 * @param attribute Attribute to get index of
	 * @return Index of the given attribute
	 */
	public int getAttribtueIndex(Attribute attribute) {
		return this.getAttributeIndex(attribute.name());
	}
	
	/**
	 * Get a new, empty {@link AttributeValues} object bound to
	 * this attribute set.
	 * 
	 * @return
	 */
	public AttributeValues getNewValues() {
		return new AttributeValues();
	}
	
	/**
	 * Class for storing attribute values. Each instance is bound to
	 * a {@link Attributes} object and only values of fields present in that
	 * object are available.
	 * 
	 * @author Adam Wierzbicki
	 */
	public class AttributeValues {
		
		private double[] values;
		
		/**
		 * Construct new AttributeValues.
		 */
		private AttributeValues() {
			this.values = new double[Attributes.this.attributes.size()];
		}
		
		/**
		 * Set the value of a given attribute.
		 * 
		 * @param index	Index of the attribute to be set 
		 * @param value Value to be set
		 */
		public void setAttributeValue(int index, double value) {
			this.values[index] = value;
		}
		
		/**
		 * Set the value of a given attribute.
		 * 
		 * @param name	Name of the attribute to be set 
		 * @param value Value to be set
		 */
		public void setAttributeValue(String name, double value) {
			int index = Attributes.this.getAttributeIndex(name);
			this.setAttributeValue(index, value);
		}
		
		/**
		 * Set the value of a given attribute.
		 * 
		 * @param attribute	Attribute to be set
		 * @param value		Value to be set
		 */
		public void setAttributeValue(Attribute attribute, double value) {
			int index = Attributes.this.getAttribtueIndex(attribute);
			this.setAttributeValue(index, value);
		}

		/**
		 * Get values of all attributes.
		 * 
		 * @return Double array containing all the attribute values.
		 */
		public double[] getValues() {
			return values.clone();
		}
	 
	}

}
