package pl.edu.mimuw.changeanalyzer.models.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

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
	 * Construct new Attributes.
	 */
	public Attributes() {
		this.attributes = new ArrayList<Attribute>();
		this.indices = new HashMap<String, Integer>();
	}
	
	/**
	 * Construct a copy of existing Attributes.
	 * 
	 * @param attributes Attributes to be copied.
	 */
	public Attributes(Attributes attributes) {
		this.attributes = new ArrayList<Attribute>(attributes.attributes);
		this.indices = new HashMap<String, Integer>(attributes.indices);
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
	public int getAttributeIndex(Attribute attribute) {
		return this.getAttributeIndex(attribute.name());
	}
	
	/**
	 * Get the numeric indices of attributes.
	 * 
	 * @param names Names of attributes to get indices of
	 * @return Indices of attributes with the given names
	 */
	public int[] getAttributeIndices(String[] names) {
		return ArrayUtils.toPrimitive(Arrays.stream(names)
			.map(this::getAttributeIndex)
			.toArray(Integer[]::new)
		);
	}
	
	/**
	 * Get the numeric indices of attributes.
	 * 
	 * @param names Attributes to get indices of
	 * @return Indices of the given attributes
	 */
	public int[] getAttributIndices(Attribute[] attributes) {
		return ArrayUtils.toPrimitive(Arrays.stream(attributes)
			.map(this::getAttributeIndex)
			.toArray(Integer[]::new)
		);
	}
	
	/**
	 * Get a shallow copy of this attributes.
	 * 
	 * @return A shallow copy of this attributes
	 */
	public Attributes copyOf() {
		return new Attributes(this);
	}

}
