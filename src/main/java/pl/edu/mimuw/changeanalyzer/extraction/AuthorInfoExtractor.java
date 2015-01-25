package pl.edu.mimuw.changeanalyzer.extraction;

import java.util.HashMap;
import java.util.Map;


/**
 * Class for gathering information about authors extracted from {@link CommitInfo} objects.
 * 
 * @author Adam Wierzbicki
 */
public class AuthorInfoExtractor {

	private Map<String, AuthorInfo> authors;
	
	/**
	 * Construct a new AuthorInfoExtractor.
	 */
	public AuthorInfoExtractor() {
		this.authors = new HashMap<String, AuthorInfo>();
	}
	
	/**
	 * Get information about author with a given name.
	 * 
	 * @param name Name of the author to get information about
	 * @return AuthorInfo object containing information about the author
	 * (null if the given name has no mapping)
	 */
	public AuthorInfo getAuthorInfo(String name) {
		return this.authors.get(name);
	}
	
	/**
	 * Update information (numbers of commits & changes) about the author of a given commit. 
	 * 
	 * @param commitInfo Information about a commit which author's info should be updated
	 */
	public void updateAuthorInfo(CommitInfo commitInfo) {
		String authorName = commitInfo.getAuthor();
		AuthorInfo authorInfo = this.authors.get(authorName);
		if (authorInfo == null) {
			authorInfo = new AuthorInfo(authorName, null);
			this.authors.put(authorName, authorInfo);
		}
		authorInfo.addCommit(commitInfo.getNumChanges());
	}
	
}
