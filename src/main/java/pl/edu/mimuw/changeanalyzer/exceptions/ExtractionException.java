package pl.edu.mimuw.changeanalyzer.exceptions;


public class ExtractionException extends ChangeAnalyzerException {

	private static final long serialVersionUID = -8356457598059513346L;

	public ExtractionException() {}

	public ExtractionException(String arg0) {
		super(arg0);
	}

	public ExtractionException(Throwable arg0) {
		super(arg0);
	}

	public ExtractionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ExtractionException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
