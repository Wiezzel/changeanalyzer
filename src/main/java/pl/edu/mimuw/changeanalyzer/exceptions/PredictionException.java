package pl.edu.mimuw.changeanalyzer.exceptions;


/**
 * Exception thrown due to an error in performing bug-proneness prediction.
 * 
 * @author Adam Wierzbicki
 */
public class PredictionException extends ChangeAnalyzerException {

	private static final long serialVersionUID = 3692808373069386287L;

	public PredictionException() {
		super();
	}

	public PredictionException(String arg0) {
		super(arg0);
	}

	public PredictionException(Throwable arg0) {
		super(arg0);
	}

	public PredictionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public PredictionException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
