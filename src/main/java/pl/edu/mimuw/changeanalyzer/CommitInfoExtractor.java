package pl.edu.mimuw.changeanalyzer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;


/**
 * Extractor of relevant commit information from JGit's RevCommit objects 
 */
public class CommitInfoExtractor {
	
	private Pattern bugfixPattern = Pattern.compile("bug|fix|issue", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Extract relevant information from a given commit. This method checks,
	 * whether the commit is a bugfix, by performing a simple regex match.
	 * 
	 * @param commit Commit to extract information from
	 * @return CommitInfo object containing ID, time and author of the given commit
	 * and information if it is a bugfix
	 */
	public CommitInfo extractCommitInfo(RevCommit commit) {
		String id = commit.getName();
		int time = commit.getCommitTime();
		PersonIdent authorIdent = commit.getAuthorIdent();
		String message = commit.getFullMessage();
		String author = authorIdent.getName();
		boolean fix = this.isBugfix(message);
		
		return new CommitInfo(id, author, time, fix); 
	}
	
	private boolean isBugfix(String message) {
		Matcher matcher = this.bugfixPattern.matcher(message);
		return matcher.find();
	}

}
