package pl.edu.mimuw.changeanalyzer.exceptions;


public class AttributeProcessingException extends ChangeAnalyzerException {

	private static final long serialVersionUID = 6337220476419627088L;

	public AttributeProcessingException() {
		super();
	}
	
	public AttributeProcessingException(String arg0) {
		super(arg0);
	}

	public AttributeProcessingException(Throwable arg0) {
		super(arg0);
	}

	public AttributeProcessingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AttributeProcessingException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
