package pl.edu.mimuw.changeanalyzer.exceptions;


/**
 * Base class for all ChangeAnalyzer exceptions.
 * 
 * @author Adam Wierzbicki
 */
public class ChangeAnalyzerException extends Exception {

	private static final long serialVersionUID = 4447485625147295555L;

	public ChangeAnalyzerException() {}

	public ChangeAnalyzerException(String arg0) {
		super(arg0);
	}

	public ChangeAnalyzerException(Throwable arg0) {
		super(arg0);
	}

	public ChangeAnalyzerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ChangeAnalyzerException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
