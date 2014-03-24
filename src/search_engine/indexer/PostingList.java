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
	public synchronized void writeToFile() throws IOException {
		// Get postingList from file
		PostingList postingListOnFile = readFromFile();

		// Merge with the current postingList
		postingListOnFile.mergeWith(this);

		// Write postingList to file
		writeToFile(postingListOnFile);
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
	 * Merge its own postingList with another postingList <br/>
	 * 
	 * @param postingList
	 *            to be merged with
	 */
	private void mergeWith(PostingList postingList) {
		// TODO: unimplemented method
	}

	/**
	 * Add a Posting into the postingList <br/>
	 * 
	 * @param posting
	 *            to be added
	 * @throws IllegalArgumentException
	 *             when the Posting has already existed in the list. Consider
	 *             using merge() instead
	 */
	private void add(Posting posting) throws IllegalArgumentException {
		for (Posting p : _postingList) {
			if (p.getVocabularyId() == posting.getVocabularyId()) {
				throw new IllegalArgumentException ("Posting with the same VocabularyId existed");
			}
			if (p.getVocabularyId() < posting.getVocabularyId()) {
				int index = _postingList.indexOf(p);
				_postingList.add(index, posting);
				break;
			}
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
			Posting newPosting = new Posting(i, nextLine);
			if (newPosting != null) {
				try {
					postingList.add(newPosting);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
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

	/**
	 * Erase the file and Write this postingList to file <br/>
	 * 
	 * @param postingList
	 *            to be written to file
	 *            
	 * @throws IOException when the file cannot be opened
	 */
	private synchronized void writeToFile(PostingList postingList) throws IOException {
		// Initialise the Stream writers
		FileOutputStream fos = new FileOutputStream(POSTING_LIST_FILE, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);

		// Write each vocabulary to each line of the file.
		for (Posting posting : _postingList) {
			bw.write(posting.toString() + "/n");
		}

		// Close stream writers_isInitialzed
		bw.close();
		osw.close();
		fos.close();
	}
}
