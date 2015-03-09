package pl.edu.mimuw.changeanalyzer.models;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Instances;


/**
 * Simple data set provider which is uncapable of extracting or processing data.
 * It expects processed data with class attribute on the last position.
 * Any call to extractDataFromRepository or call to readDataFromFile with `raw`
 * argument set to true will throw an {@link UnsupportedOperationException}.
 * 
 * @author Adam Wierzbicki
 */
public class ReadOnlyDataSetProvider extends DataSetProvider {

	/**
	 * Construct a new ReadOnlyDataSetProvider.
	 */
	public ReadOnlyDataSetProvider() {
		super(null, new DefaultDataSetProcessor());
	}
	
	@Override
	public void extractDataFromRepository(Repository repository) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void extractDataFromRepository(File repoDir) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void extractDataFromRepository(String repoPath) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void readDataFromFile(String dataPath, boolean raw) throws IOException {
		if (raw) {
			throw new UnsupportedOperationException();
		}
		super.readDataFromFile(dataPath, false);
	}
	
	private static class DefaultDataSetProcessor implements DataSetProcessor {

		@Override
		public Instances processDataSet(Instances dataSet) throws ProcessingException {
			return dataSet;
		}

		@Override
		public void setClassAttribute(Instances dataSet) throws ProcessingException {
			dataSet.setClassIndex(dataSet.numAttributes() - 1);
		}
		
	}

}
