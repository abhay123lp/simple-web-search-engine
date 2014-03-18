package search_engine;

/**
 * This class provides an implementation of a tuple {@code<docId, tf>} in which
 * tf is term frequency: number of appearance of a particular vocabulary in the
 * document with stored docId <br/>
 * 
 * @author ngtrhieu0011
 * 
 */
class PostTuple implements Comparable<PostTuple> {
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
}