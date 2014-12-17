package pl.edu.mimuw.changeanalyzer.io;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


public class CSVRepoHistoryWriter implements RepoHistoryWriter {

	private CSVPrinter methodHistoryPrinter;
	private CSVPrinter commitPrinter;
	
	/**
	 * Construct a new CSVRepoHistoryWriter.
	 * 
	 * @param changesFile	File which the constructed printer will write code changes to
	 * @param commitsFile	File which the constructed printer will write commits to
	 * @param format		CSV format of the written records
	 * @throws IOException
	 */
	public CSVRepoHistoryWriter(File changesFile, File commitsFile, CSVFormat format) throws IOException {
		this.methodHistoryPrinter = new CSVPrinter(new FileWriter(changesFile), format);
		this.commitPrinter = new CSVPrinter(new FileWriter(commitsFile), format);
	}

	/**
	 * Construct a new CSVRepoHistoryWriter. It will use default CSV format.
	 * 
	 * @param changesFile	File which the constructed printer will write code changes to
	 * @param commitsFile	File which the constructed printer will write commits to
	 * @throws IOException
	 */
	public CSVRepoHistoryWriter(File changesFile, File commitsFile) throws IOException {
		this(changesFile, commitsFile, CSVFormat.DEFAULT);
	}
	
	/**
	 * Construct a new CSVRepoHistoryWriter.
	 * 
	 * @param changesPath	Path to a file which the constructed printer will write code changes to
	 * @param commitsPath	Path to a file which the constructed printer will write commits to
	 * @param format		CSV format of the written records
	 * @throws IOException
	 */
	public CSVRepoHistoryWriter(String changesPath, String commitsPath, CSVFormat format) throws IOException {
		this.methodHistoryPrinter = new CSVPrinter(new FileWriter(changesPath), format);
		this.commitPrinter = new CSVPrinter(new FileWriter(commitsPath), format);
	}
	
	/**
	 * Construct a new CSVRepoHistoryWriter. It will use default CSV format.
	 * 
	 * @param changesPath	Path to a file which the constructed printer will write code changes to
	 * @param commitsPath	Path to a file which the constructed printer will write commits to
	 * @throws IOException
	 */
	public CSVRepoHistoryWriter(String changesPath, String commitsPath) throws IOException {
		this(changesPath, commitsPath, CSVFormat.DEFAULT);
	}
	
	/**
	 * Write all source code changes contained in given method histories.
	 * Written fields are: method name, commit ID, change type.
	 * 
	 * @param histories Method histories conatining changes to be written
	 * @throws IOException
	 */
	@Override
	public void writeChanges(Iterable<MethodHistory> histories) throws IOException {
		for (MethodHistory history: histories) {
			String methodName = history.getUniqueName();
			for (StructureEntityVersion version: history.getVersions()) {
				String commitId = version.getVersion();
				for (SourceCodeChange change: version.getSourceCodeChanges()) {
					this.methodHistoryPrinter.printRecord(new Object[] {
						methodName,
						commitId,
						change.getChangeType()
					});
				}
			}
		}
	}

	/**
	 * Write all given commits. Written fields are: commit SHA-1, commit message.
	 * 
	 * @param commits Commits to be written
	 * @throws IOException
	 */
	@Override
	public void writeCommits(Iterable<RevCommit> commits) throws IOException {
		for (RevCommit commit: commits) {
			this.commitPrinter.printRecord(new Object[] {
				commit.getName(),
				commit.getFullMessage()
			});
		}
	}

	@Override
	public void close() throws IOException {
		this.methodHistoryPrinter.close();
		this.commitPrinter.close();
	}
	
}
