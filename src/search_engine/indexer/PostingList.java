package search_engine.indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * This class provides implementation of a PostingList and services related to
 * PostingList</br> The core data structure is a List of Posting <br/>
 * 
 * @author ngtrhieu0011
 */
class PostingList {
	private final static String POSTING_LIST_FILE = "postings.txt";
	private static Object fileLock = new Object();
	private ArrayList<Posting> _postingList;

	/**
	 * Public Constructor <br/>
	 * Create a local empty PostingList <br/>
	 */
	public PostingList() {
		_postingList = new ArrayList<Posting>();
	}

	/**
	 * Update the local Document List to file <br/>
	 * This method will need to first retrieve the postingList that already on
	 * the file <br/>
	 * Then merge itself to the postingList on file <br/>
	 * And write it back to file <br/>
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 */
	public void writeToFile() throws IOException {
		synchronized (fileLock) {
			// Get postingList from file
			PostingList postingListOnFile = readFromFile();

			// Merge with the current postingList
			postingListOnFile.mergeWith(this);

			// Write postingList to file
			writeToFile(postingListOnFile);
		}
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
	 * Get a Posting based on its index
	 * 
	 * @param index
	 *            of the Posting
	 * 
	 * @return the Posting with the specified index. Null if IndexOutOfBound
	 *         exception is thrown
	 */
	public Posting getPostingAtIndex(int index) {
		if (index < _postingList.size()) {
			return _postingList.get(index);
		} else {
			return null;
		}
	}

	/**
	 * Merge its own postingList with another postingList <br/>
	 * 
	 * @param postingList
	 *            to be merged with
	 */
	private void mergeWith(PostingList postingList) {
		int i = 0;
		while (true) {
			Posting targetPosting = postingList.getPostingAtIndex(i);
			i++;
			if (targetPosting == null) {
				break;
			}

			int vocabId = targetPosting.getVocabularyId();

			Posting posting = getPosting(vocabId);

			if (posting != null) {
				Posting mergedPosting = Posting.merge(posting, targetPosting);
				add(mergedPosting);
			} else {
				add(targetPosting);
			}
		}
	}

	/**
	 * Add a Posting into the postingList <br/>
	 * If there is another Posting with the same vocabularyId, that Posting will
	 * be replaced <br/>
	 * 
	 * @param posting
	 *            to be added
	 * 
	 */
	private void add(Posting posting) throws IllegalArgumentException {
		if (_postingList.size() > 0) {
			for (Posting p : _postingList) {
				if (p.compareTo(posting) == 0) {
					int index = _postingList.indexOf(p);
					_postingList.remove(index);
					_postingList.add(index, posting);
					return;
				}
				if (p.compareTo(posting) > 0) {
					int index = _postingList.indexOf(p);
					_postingList.add(index, posting);
					return;
				}
			}
			_postingList.add(posting);
		} else {
			_postingList.add(posting);
		}
	}

	/**
	 * Return a PostingList that on the File <br/>
	 * 
	 * @return PostingList that on the file
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 */
	private static PostingList readFromFile() throws IOException {
		synchronized (fileLock) {
			// Initialise the Stream readers
			FileInputStream fis = new FileInputStream(POSTING_LIST_FILE);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);

			// Initialise an empty PostingList
			PostingList postingList = new PostingList();

			// Fetch data from file into local dictionary
			String nextLine = null;
			int i = 0;
			do {
				nextLine = br.readLine();
				if (nextLine != null) {
					Posting newPosting = new Posting(i, nextLine);
					if (newPosting != null) {
						try {
							postingList.add(newPosting);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
					}
				}

				i++;
			} while (nextLine != null);

			// Close stream readers
			br.close();
			isr.close();
			fis.close();

			return postingList;
		}
	}

	/**
	 * Erase the file and Write this postingList to file <br/>
	 * 
	 * @param postingList
	 *            to be written to file
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 */
	private void writeToFile(PostingList postingList) throws IOException {
		synchronized (fileLock) {
			// Initialise the Stream writers
			FileOutputStream fos = new FileOutputStream(POSTING_LIST_FILE, false);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			// Write each vocabulary to each line of the file.

			for (int i = 0; postingList.getPostingAtIndex(i) != null; i++) {
				Posting posting = postingList.getPostingAtIndex(i);
				bw.write(posting.toString() + "\n");
			}

			// Close stream writers_isInitialzed
			bw.close();
			osw.close();
			fos.close();
		}
	}
}
