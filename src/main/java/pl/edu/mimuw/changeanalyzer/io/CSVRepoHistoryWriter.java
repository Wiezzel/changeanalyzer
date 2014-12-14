package pl.edu.mimuw.changeanalyzer.io;


import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;


public class CSVRepoHistoryWriter implements RepoHistoryWriter {

	private CSVPrinter methodHistoriesPrinter;
	private CSVPrinter commitsPrinter;
	
	@Override
	public void writeMethodHistories(Iterable<MethodHistory> histories) throws IOException {
		for (MethodHistory history: histories) {
			Object[] record = methodHistoryToRecord(history);
			this.methodHistoriesPrinter.printRecord(record);
		}
	}

	@Override
	public void writeCommits(Iterable<RevCommit> commits) throws IOException {
		for (RevCommit commit: commits) {
			Object[] record = commitToRecord(commit);
			this.commitsPrinter.printRecord(record);
		}
	}
	
	private static Object[] methodHistoryToRecord(MethodHistory history) {
		return new Object[] {
			//TODO
		};
	}
	
	private static Object[] commitToRecord(RevCommit commit) {
		return new Object[] {
			commit.getName(),
			commit.getFullMessage()
		};
	}
	
}
