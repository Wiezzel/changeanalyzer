package pl.edu.mimuw.changeanalyzer.extraction;


/**
 * Class containing relevant information about a commit
 */
public class CommitInfo {

	private String id;
	private String author;
	private int time;
	private boolean fix;
	private int numChanges;
	private int numEntities;
	
	/**
	 * Create a new CommitInfo.
	 * 
	 * @param id			Commit ID (SHA-1)
	 * @param author		Name of the author of the commit
	 * @param time			Commit time in seconds since epoch
	 * @param fix			Is this commit a bugfix
	 * @param numChanges	Number of fine-grained changes in this commit
	 * @param numEntities	Number of entities (methods) changed in this commit
	 */
	public CommitInfo(String id, String author, int time, boolean fix, int numChanges, int numEntities) {
		this.id = id;
		this.author = author;
		this.time = time;
		this.fix = fix;
		this.numChanges = numChanges;
		this.numEntities = numEntities;
	}

	/**
	 * Get commit ID (SHA-1).
	 * 
	 * @return Commit ID
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Get name of the author of the commit.
	 * 
	 * @return Name of the author of the commit
	 */
	public String getAuthor() {
		return this.author;
	}
	
	/**
	 * Get time of the commit.
	 * 
	 * @return Commit time in seconds since epoch
	 */
	public int getTime() {
		return this.time;
	}

	/**
	 * Check, whether the commit is a bugfix.
	 * 
	 * @return True iff the commit is a bugfix
	 */
	public boolean isFix() {
		return this.fix;
	}

	/**
	 * Get the number of fine-grained changes in this commit.
	 * 
	 * @return Number of changes in this commit
	 */
	public int getNumChanges() {
		return this.numChanges;
	}

	/**
	 * Get the number of entities (methods) changed in this commit.
	 * 
	 * @return Number of changed entities
	 */
	public int getNumEntities() {
		return numEntities;
	}
	
	/**
	 * Add new changed entity (update number of changes & number of changed entities).
	 * 
	 * @param numChanges Number of changess
	 */
	public void addChangedEntity(int numChanges) {
		this.numChanges += numChanges;
		this.numEntities++;
	}

}
