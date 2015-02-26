package pl.edu.mimuw.changeanalyzer.distiller;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaSourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.distilling.ClassDistiller;
import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.distilling.SourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.distilling.refactoring.RefactoringCandidateProcessor;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDiffNode;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDifferencer;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.TreeDifferencer;


public class InMemoryChangeDistiller {
	
	private DistillerFactory fDistillerFactory;
	private RefactoringCandidateProcessor fRefactoringProcessor;

	private List<SourceCodeChange> fChanges;
	private ASTHelper<StructureNode> fLeftASTHelper;
	private ASTHelper<StructureNode> fRightASTHelper;
	private ClassHistory fClassHistory;
	private String fVersion;

	public InMemoryChangeDistiller () {
		try {
			this.fDistillerFactory = new JavaDistillerFactory();
			Constructor<?> constructor = RefactoringCandidateProcessor.class.getDeclaredConstructor(
					DistillerFactory.class, SourceCodeChangeClassifier.class);
			constructor.setAccessible(true);
			this.fRefactoringProcessor = (RefactoringCandidateProcessor) constructor.newInstance(
					this.fDistillerFactory, new JavaSourceCodeChangeClassifier());
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Extract classified {@link SourceCodeChange}s between two {@link File}s.
	 * 
	 * @param leftSource
	 * @param leftFileName
	 * @param rightSource
	 * @param rightFileName
	 */
	@SuppressWarnings("unchecked")
	public void extractClassifiedSourceCodeChanges(String leftSource, String leftFileName, 
			String rightSource, String rightFileName) {
		this.fLeftASTHelper = (ASTHelper<StructureNode>) (ASTHelper<? extends StructureNode>) 
				new InMemoryJavaASTHelper(leftSource, leftFileName);
		this.fRightASTHelper = (ASTHelper<StructureNode>) (ASTHelper<? extends StructureNode>) 
				new InMemoryJavaASTHelper(rightSource, rightFileName);
		this.extractDifferences();
	}

	/**
	 * Extract classified {@link SourceCodeChange}s between two {@link File}s.
	 * 
	 * @param leftSource
	 * @param leftFileName
	 * @param rightSource
	 * @param rightFileName
	 * @param version
	 */
	public void extractClassifiedSourceCodeChanges(String leftSource, String leftFileName, 
			String rightSource, String rightFileName, String version) {
		this.fVersion = version;
		this.extractClassifiedSourceCodeChanges(leftSource, leftFileName, rightSource, rightFileName);
		
	}
	
//	//DEUBG
//	@SuppressWarnings("unchecked")
//	public void extract(File left, File right, String v) {
//		this.fVersion = v;
//		this.fLeftASTHelper = (ASTHelper<StructureNode>) (ASTHelper<? extends StructureNode>) 
//				new InMemoryJavaASTHelper(left);
//		this.fRightASTHelper = (ASTHelper<StructureNode>) (ASTHelper<? extends StructureNode>) 
//				new InMemoryJavaASTHelper(right);
//		this.extractDifferences();
//	}
	
	private void extractDifferences() {
		StructureDifferencer structureDifferencer = new StructureDifferencer();
		structureDifferencer.extractDifferences(this.fLeftASTHelper.createStructureTree(), 
				this.fRightASTHelper.createStructureTree());
		StructureDiffNode structureDiff = structureDifferencer.getDifferences();
		if (structureDiff != null) {
			this.fChanges = new LinkedList<SourceCodeChange>();
			this.processRootChildren(structureDiff);
		} 
		else {
			this.fChanges = Collections.emptyList();
		}
	}
	
	private void processRootChildren(StructureDiffNode diffNode) {
		for (StructureDiffNode child : diffNode.getChildren()) {
			if (child.isClassOrInterfaceDiffNode() && mayHaveChanges(child.getLeft(), child.getRight())) {
				if (this.fClassHistory == null) {
					if (fVersion != null) {
						this.fClassHistory = new ClassHistory(this.fRightASTHelper.createStructureEntityVersion(child.getRight(), this.fVersion));
					}
					else {
						this.fClassHistory = new ClassHistory(this.fRightASTHelper.createStructureEntityVersion(child.getRight()));
					}
				}
				this.processClassDiffNode(child);
			}
		}
	}

	private void processClassDiffNode(StructureDiffNode child) {
		ClassDistiller classDistiller;
		if (fVersion != null) {
			classDistiller = new ClassDistiller(
				child,
				fClassHistory,
				fLeftASTHelper,
				fRightASTHelper,
				fRefactoringProcessor,
				fDistillerFactory,
				fVersion
			);
		} 
		else {
			classDistiller = new ClassDistiller(
				child,
				fClassHistory,
				fLeftASTHelper,
				fRightASTHelper,
				fRefactoringProcessor,
				fDistillerFactory
			);
		}
		classDistiller.extractChanges();
		this.fChanges.addAll(classDistiller.getSourceCodeChanges());
	}

	private boolean mayHaveChanges(StructureNode left, StructureNode right) {
		return (left != null) && (right != null);
	}

	public List<SourceCodeChange> getSourceCodeChanges() {
		return this.fChanges;
	}

	public ClassHistory getClassHistory() {
		return this.fClassHistory;
	}
	
	private class JavaDistillerFactory implements DistillerFactory {

		@Override
		public Distiller create(StructureEntityVersion structureEntity) {
			try {
				TreeDifferencer treeDifferencer = new TreeDifferencer();
				SourceCodeChangeClassifier changeClassifier = new JavaSourceCodeChangeClassifier();
				Constructor<?> constructor = Distiller.class.getDeclaredConstructor(StructureEntityVersion.class,
						TreeDifferencer.class, SourceCodeChangeClassifier.class);
				constructor.setAccessible(true);
				return (Distiller) constructor.newInstance(structureEntity, treeDifferencer, changeClassifier);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

}
