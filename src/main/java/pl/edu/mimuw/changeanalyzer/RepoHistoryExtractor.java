package pl.edu.mimuw.changeanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


public class RepoHistoryExtractor {
	
	private Repository repository;
	private ClassHistoryExtractor extractor;
	
	public RepoHistoryExtractor (Repository repository) {
		this.repository = repository;
		this.extractor = new ClassHistoryExtractor(repository);
	}
	
	public RepoHistoryExtractor (File repoDir) throws IOException {
		FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		this.repository = repoBuilder.setWorkTree(repoDir).build();
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
	
	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		RepoHistoryExtractor extractor = new RepoHistoryExtractor("C:\\jgit");
		long startTime = System.currentTimeMillis();
		Map<String, ClassHistory> map =  extractor.extractClassHistories();
		long endTime = System.currentTimeMillis();
		double execTime = ((double) (endTime - startTime)) / 1000;
		System.out.println("Execution time: " + execTime + " s");
		System.out.println(map.size());
	}

}
