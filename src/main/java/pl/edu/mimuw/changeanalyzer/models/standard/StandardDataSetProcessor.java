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


public class StandardDataSetProcessor extends DataSetProcessor {
	
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
	public static final String[] PARAM_CHANGE_ATTRIBUTES = {
		"PARAMETER_DELETE",
		"PARAMETER_INSERT",
		"PARAMETER_ORDERING_CHANGE",
		"PARAMETER_RENAMING",
		"PARAMETER_TYPE_CHANGE"
	};
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
	
	public static final Attribute PARAM_CHANGE = new Attribute("paramChange");
	public static final Attribute HEADER_CHANGE = new Attribute("headerChange");
	
	private int[] paramChangeIndices;
	private int[] headerChnageIndices;
	private int[] allDeleteIndices;
	
	public StandardDataSetProcessor(Attributes attributes, int classIndex) {
		super(attributes, classIndex);
		this.initAttributeIndices();
	}

	public StandardDataSetProcessor(Attributes attributes, Attribute classAttr) {
		super(attributes, classAttr);
		this.initAttributeIndices();
	}

	public StandardDataSetProcessor(Attributes attributes, String classAttrName) {
		super(attributes, classAttrName);
		this.initAttributeIndices();
	}
	
	private void initAttributeIndices() {
		this.paramChangeIndices = this.attributes.getAttributeIndices(PARAM_CHANGE_ATTRIBUTES);
		this.headerChnageIndices = this.attributes.getAttributeIndices(HEADER_CHANGE_ATTRIBUTES);
		this.allDeleteIndices = ArrayUtils.addAll(ArrayUtils.addAll(this.paramChangeIndices,
				this.headerChnageIndices), this.attributes.getAttributeIndices(DISCARD_ATTRIBUTES));
	}

	@Override
	public Instances processDataSet(Instances dataSet) throws ProcessingException {
		AttributeProcessor paramProcessor = new SumAttributes(this.paramChangeIndices, PARAM_CHANGE, dataSet);
		dataSet = paramProcessor.processAttributes(dataSet);

		AttributeProcessor headerProcessor = new SumAttributes(this.headerChnageIndices, HEADER_CHANGE, dataSet);
		dataSet = headerProcessor.processAttributes(dataSet);
		
		AttributeProcessor deleteProcessor = new DeleteAttributes(this.allDeleteIndices);
		dataSet = deleteProcessor.processAttributes(dataSet);
		
		dataSet.setClassIndex(classIndex);
		return dataSet;
	}

}
