package pl.edu.mimuw.changeanalyzer.extraction;

import java.io.IOException;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import pl.edu.mimuw.changeanalyzer.exceptions.ExtractionException;


/**
 * Utility class for repository information extraction.
 * 
 * @author Adam Wierzbicki
 */
public class Utils {
	
	/**
	 * Get ID of the HEAD of a repository
	 * 
	 * @param repository Repository to find HEAD
	 * @return ID of the HEAD of the given repository
	 * @throws IOException
	 * @throws ExtractionException
	 */
	public static ObjectId getHead(Repository repository) throws IOException, ExtractionException {
		try {
			return repository.resolve(Constants.HEAD);
		} catch (RevisionSyntaxException | AmbiguousObjectException | IncorrectObjectTypeException e) {
			throw new ExtractionException("Failed to obtain repository head", e);
		}
	}

}
