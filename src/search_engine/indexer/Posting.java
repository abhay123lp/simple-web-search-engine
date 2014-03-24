package search_engine.indexer;

import java.util.ArrayList;

/**
 * Posting provides a local posting, which will contains a list of document ID
 * in which a word appear <br/>
 * This class supports <b>Comparable</b>, which used to sort Posting based on
 * the vocabularyId the Posting has <br/>
 * 
 * @author ngtrhieu0011
 * 
 */
class Posting implements Comparable<Posting> {
	private final String DELIMITOR = " ";

	private int _vocabularyId;
	private ArrayList<PostTuple> _posting;

	/**
	 * Public Constructor <br/>
	 * Construct an empty Posting with a vocabularyId <br/>
	 * 
	 * @param vocabularyId
	 */
	public Posting(int vocabularyId) {
		_vocabularyId = vocabularyId;
		_posting = new ArrayList<PostTuple>();
	}

	/**
	 * Protected Constructor <br/>
	 * This is called by the Posting List to translate the Posting stored as
	 * String in file to an actual Posting object <br/>
	 * 
	 * @param vocabularyId
	 *            the vocabularyId of the Posting. This is denoted as the line
	 *            number in the file
	 * @param postingString
	 *            the raw string from the file
	 */
	protected Posting(int vocabularyId, String postingString) {
		// Initialise the new empty Posting and assign the vocabularyId
		_vocabularyId = vocabularyId;
		_posting = new ArrayList<PostTuple>();

		// Translate the postingString
		String[] tokens = postingString.split(DELIMITOR);
		for (String token : tokens) {
			PostTuple newTuple = new PostTuple(token);
			_posting.add(newTuple);
		}
	}

	/**
	 * Get the vocabulary id of this posting
	 * 
	 * @return vocabularyId
	 */
	public int getVocabularyId() {
		return _vocabularyId;
	}

	/**
	 * Check whether a docId is already inside this posting <br/>
	 * 
	 * @param docId
	 * @return true if the docId is found inside the posting <br/>
	 *         false if the docId is not found <br/>
	 */
	public boolean isContainDocId(int docId) {
		return _posting.contains(new Integer(docId));
	}

	/**
	 * Add a docId into the posting <br/>
	 * 
	 * @param docId
	 */
	public void addDocId(int docId) {
		// Assume that the posting list is always sorted

		// If the last item is smaller than docId: append the docId to the list
		PostTuple lastTuple = _posting.get(_posting.size() - 1);
		if (lastTuple.getDocId() < docId) {
			_posting.add(new PostTuple(docId));
		} else if (lastTuple.getDocId() == docId) {
			lastTuple.increaseTf();
		} else {
			// Scan the posting list and find the place to insert
			for (int i = 0; i < _posting.size() - 1; i++) {
				PostTuple preTuple = _posting.get(i);
				PostTuple nextTuple = _posting.get(i + 1);
				if (preTuple.getDocId() == docId) {
					preTuple.increaseTf();
					return; // already exist, quit
				}
				if (preTuple.getDocId() < docId && docId < nextTuple.getDocId()) {
					_posting.add(i, new PostTuple(docId)); // insert and quit
					return;
				}
			}
		}
	}

	/**
	 * Comparison method
	 */
	public int compareTo(Posting target) {
		return _vocabularyId - target.getVocabularyId();
	}

	/**
	 * Translate the current Posting into String that can be stored into file <br/>
	 */
	public String toString() {
		// Empty postingString
		String postingString = "";

		for (PostTuple tuple : _posting) {
			postingString += tuple.toString() + DELIMITOR;
		}

		postingString.trim();

		return postingString;
	}

	/**
	 * Static Service <br/>
	 * Merge two Postings together <br/>
	 * The input Postings need to have the same vocabularyId <br/>
	 * 
	 * @param p1
	 *            Posting 1
	 * @param p2
	 *            Posting 2
	 * @return the merge posting, null if p1 and p2 have different vocabularyId
	 */
	public static Posting merge(Posting p1, Posting p2) {
		// TODO unimplemented method
		return null;
	}
}