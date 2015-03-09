package pl.edu.mimuw.changeanalyzer.models.standard;

import org.apache.commons.lang3.ArrayUtils;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import pl.edu.mimuw.changeanalyzer.models.DataSetProcessor;
import pl.edu.mimuw.changeanalyzer.models.attributes.AttributeProcessor;
import pl.edu.mimuw.changeanalyzer.models.attributes.Attributes;
import pl.edu.mimuw.changeanalyzer.models.attributes.DeleteAttributes;
import pl.edu.mimuw.changeanalyzer.models.attributes.SumAttributes;
import weka.core.Attribute;
import weka.core.Instances;


/**
 * Stadard data set processor used by ChangeAnalyzer for processing raw
 * data sets built by a {@link StandardDataSetBuilder}. It perfoms the
 * following modifications:
 * <ul>
 * 		<li>Discards unimportant attributes listed in {@link #DISCARD_ATTRIBUTES}.</li>
 * 		<li>Sums values of parameter change-related attributes listed in
 * 			{@link #PARAMS_CHANGE_ATTRIBUTES} into a new attribute {@link #PARAMS_CHANGE}.</li>
 * 		<li>Sums values of header change-related atrributes listed in
 * 			{@link #HEADER_CHANGE_ATTRIBUTES} into a new attribute {@link #HEADER_CHANGE}.</li>
 * </ul>
 * 
 * @author Adam Wierzbicki
 */
public class StandardDataSetProcessor implements DataSetProcessor {
	
	/**
	 * Attributes to remove from a data set.
	 */
	public static final String[] DISCARD_ATTRIBUTES = {
		"ADDING_ATTRIBUTE_MODIFIABILITY",
		"ADDING_CLASS_DERIVABILITY",
		"ADDITIONAL_CLASS",
		"ADDITIONAL_FUNCTIONALITY",
		"ADDITIONAL_OBJECT_STATE",
		"ATTRIBUTE_RENAMING",
		"ATTRIBUTE_TYPE_CHANGE",
		"CLASS_RENAMING",
		"COMMENT_DELETE",
		"COMMENT_INSERT",
		"COMMENT_MOVE",
		"COMMENT_UPDATE",
		"DOC_DELETE",
		"DOC_INSERT",
		"DOC_UPDATE",
		"PARENT_CLASS_CHANGE",
		"PARENT_CLASS_DELETE",
		"PARENT_CLASS_INSERT",
		"PARENT_INTERFACE_CHANGE",
		"PARENT_INTERFACE_DELETE",
		"PARENT_INTERFACE_INSERT",
		"REMOVED_CLASS",
		"REMOVED_FUNCTIONALITY",
		"REMOVED_OBJECT_STATE",
		"REMOVING_ATTRIBUTE_MODIFIABILITY",
		"REMOVING_CLASS_DERIVABILITY",
		"UNCLASSIFIED_CHANGE"
	};
	
	/**
	 * Attributes describing paramters changes (deletion, insertion,
	 * renaming, reordering or type change).
	 */
	public static final String[] PARAMS_CHANGE_ATTRIBUTES = {
		"PARAMETER_DELETE",
		"PARAMETER_INSERT",
		"PARAMETER_ORDERING_CHANGE",
		"PARAMETER_RENAMING",
		"PARAMETER_TYPE_CHANGE"
	};
	
	/**
	 * Attributes describing header changes (method name, overridability,
	 * accessibiblity and return type).
	 */
	public static final String[] HEADER_CHANGE_ATTRIBUTES = {
		"ADDING_METHOD_OVERRIDABILITY",
		"DECREASING_ACCESSIBILITY_CHANGE",
		"INCREASING_ACCESSIBILITY_CHANGE",
		"METHOD_RENAMING",
		"REMOVING_METHOD_OVERRIDABILITY",
		"RETURN_TYPE_CHANGE",
		"RETURN_TYPE_DELETE",
		"RETURN_TYPE_INSERT"
	};
	
	public static final Attribute PARAMS_CHANGE = new Attribute("paramsChange");
	public static final Attribute HEADER_CHANGE = new Attribute("headerChange");
	
	private int[] paramsChangeIndices;
	private int[] headerChnageIndices;
	private int[] allDeleteIndices;
	private String classAttrName;

	/**
	 * Construct a new StandardDataSetProcessor.
	 * 
	 * @param attributes	Attributes of the data sets which are to be processed by this
	 * 						processor. (Must include all attributes listed in {@link #DISCARD_ATTRIBUTES}
	 * 						{@link #PARAMS_CHANGE_ATTRIBUTES} and {@link #HEADER_CHANGE_ATTRIBUTES}).
	 * @param classAttrName	Name of the class (bug-proneness) attribute
	 */
	public StandardDataSetProcessor(Attributes attributes, String classAttrName) {
		this.classAttrName = classAttrName;
		this.paramsChangeIndices = attributes.getAttributeIndices(PARAMS_CHANGE_ATTRIBUTES);
		this.headerChnageIndices = attributes.getAttributeIndices(HEADER_CHANGE_ATTRIBUTES);
		this.allDeleteIndices = ArrayUtils.addAll(ArrayUtils.addAll(this.paramsChangeIndices,
				this.headerChnageIndices), attributes.getAttributeIndices(DISCARD_ATTRIBUTES));
	}
	
	/**
	 * Construct a new StandardDataSetProcessor.
	 * 
	 * @param attributes		Attributes of the data sets which are to be processed by this
	 * 							processor. (Must include all attributes listed in {@link #DISCARD_ATTRIBUTES}
	 * 							{@link #PARAMS_CHANGE_ATTRIBUTES} and {@link #HEADER_CHANGE_ATTRIBUTES}).
	 * @param classAttribute	Class attribute (bug-proneness).
	 */
	public StandardDataSetProcessor(Attributes attributes, Attribute classAttribute) {
		this(attributes, classAttribute.name());
	}
	
	/**
	 * Construct a new StandardDataSetProcessor.
	 * 
	 * @param attributes	Attributes of the data sets which are to be processed by this
	 * 						processor. (Must include all attributes listed in {@link #DISCARD_ATTRIBUTES}
	 * 						{@link #PARAMS_CHANGE_ATTRIBUTES} and {@link #HEADER_CHANGE_ATTRIBUTES}).
	 * @param classIndex	Index of the class (bug-proneness) attribute
	 */
	public StandardDataSetProcessor(Attributes attributes, int classIndex) {
		this(attributes, attributes.getAttribute(classIndex));
	}
	
	@Override
	public Instances processDataSet(Instances dataSet) throws ProcessingException {
		AttributeProcessor paramsProcessor = new SumAttributes(this.paramsChangeIndices, PARAMS_CHANGE, dataSet);
		dataSet = paramsProcessor.processAttributes(dataSet);

		AttributeProcessor headerProcessor = new SumAttributes(this.headerChnageIndices, HEADER_CHANGE, dataSet);
		dataSet = headerProcessor.processAttributes(dataSet);
		
		AttributeProcessor deleteProcessor = new DeleteAttributes(this.allDeleteIndices);
		dataSet = deleteProcessor.processAttributes(dataSet);

		return dataSet;
	}

	@Override
	public void setClassAttribute(Instances dataSet) throws ProcessingException {
		Attribute classAttribute = dataSet.attribute(this.classAttrName);
		dataSet.setClass(classAttribute);
	}

}
