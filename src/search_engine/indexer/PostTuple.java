package search_engine.indexer;

/**
 * This class provides an implementation of a tuple {@code<docId, tf>} in which
 * tf is term frequency: number of appearance of a particular vocabulary in the
 * document with stored docId <br/>
 * 
 * @author ngtrhieu0011
 * 
 */
class PostTuple implements Comparable<PostTuple> {
	private final String DELIMITOR = ",";
	private int _docId;
	private int _tf;

	/**
	 * Constructor <br/>
	 * Construct a new tuplet with a docId given <br/>
	 * tf will be initialised to be 1 <br/>
	 * 
	 * @param docId
	 */
	public PostTuple(int docId) {
		_docId = docId;
		_tf = 1;
	}

	/**
	 * Constructor <br/>
	 * Construct a new tuplet with docId and tf given <br/>
	 * 
	 * @param docId
	 * @param tf
	 */
	public PostTuple(int docId, int tf) {
		_docId = docId;
		_tf = tf;
	}

	/**
	 * Protected Constructor <br/>
	 * This is called by the Posting to attempt read and construct postings from
	 * file <br/>
	 * 
	 * <b>Note</b> that the tupleString must have the format: <br/>
	 * 
	 * <pre>
	 * df,tf
	 * </pre>
	 * 
	 * where df and tf are parse-able integer numbers in string format. Fail is
	 * assumption and the program will crash <br/>
	 * 
	 * @param tupleString
	 *            the raw String from the file
	 */
	protected PostTuple(String tupleString) {
		String[] tokens = tupleString.split(DELIMITOR);
		try {
			_docId = Integer.parseInt(tokens[0]);
			_tf = Integer.parseInt(tokens[1]);
		} catch (NumberFormatException e) {
			// This assert will be false when the format of tupleString is not
			// standard
			assert false;
		}
	}

	/**
	 * Getter for docId
	 * 
	 * @return docId
	 */
	public int getDocId() {
		return _docId;
	}

	/**
	 * Getter for tf
	 * 
	 * @return tf
	 */
	public int getTf() {
		return _tf;
	}

	/**
	 * Increase the tf by 1
	 */
	public void increaseTf() {
		_tf++;
	}

	/**
	 * Comparison method
	 */
	public int compareTo(PostTuple target) {
		return _docId - target.getDocId();
	}

	/**
	 * Translate the current PostTuple into String that can be stored into file <br/>
	 */
	public String toString() {
		return _docId + DELIMITOR + _tf;
	}

	/**
	 * Merge 2 tuples <br/>
	 * The tuples need to have the same docId <br/>
	 * @param tuple1
	 * @param tuple2
	 * @return merged tuple or null if 2 tuples are not having the same docId
	 */
	protected static PostTuple merge(PostTuple tuple1, PostTuple tuple2) {
		if (tuple1.getDocId() == tuple2.getDocId()) {
			return new PostTuple (tuple1.getDocId(), tuple1.getTf() + tuple2.getTf());
		}
		return null;
	}
}