package search_engine;

import java.util.ArrayList;

/**
 * This class provides implementation of a PostingList and services related to
 * PostingList</br> The core data structure is a List of Posting <br/>
 * 
 * @author ngtrhieu0011
 */
class PostingList {
	private ArrayList<Posting> _postingList;

	/**
	 * Public Constructor <br/>
	 * Create a local empty PostingList <br/>
	 */
	public PostingList() {
		_postingList = new ArrayList<Posting>();
	}

	/**
	 * Create a new empty Posting and insert into the list <br/>
	 * Return the created Posting <br/>
	 * If there is another Posting with the same vocabularyId, the empty Posting
	 * will not be added <br/>
	 * 
	 * @param vocabularyId
	 * @return the Posting just added. If there is another Posting with the same
	 *         vocabularyId, return that instead
	 */
	public Posting addAndGetPosting(int vocabularyId) {
		Posting newPosting = new Posting(vocabularyId);

		if (_postingList.isEmpty()) {
			_postingList.add(newPosting);
			return newPosting;
		} else {
			for (Posting posting : _postingList) {
				if (posting.compareTo(newPosting) == 0) {
					return posting;
				}
				if (posting.compareTo(newPosting) > 0) {
					int index = _postingList.indexOf(posting);
					_postingList.add(index, newPosting);
					return newPosting;
				}
			}
		}

		_postingList.add(newPosting);
		return newPosting;
	}

	/**
	 * Get the posting with the specific vocabularyId <br/>
	 * 
	 * @param vocabularyId
	 * @return the posting with the specific id, null if cannot find that
	 *         posting
	 */
	public Posting getPosting(int vocabularyId) {
		for (Posting posting : _postingList) {
			if (posting.getVocabularyId() == vocabularyId) {
				return posting;
			}
		}
		return null;
	}

	/**
	 * Update the local Document List to file
	 */
	public synchronized void writeToFile() {
		// TODO: un-implement method
	}
}
