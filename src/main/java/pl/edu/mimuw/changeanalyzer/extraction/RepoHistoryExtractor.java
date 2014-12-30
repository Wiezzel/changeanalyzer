package pl.edu.mimuw.changeanalyzer.extraction;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.io.CSVRepoHistoryWriter;
import pl.edu.mimuw.changeanalyzer.io.RepoHistoryWriter;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


public class RepoHistoryExtractor {
	
	private Repository repository;
	private Git git;
	private ClassHistoryExtractor extractor;
	
	public RepoHistoryExtractor (Repository repository) {
		this.repository = repository;
		this.git = new Git(repository);
		this.extractor = new ClassHistoryExtractor(repository);
	}
	
	public RepoHistoryExtractor (File repoDir) throws IOException {
		FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		this.repository = repoBuilder.setWorkTree(repoDir).build();
		this.git = new Git(this.repository);
		this.extractor = new ClassHistoryExtractor(this.repository);
	}
	
	public RepoHistoryExtractor (String repoPath) throws IOException {
		this(new File(repoPath));
	}
	
	/**
	 * Extract histories of all clases (that is all .java files) in the repository.
	 * 
	 * @return Mapping from file paths to class histories
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public Map<String, ClassHistory> extractClassHistories() throws IOException, ChangeAnalyzerException {
		Map<String, ClassHistory> map = new HashMap<String, ClassHistory>();
		
		ObjectId headId = Utils.getHead(this.repository);
		RevWalk revWalk = new RevWalk(this.repository);
		RevCommit headCommit = revWalk.parseCommit(headId);
		RevTree revTree = headCommit.getTree();
		
		TreeWalk treeWalk = new TreeWalk(this.repository);
		treeWalk.addTree(revTree);
		treeWalk.setRecursive(true);
		TreeFilter filter = PathSuffixFilter.create(".java");
		treeWalk.setFilter(filter);
		
		while (treeWalk.next()) {
			String path = treeWalk.getPathString();
			ClassHistory history = this.extractor.extractClassHistory(path);
			map.put(path, history);
		}
		
		return map;
	}
	
	/**
	 * Extract all commits which can be achieved from the repostory HEAD.
	 * 
	 * @return Extracted commits
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public Iterable<RevCommit> extractRelevantCommits() throws IOException, ChangeAnalyzerException {
		ObjectId head = Utils.getHead(this.repository);
		LogCommand logCommand = this.git.log().add(head);
		try {
			return logCommand.call();
		} catch (GitAPIException e) {
			throw new ChangeAnalyzerException("Failed to execute LOG command", e);
		}
	}
	
	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		RepoHistoryExtractor extractor = new RepoHistoryExtractor("C:\\jgit");
		RepoHistoryWriter writer = new CSVRepoHistoryWriter("changes.csv", "commits.csv");
		
		long startTime = System.currentTimeMillis();
		
		Map<String, ClassHistory> map =  extractor.extractClassHistories();
		ClassHistoryWrapper wrapper = new ClassHistoryWrapper(map.values());
		Iterable<RevCommit> commits = extractor.extractRelevantCommits();
		
		writer.writeChanges(wrapper);
		writer.writeCommits(commits);
		writer.close();

		long endTime = System.currentTimeMillis();
		double execTime = ((double) (endTime - startTime)) / 1000;
		System.out.println("Execution time: " + execTime + " s");
	}

}
