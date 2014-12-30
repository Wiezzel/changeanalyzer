package pl.edu.mimuw.changeanalyzer.extraction;

/**
 * Class containing relevant information about a commit
 */
public class CommitInfo {

	private String id;
	private String author;
	private int time;
	private boolean fix;
	
	/**
	 * Create a new CommitInfo.
	 * 
	 * @param id		Commit ID (SHA-1)
	 * @param author	Name of the author of the commit
	 * @param time		Commit time in seconds since epoch
	 * @param fix		Is this commit a bugfix
	 */
	public CommitInfo(String id, String author, int time, boolean fix) {
		this.id = id;
		this.author = author;
		this.time = time;
		this.fix = fix;
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
	
}
