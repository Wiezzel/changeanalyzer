package pl.edu.mimuw.changeanalyzer.exceptions;


/**
 * Exception thrown when an error occurs during processing a data set.
 * 
 * @author Adam Wierzbicki
 */
public class ProcessingException extends ChangeAnalyzerException {

	private static final long serialVersionUID = 6337220476419627088L;

	public ProcessingException() {
		super();
	}
	
	public ProcessingException(String arg0) {
		super(arg0);
	}

	public ProcessingException(Throwable arg0) {
		super(arg0);
	}

	public ProcessingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ProcessingException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
