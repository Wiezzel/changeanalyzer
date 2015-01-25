package pl.edu.mimuw.changeanalyzer.extraction;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * Extractor of relevant commit information from JGit's RevCommit objects.
 * It stores {@link CommitInfo} objects.
 * 
 * @author Adam Wierzbicki
 */
public class CommitInfoExtractor {
	
	private Pattern bugfixPattern;
	private Map<String, CommitInfo> commits;
	
	/**
	 * Construct a new CommitInfoExtractor.
	 */
	public CommitInfoExtractor() {
		this.bugfixPattern = Pattern.compile("bug|fix|issue", Pattern.CASE_INSENSITIVE);
		this.commits = new HashMap<String, CommitInfo>();
	}
	
	/**
	 * Extract & store relevant information about a given commit. This method checks,
	 * whether the commit is a bugfix by performing a simple regex match.
	 * 
	 * @param commit Commit to extract information from
	 */
	public void extractCommitInfo(RevCommit commit) {
		String id = commit.getName();
		int time = commit.getCommitTime();
		PersonIdent authorIdent = commit.getAuthorIdent();
		String message = commit.getFullMessage();
		String author = authorIdent.getName();
		boolean fix = this.isBugfix(message);
		
		this.commits.put(id, new CommitInfo(id, author, time, fix, 0, 0)); 
	}	
	
	/**
	 * Add the number of changes in a given structure entity version, to the total
	 * sum of changes in the commit associated with this version.
	 * 
	 * @param version Structure entity version to get number of changes from
	 */
	public void updateNumChanges(StructureEntityVersion version) {
		String id = version.getVersion();
		int numChanges = version.getSourceCodeChanges().size();
		this.commits.get(id).addChangedEntity(numChanges);
	}
	
	/**
	 * Get a CommitInfo object for a given commit ID.
	 * 
	 * @param commitId Commit ID to get commit info for
	 * @return Commit info for the given ID
	 * (null if the given ID has no mapping)
	 */
	public CommitInfo getCommitInfo(String commitId) {
		return this.commits.get(commitId);
	}

	/**
	 * Get information extracted from all commits.
	 * 
	 * @return An iterable of CommitInfo objects
	 */
	public Iterable<CommitInfo> getAllCommitInfos() {
		return this.commits.values();
	}
	
	/**
	 * Check if a given commit message indicates that commit is a bug-fix.
	 * 
	 * @param message Message to check
	 * @return True iff commit is a bug-fix
	 */
	private boolean isBugfix(String message) {
		Matcher matcher = this.bugfixPattern.matcher(message);
		return matcher.find();
	}

}
