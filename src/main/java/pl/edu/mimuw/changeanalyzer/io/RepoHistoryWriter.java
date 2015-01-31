package pl.edu.mimuw.changeanalyzer.io;


import java.io.IOException;

import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;


/**
 * Simple interface for saving information extracted from a repository.
 * 
 * @author Adam Wierzbicki
 */
public interface RepoHistoryWriter {
	
	/**
	 * Write fined-grained source code changes.
	 * 
	 * @param histories Method histories containing changes to be written
	 * @throws IOException
	 */
	public void writeChanges(Iterable<MethodHistory> histories) throws IOException;
	
	/**
	 * Write commits.
	 * 
	 * @param commits Commits to be written.
	 * @throws IOException
	 */
	public void writeCommits(Iterable<RevCommit> commits) throws IOException;
	
	/**
	 * Close the writer. This method should be called to avoid resource leak.
	 * A closed writer can no loger write any records.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;

}
