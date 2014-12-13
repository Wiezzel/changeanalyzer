package pl.edu.mimuw.changeanalyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


public class Test {

	public static void main(String[] args) throws IOException {
		
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		Repository repository = repositoryBuilder
				.setWorkTree(new File("C:\\workspace\\Changedistiller"))
//				.setGitDir(new File("C:\\workspace\\Changedistiller\\.git"))
//				.readEnvironment()
//				.findGitDir()
				.build();
		
		ObjectId headId = repository.resolve("99b9669f70f54d7f05007f87a6ff973e5c76b8fe");
		RevWalk revWalk = new RevWalk(repository);
		RevCommit revCommit = revWalk.parseCommit(headId);
		RevTree revTree = revCommit.getTree();
		
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.addTree(revTree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(PathFilter.create("src/main/java/ch/uzh/ifi/seal/changedistiller/"
				+ "structuredifferencing/StructureDifferencer.java"));
		if (!treeWalk.next()) {
			throw new IllegalStateException("File not found");
		}
		
		ObjectId fileId = treeWalk.getObjectId(0);
		ObjectLoader loader = repository.open(fileId);
		loader.copyTo(new FileOutputStream("SD2.java"));
		
		Git git = new Git(repository);
		
		revWalk.dispose();
		repository.close();
		
		File foo1 = new File("Foo1.java");
		File foo2 = new File("Foo2.java");
		File foo3 = new File("Foo3.java");
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		
		distiller.extractClassifiedSourceCodeChanges(foo1, foo2);
		printChanges(distiller.getSourceCodeChanges());
		distiller.extractClassifiedSourceCodeChanges(foo2, foo3);
		printChanges(distiller.getSourceCodeChanges());
//		distiller.extractClassifiedSourceCodeChanges(new File("SD0.java"), new File("SD1.java"));
//		printChanges(distiller.getSourceCodeChanges());
		
		for (Map.Entry<String, MethodHistory> entry: distiller.getClassHistory().getMethodHistories().entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue().getUniqueName());
			for (StructureEntityVersion version: entry.getValue().getVersions()) {
				System.out.println(version.getUniqueName());
			}
		}
	}
	
	private static void printChanges(Iterable<SourceCodeChange> changes) {
		for (SourceCodeChange change: changes) {
			System.out.println(change.getChangeType());
			System.out.println(change.getChangedEntity().getUniqueName());
		}
	}

}
