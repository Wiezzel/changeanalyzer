package pl.edu.mimuw.changeanalyzer.extraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.FollowFilter;
import org.eclipse.jgit.revwalk.RenameCallback;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.exceptions.ExtractionException;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


/**
 * Class responsible for extraction of class histories. It contains reference
 * to a local Git repository. Each class is identified by a relative (to the repository
 * root directory) path to the .java file which cotains it.
 * 
 * @author Adam Wierzbicki
 */
public class ClassHistoryExtractor {
	
	public static final String DIFF_SECTION = "diff";
	public static final String RENAME_KEY = "renames";
	public static final String RENAME_VALUE = "copy";
	
	private Repository repository;
	
	/**
	 * Construct a new ClassHistoryExtractor.
	 * 
	 * @param repository Repository to extract class histories from
	 */
	public ClassHistoryExtractor(Repository repository) {
		this.repository = repository;
		this.repository.getConfig().setString(DIFF_SECTION, null, RENAME_KEY, RENAME_VALUE);
		// Copy detection is not working in JGit 3.6.2, but maybe will be fixed
	}

	/**
	 * Construct a new ClassHistoryExtractor.
	 * 
	 * @param repoDir Directory with a repository to extract class histories from
	 * @throws IOException When the given directory doesn't contain a proper repository
	 */
	public ClassHistoryExtractor(File repoDir) throws IOException {
		this(new FileRepositoryBuilder().setWorkTree(repoDir).build());
	}
	
	/**
	 * Construct a new ClassHistoryExtractor.
	 * 
	 * @param repoPath Path to a repository to extract class histories from
	 * @throws IOException When the given path doesn't point to a proper repository
	 */
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
	public RevWalk getFileRevisions(String filePath, RenameCallback callback)
			throws IOException, ChangeAnalyzerException {
		
		ObjectId head = Utils.getHead(this.repository);
		if (head == null) {
			throw new ExtractionException("Invalid repository: " + this.repository.getWorkTree().getAbsolutePath());
		}
		
		DiffConfig diffConfig = this.repository.getConfig().get(DiffConfig.KEY);
		FollowFilter filter = FollowFilter.create(filePath, diffConfig);
		filter.setRenameCallback(callback);
		
		RevWalk revWalk = new RevWalk(this.repository);
		RevCommit headCommit = revWalk.parseCommit(head);
		revWalk.markStart(headCommit);
		revWalk.setTreeFilter(filter);
		
		return revWalk;
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
		Stack<String> filePaths = new Stack<String>();
		filePaths.add(filePath);
		
		RevWalk revWalk = this.getFileRevisions(filePath, new RenameCallback() {
			@Override
			public void renamed(DiffEntry diffentry) {
				filePaths.add(diffentry.getOldPath());
			}
		});
		for (RevCommit commit: revWalk) {
			commits.add(commit);
		}
		revWalk.dispose();
		if (commits.size() < 2) {
			return null;
		}
		
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		File tmpOldFile = File.createTempFile(filePath.replace('/', '.'), ".old");
		File tmpNewFile = File.createTempFile(filePath.replace('/', '.'), ".new");
		
		RevCommit oldCommit = null;
		RevCommit newCommit = commits.pop();
		String oldPath = null;
		String newPath = filePaths.pop();
				
		while (!commits.empty()) {
			oldCommit = newCommit;
			newCommit = commits.pop();
			oldPath = newPath;
			
			ObjectId oldFileId = this.getFileId(oldCommit, oldPath);
			ObjectId newFileId = this.getFileId(newCommit, newPath);
			if (newFileId == null) {
				if (filePaths.empty()) {
					System.err.println("Discarding " + newCommit);
					newCommit = oldCommit;
					continue;
				}
				newPath = filePaths.peek();
				newFileId = this.getFileId(newCommit, newPath);
				if (newFileId == null) {
					System.err.println("Discarding " + newCommit);
					newCommit = oldCommit;
					newPath = oldPath;
					continue;
				}
				filePaths.pop();
			}
			
			this.storeFileRevision(oldFileId, tmpOldFile);
			this.storeFileRevision(newFileId, tmpNewFile);
			distiller.extractClassifiedSourceCodeChanges(tmpOldFile, tmpNewFile, newCommit.name());
		}
		
		if (!tmpOldFile.delete()) {
			throw new IOException("Failed to delete file " + tmpOldFile.getAbsolutePath());
		}
		if (!tmpNewFile.delete()) {
			throw new IOException("Failed to delete file " + tmpNewFile.getAbsolutePath());
		}
		return distiller.getClassHistory();
	}
	
	/**
	 * 
	 * @param commit	Commit from which file version is retrieved
	 * @param path		Path to the versioned file (relative to the main directory of the repository)
	 * @return
	 * @throws IOException
	 */
	private ObjectId getFileId(RevCommit commit, String path) throws IOException {
		RevTree revTree = commit.getTree();
		PathFilter filter = PathFilter.create(path);
		
		TreeWalk treeWalk = new TreeWalk(this.repository);
		treeWalk.addTree(revTree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(filter);
	
		if (!treeWalk.next()) {
			return null;
		}
		return treeWalk.getObjectId(0);
	}
	
	/**
	 * Copy the content of a git versioned file into another file.
	 *
	 * @param fileId	ID of a versioned file
	 * @param destFile 	Destination file
	 * @throws IOException
	 */
	private void storeFileRevision(ObjectId fileId, File destFile) throws IOException {
		ObjectLoader loader = this.repository.open(fileId, Constants.OBJ_BLOB);
		OutputStream stream = new FileOutputStream(destFile);
		
		try {
			loader.copyTo(stream);
		} catch (IOException e) {
			throw e;
		} finally {
			stream.close();
		}
	}

}
