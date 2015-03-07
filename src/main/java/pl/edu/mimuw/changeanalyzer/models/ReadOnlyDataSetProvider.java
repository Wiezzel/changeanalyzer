package pl.edu.mimuw.changeanalyzer.models;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

import pl.edu.mimuw.changeanalyzer.exceptions.ProcessingException;
import weka.core.Instances;


public class ReadOnlyDataSetProvider extends DataSetProvider {

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
