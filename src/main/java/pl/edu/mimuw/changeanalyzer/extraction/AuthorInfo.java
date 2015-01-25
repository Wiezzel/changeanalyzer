package pl.edu.mimuw.changeanalyzer.extraction;

/**
 * Class containing relevant information about an author
 * 
 * @author Adam Wierzbicki
 */
public class AuthorInfo {
	
	private String name;
	private String mail;
	private int numCommits;
	private int numChanges;
	
	/**
	 * Construct a new AuthorInfo.
	 * 
	 * @param name			Name of the author
	 * @param mail			E-mail address of the author
	 * @param numCommits	Total number of commits contributed by the author 
	 * @param numChanges	Total number of fine-grained changes done by the author
	 */
	public AuthorInfo(String name, String mail, int numCommits, int numChanges) {
		this.name = name;
		this.mail = mail;
		this.numCommits = numCommits;
		this.numChanges = numChanges;
	}
	
	/**
	 * Construct a new AuthorInfo. Set initial number of commits & changes to 0.
	 * 
	 * @param name			Name of the author
	 * @param mail			E-mail address of the author
	 */
	public AuthorInfo(String name, String mail) {
		this(name, mail, 0, 0);
	}

	/**
	 * Get name of the author.
	 * 
	 * @return Author's name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get e-mail address of the author.
	 * 
	 * @return Author's email
	 */
	public String getMail() {
		return this.mail;
	}
	
	/**
	 * Get total number of commits contributed by the author.
	 * 
	 * @return Number of commits
	 */
	public int getNumCommits() {
		return this.numCommits;
	}
	
	/**
	 * Get total number of fine-grained changes done by the author.
	 * 
	 * @return Number of changes
	 */
	public int getNumChanges() {
		return this.numChanges;
	}
	
	/**
	 * Add a new commit by this author (update numCommits & numChanges counters).
	 * 
	 * @param numChanges Number of changes in the commit
	 */
	public void addCommit(int numChanges) {
		this.numCommits++;
		this.numChanges += numChanges;
	}

}
