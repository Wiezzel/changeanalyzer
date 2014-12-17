package pl.edu.mimuw.changeanalyzer.io;


import java.io.IOException;

import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;


public interface RepoHistoryWriter {
	
	public void writeChanges(Iterable<MethodHistory> histories) throws IOException;
	
	public void writeCommits(Iterable<RevCommit> commits) throws IOException;
	
	public void close() throws IOException;

}
