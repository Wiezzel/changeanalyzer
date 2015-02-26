package pl.edu.mimuw.changeanalyzer.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.core.Attribute;


/**
 * Class representing a set of model attributes (see {@link Attribute}).
 * 
 * @author Adam Wierzbicki
 */
public class Attributes {
	
	private ArrayList<Attribute> attributes;
	private Map<String, Integer> indices;
	
	/**
	 * Create new Attributes.
	 */
	public Attributes() {
		this.attributes = new ArrayList<Attribute>();
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
		this.attributes.add(attribute);
	}
	
	/**
	 * Get a vector with all the attributes in this set.
	 * 
	 * @return Fast vector with attributes 
	 */
	public ArrayList<Attribute> getAttributesVector() {
		return this.attributes;
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

}
