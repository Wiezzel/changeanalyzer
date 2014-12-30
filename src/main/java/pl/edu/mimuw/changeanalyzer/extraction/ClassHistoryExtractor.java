package pl.edu.mimuw.changeanalyzer.extraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;


public class ClassHistoryExtractor {
	
	private Repository repository;
	private Git git;
	
	public ClassHistoryExtractor(Repository repository) {
		this.repository = repository;
		this.git = new Git(repository);
	}

	public ClassHistoryExtractor(File repoDir) throws IOException {
		FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		this.repository = repoBuilder.setWorkTree(repoDir).build();
		this.git = new Git(this.repository);
	}
	
	public ClassHistoryExtractor(String repoPath) throws IOException {
		this(new File(repoPath));		
	}
	
	/**
	 * Get all commits modifying the given file (as with 'git log' command)
	 * 
	 * @param filePath	Path to the file (relative to the main directory of the repository)
	 * @return Commits modifying the given file
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public Iterable<RevCommit> getFileRevisions(String filePath) throws IOException, ChangeAnalyzerException {
		ObjectId head = Utils.getHead(this.repository);
		LogCommand logCommand = this.git.log().add(head).addPath(filePath);
		try {
			return logCommand.call();
		} catch (GitAPIException e) {
			throw new ChangeAnalyzerException("Failed to execute LOG command", e);
		}
		//TODO: Handle renamed/moved files
	}
	
	/**
	 * Analyze all commits modyfing a given file (in a linear time order) and extract
	 * history of the class represented by this file.
	 * 
	 * @param filePath 	Path to the file which history is to be extracted (relative to the main 
	 * 					directory of the repository). This should be a .java file.
	 * @return History of the given file
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public ClassHistory extractClassHistory(String filePath) throws IOException, ChangeAnalyzerException {
		Stack<RevCommit> commits = new Stack<RevCommit>();
		for (RevCommit commit: this.getFileRevisions(filePath)) {
			commits.add(commit);
		}
		if (commits.size() < 2) {
			return null;
		}
		
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		File tmpOldFile = File.createTempFile(filePath.replace('/', '.'), ".old");
		File tmpNewFile = File.createTempFile(filePath.replace('/', '.'), ".new");
		
		RevCommit oldCommit = null;
		RevCommit newCommit = commits.pop();
		while (!commits.empty()) {
			oldCommit = newCommit;
			newCommit = commits.pop();
			this.storeFileRevision(oldCommit, filePath, tmpOldFile);
			this.storeFileRevision(newCommit, filePath, tmpNewFile);
			distiller.extractClassifiedSourceCodeChanges(tmpOldFile, tmpNewFile, newCommit.name());
		}
		
		tmpOldFile.delete();
		tmpNewFile.delete();
		return distiller.getClassHistory();
	}
	
	/**
	 * Copy the content of a git versioned file into another file. Retrieved content
	 * is identical to the state of the versioned file after a given commit.
	 * 
	 * @param commit 			Commit from which file version is retrieved
	 * @param versionedFilePath Path to the versioned file (relative to the main directory of the repository)
	 * @param destFile 			Destination file
	 * @throws IOException
	 */
	private void storeFileRevision(RevCommit commit, String versionedFilePath, File destFile) throws IOException {
		RevTree revTree = commit.getTree();
		PathFilter filter = PathFilter.create(versionedFilePath);
		
		TreeWalk treeWalk = new TreeWalk(this.repository);
		treeWalk.addTree(revTree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(filter);
	
		if (!treeWalk.next()) {
			//throw new IllegalStateException("File " + versionedFilePath + " not found in commit " + commit.name());
			return;
		}
		ObjectId fileId = treeWalk.getObjectId(0);
		ObjectLoader loader = this.repository.open(fileId, Constants.OBJ_BLOB);
		

		OutputStream stream = new FileOutputStream(destFile);
		try {
			loader.copyTo(stream);
		} catch (IOException e) {
			stream.close();
			throw e;
		}
	}

	public static void main(String[] args) throws IOException, ChangeAnalyzerException {
		ClassHistoryExtractor extractor = new ClassHistoryExtractor("C:\\jgit");
		String filePath = "org.eclipse.jgit/src/org/eclipse/jgit/api/AddCommand.java";
		ClassHistory history = extractor.extractClassHistory(filePath);
		for (MethodHistory methodHistory : history.getMethodHistories().values()) {
			System.out.println("\nHISTORY OF " + methodHistory.getUniqueName() + "\n");
			for (StructureEntityVersion version : methodHistory.getVersions()) {
				System.out.println("\nVersion " + version.getVersion() + "\n");
				for (SourceCodeChange change : version.getSourceCodeChanges()) {
					System.out.println(change.getChangeType() + ": " + change.getChangedEntity().getUniqueName());
				}
			}
		}
	}

}
