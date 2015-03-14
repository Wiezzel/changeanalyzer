package pl.edu.mimuw.changeanalyzer.models.attributes;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;


/**
 * Attribute processor which moves class attribute to the last position.
 * 
 * @author Adam Wierzbicki
 */
public class ReorderClassAttribute implements AttributeProcessor {
	
	private int classAttrIndex = 0;
	private String classAttrName = null;
	
	/**
	 * Construct a new ReorderClassAttribute processor. The constructed instance
	 * will retrieve class attribute index from data sets.
	 */
	public ReorderClassAttribute() {}

	/**
	 * Construct a new ReorderClassAttribute processor using the given class
	 * attribute index.
	 * 
	 * @param classAttrIndex Class attribute index to be used by this processor
	 */
	public ReorderClassAttribute(int classAttrIndex) {
		this.classAttrIndex = classAttrIndex + 1;
	}

	/**
	 * Construct a new ReorderClassAttribute processor using the given class
	 * attribute name.
	 * 
	 * @param classAttrName Class attribute name to be used by this processor
	 */
	public ReorderClassAttribute(String classAttrName) {
		this.classAttrName = classAttrName;
	}

	@Override
	public Instances processAttributes(Instances data) throws ProcessingException {
		int classIndex = this.getClassIndex(data);
		if (classIndex < 1 || classIndex == data.numAttributes()) {
			return data;
		}
		
		String rangeList = classIndex > 1
			? "1-" + (classIndex - 1) + "," + (classIndex + 1) + "-last," + classIndex
			: "2-last,1";
		
		try {
			Reorder reorder = new Reorder();
			reorder.setAttributeIndices(rangeList);
			reorder.setInputFormat(data);
			return Filter.useFilter(data, reorder);
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}
	
	/**
	 * Get class attribute index of a data set. If this processor's class attribute
	 * index is set, it is returned. Otherwise, if class attribute name is set,
	 * it is searched among the attributes of the given data set and the found
	 * index is returned. Otherwise, the class index is retrieved from the data
	 * set itself.
	 * 
	 * @param data Data set to get class index of
	 * @return Class index of the given data set
	 */
	private int getClassIndex(Instances data) {
		if (this.classAttrIndex > 0) {
			if (this.classAttrIndex > data.numAttributes()) {
				throw new IllegalArgumentException("Class attribute index `" + this.classAttrIndex 
						+ "` exceeds attribute number");
			}
			return this.classAttrIndex; 
		}
		if (this.classAttrName != null) {
			Attribute classAttr = data.attribute(this.classAttrName);
			if (classAttr == null) {
				throw new IllegalArgumentException("Class attribute `" + this.classAttrName +"` not found");
			}
			return classAttr.index() + 1;
		}
		return data.classIndex();
	}

}
