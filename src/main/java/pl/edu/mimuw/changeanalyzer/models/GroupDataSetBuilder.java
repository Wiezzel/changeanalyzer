package pl.edu.mimuw.changeanalyzer.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.edu.mimuw.changeanalyzer.extraction.AuthorInfo;
import pl.edu.mimuw.changeanalyzer.extraction.CommitInfo;
import pl.edu.mimuw.changeanalyzer.models.Attributes.AttributeValues;
import weka.core.Attribute;
import weka.core.Instance;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


/**
 * This data set builder class uses commit group as model. Each 
 * produced instance represents a group of subsequent commits starting with
 * a commit directly succeeding a bug-fix. Groups contain from one commit
 * up to a whole chunk.
 * 
 * @author Adam Wierzbicki
 */
public class GroupDataSetBuilder extends ChunkDataSetBuilder {
	
	public static final Attribute NUM_COMMITS = new Attribute("numCommits");
	public static final Attribute NUM_AUTHORS = new Attribute("numAuthors");
	public static final Attribute AVG_CHANGES = new Attribute("avgChanges");
	public static final Attribute AVG_ENTITIES = new Attribute("avgEntities");
	public static final Attribute AVG_AUTHOR_COMMITS = new Attribute("avgAuthorCommits");
	public static final Attribute AVG_AUTHOR_CHANGES = new Attribute("avgAuthorChanges");
	
	private Set<String> authors;

	/**
	 * Construct a new group data set builder.
	 */
	public GroupDataSetBuilder() {
		super(false);
		this.authors = new HashSet<String>();
		
		this.attributes.addAttribute(NUM_COMMITS);
		this.attributes.addAttribute(NUM_AUTHORS);
		this.attributes.addAttribute(AVG_CHANGES);
		this.attributes.addAttribute(AVG_ENTITIES);
		this.attributes.addAttribute(AVG_AUTHOR_COMMITS);
		this.attributes.addAttribute(AVG_AUTHOR_CHANGES);
	}
	
	@Override
	protected void processChunk(List<StructureEntityVersion> versions, boolean isFixed) {
		this.changeCounter.reset();
		int index = 0;
		int totalChanges = 0;
		int totalEntities = 0;
		int totalAuthorCommits = 0;
		int totalAuthorChanges = 0;
		this.authors.clear();
		
		for (StructureEntityVersion version: versions) {
			AttributeValues values = this.getAttrValues(version, index, isFixed);
			
			CommitInfo commitInfo = this.getCommitInfo(version);
			AuthorInfo authorInfo = this.getAuthorInfo(commitInfo.getAuthor());
			totalChanges += commitInfo.getNumChanges();
			totalEntities += commitInfo.getNumEntities();
			totalAuthorCommits += authorInfo.getNumCommits();
			totalAuthorChanges += authorInfo.getNumChanges();
			this.authors.add(commitInfo.getAuthor());
			int numCommits = ++index;
			int numAuthors = this.authors.size();
			
			values.setAttributeValue(NUM_COMMITS, numCommits);
			values.setAttributeValue(NUM_AUTHORS, numAuthors);
			values.setAttributeValue(AVG_CHANGES, (double) totalChanges / numCommits);
			values.setAttributeValue(AVG_ENTITIES, (double) totalEntities / numCommits);
			values.setAttributeValue(AVG_AUTHOR_COMMITS, (double) totalAuthorCommits / numCommits);
			values.setAttributeValue(AVG_AUTHOR_CHANGES, (double) totalAuthorChanges / numCommits);
			
			Instance instance = new Instance(1.0, values.getValues());
			this.addToResult(instance);
		}
	}

}
