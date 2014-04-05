package search_engine.indexer;

/**
 * The private implementation of Vocabulary in Dictionary which basically a
 * implementation of {@code <word, df>} where <b>word</b> is the stemmed token
 * and <b>df</b> is the number of documents the word appeared in<br/>
 * The class supports <b>Comparable</b>, which basically compare the word of the
 * Vocabulary <br/>
 * 
 * @author ngtrhieu0011
 */
class Vocabulary implements Comparable<Vocabulary> {
	private final String DELIMITOR = " ";
	
	private String _word;
	private int _df;
	
	/**
	 * Static Helper - Constructor <br/>
	 * Parse a line from dictionary file into a Vocabulary <br/>
	 * Extract both the word (1st token) and df (2nd token) to create a new
	 * Vocabulary <br/>
	 * 
	 * @param line
	 *            from dictionary
	 * @return <b>Vocabulary</b> the parsed Vocabulary <br/>
	 *         <b>null</b> if the <b>line</b> is syntactically incorrect
	 */
	public static Vocabulary parseToVocabulary(String line) {
		Vocabulary vocabulary = null;
		try {
			String[] tokens = line.split(" ");
			String word = tokens[0];
			int df = Integer.parseInt(tokens[1]);
			vocabulary = new Vocabulary(word, df);
		} catch (Exception e) {
			// Catch Parsing Error
			vocabulary = null;
		}
		return vocabulary;
	}

	/**
	 * Create a new Vocabulary <br/>
	 * The df is set to 1 when initialised <br/>
	 * 
	 * @param word
	 */
	public Vocabulary(String word) {
		_word = word;
		_df = 1;
	}

	/**
	 * Create a new Vocabulary and set the document frequency (df)<br/>
	 * 
	 * @param word
	 * @param df
	 */
	public Vocabulary(String word, int df) {
		_word = word;
		_df = df;
	}

	/**
	 * Getter for word
	 * 
	 * @return word
	 */
	public String word() {
		return _word;
	}

	/**
	 * Getter for document frequency (df)
	 * 
	 * @return df
	 */
	public int df() {
		return _df;
	}

	/**
	 * Comparator
	 */
	public int compareTo(Vocabulary target) {
		return _word.compareTo(target.word());
	}

	/**
	 * Increase Document Frequency (df) <br/>
	 * This is called when you register new document <br/>
	 */
	public void increaseDocFreq() {
		_df++;
	}
	
	/**
	 * Increase Document Frequency by some amount <br/>
	 * This is called when you register new document <br/>
	 * 
	 * @param amount
	 */
	public synchronized void increaseDocFreq(int amount) {
		_df+=amount;
	}
	
/**
 * Translate the current Vocabulary into String that can be stored into file <br/>
 */
	public String toString () {
		return _word + DELIMITOR + _df;
	}
}
