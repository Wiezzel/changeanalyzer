package pl.edu.mimuw.changeanalyzer.io;


import java.io.IOException;

import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;


public interface RepoHistoryWriter {
	
	public void writeMethodHistories(Iterable<MethodHistory> histories) throws IOException;
	
	public void writeCommits(Iterable<RevCommit> commits) throws IOException;

}
