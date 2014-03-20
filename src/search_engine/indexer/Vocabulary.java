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
	private String _word;
	private int _df;

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
}
