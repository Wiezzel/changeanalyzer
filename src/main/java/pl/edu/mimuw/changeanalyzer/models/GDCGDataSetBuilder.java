package pl.edu.mimuw.changeanalyzer.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.edu.mimuw.changeanalyzer.extraction.AuthorInfo;
import pl.edu.mimuw.changeanalyzer.extraction.CommitInfo;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * This data set builder class assumes geometric decrease (GD) of bug-proneness
 * (see {@link GDDataSetBuilder}) and uses commit group (CG) as model. Each 
 * produced instance represents a group of subsequent commits starting with
 * a commit directly succeeding a bug-fix. Groups contain from one commit
 * up to a whole chunk.
 * This being so, initialLevel corresponds to the bug-proneness of the full chunk,
 * rather than bug-proneness of the last commit (and hence should default to 1.0).
 * 
 * @author Adam Wierzbicki
 */
public class GDCGDataSetBuilder extends GDDataSetBuilder {
	
	private static final FastVector ATTRIBUTES = (FastVector) BASIC_ATTRS.copy();
	
	static {
		ATTRIBUTES.addElement(new Attribute("numCommits"));
		ATTRIBUTES.addElement(new Attribute("numAuthors"));
		ATTRIBUTES.addElement(new Attribute("totalChanges"));
		ATTRIBUTES.addElement(new Attribute("totalEntities"));
		ATTRIBUTES.addElement(new Attribute("totalAuthorCommits"));
		ATTRIBUTES.addElement(new Attribute("totalAuthorChanges"));
	}
	
	private Set<String> authors;

	/**
	 * Construct a new GDCG data set builder.
	 * 
	 * @param initialLevel		Bug-proneness the whole chunk
	 * @param ratio				Bug-proneness decrease ratio
	 * @param bugfixesIncluded	Should the created bulder include bugfix changes in produced instances
	 */
	public GDCGDataSetBuilder(double initialLevel, double ratio, boolean bugfixesIncluded) {
		super(initialLevel, ratio, bugfixesIncluded);
		this.authors = new HashSet<String>();
	}
	
	/**
	 * Construct a new GDCG data set builder (inital level = 1.0, bugfixes not included).
	 * 
	 * @param ratio Bug-proneness decrease ratio
	 */
	public GDCGDataSetBuilder(double ratio) {
		this(1.0, ratio, false);
	}
	
	@Override
	public FastVector getAttributes() {
		return ATTRIBUTES;
	}
	
	@Override
	protected void processChunk(List<StructureEntityVersion> versions, boolean isFixed) {
		this.changeCounter.reset();
		int i = versions.size() - 2 + (this.bugfixesIncluded() ? 0 : 1);
		int numCommits = 0;
		int totalChanges = 0;
		int totalEntities = 0;
		int totalAuthorCommits = 0;
		int totalAuthorChanges = 0;
		this.authors.clear();
		
		for (StructureEntityVersion version: versions) {
			double bugProneness = isFixed ? (i < 0 ? 0.0 : this.getBugProneness(i)) : Instance.missingValue();
			int[] changeCounts = this.changeCounter.countChanges(version);
			double[] attrValues = this.getAttrValues(version, bugProneness, changeCounts);
			attrValues[this.getNumAttrs() - 6] = numCommits;
			
			CommitInfo commitInfo = this.getCommitInfo(version);
			AuthorInfo authorInfo = this.getAuthorInfo(commitInfo.getAuthor());
			totalChanges += commitInfo.getNumChanges();
			totalEntities += commitInfo.getNumEntities();
			totalAuthorCommits += authorInfo.getNumCommits();
			totalAuthorChanges += authorInfo.getNumChanges();
			this.authors.add(commitInfo.getAuthor());
			attrValues[this.getNumAttrs() - 5] = this.authors.size();
			attrValues[this.getNumAttrs() - 4] = totalChanges;
			attrValues[this.getNumAttrs() - 3] = totalEntities;
			attrValues[this.getNumAttrs() - 2] = totalAuthorCommits;
			attrValues[this.getNumAttrs() - 1] = totalAuthorChanges;
			
			Instance instance = new Instance(1.0, attrValues);
			this.addToResult(instance);
			
			--i;
			++numCommits;
		}
	}

}
