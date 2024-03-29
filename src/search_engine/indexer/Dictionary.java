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
 * <b> Singleton class </b><br/>
 * <br/>
 * The dictionary store all the known Vocabulary as a tuple
 * {@code <vocabulary,df>} where df is <b>document frequency</b>: number of
 * documents this word has appeared. <br/>
 * <br/>
 * <b>Note: The Dictionary needs to be close () when done to save all the
 * changes into files</b> <br/>
 * 
 * @author ngtrhieu0011
 */

class Dictionary {
	private final String DICTONARY_FILENAME = "dictionary.txt";

	// Singleton dictionary
	private static ArrayList<Vocabulary> _dictionary = new ArrayList<Vocabulary>();
	private static int _no_instances = 0;

	private boolean _isInitialized = false;

	public Dictionary() throws IOException {
		start();
	}

	/**
	 * Initialise the Dictionary <br/>
	 * Fetch the dictionary from the disk if the dictionary does not exist <br/>
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 */
	public void start() throws IOException {
		synchronized (_dictionary) {
			_no_instances++;
			if (_no_instances == 1) {
				fetchDictionary();
			}
			_isInitialized = true;
		}
	}

	/**
	 * Close the Dictionary <br/>
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 * 
	 */
	public void close() throws IOException {
		synchronized (_dictionary) {
			_no_instances--;
			if (_no_instances == 0) {
				writeToFile();
			}
			_isInitialized = false;
		}
	}

	/**
	 * Get Vocabulary Id of a word <br/>
	 * The word should be stemmed properly <br/>
	 * 
	 * @param word
	 *            the String contains a stemmed word
	 * @return id if the word is found in the list <br/>
	 *         -1 if no such word or the Dictionary hasn't initialised/has been
	 *         closed <br/>
	 */
	public int getVocabularyId(String word) {
		if (_isInitialized) {
			Vocabulary target = new Vocabulary(word);
			synchronized (_dictionary) {
				for (int i = 0; i < _dictionary.size(); i++) {
					Vocabulary vocabulary = _dictionary.get(i);
					if (vocabulary.compareTo(target) == 0) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Return a Vocabulary according to the vocabularyId <br/>
	 * 
	 * @param vocabularyId
	 * @return Vocabulary if found, null if not found or the Dictionary hasn't
	 *         initialised/has been closed <br/>
	 */
	public Vocabulary getVocabulary(int vocabularyId) {
		if (_isInitialized) {
			synchronized (_dictionary) {
				if (vocabularyId < _dictionary.size()) {
					return _dictionary.get(vocabularyId);
				}
			}
		}
		return null;
	}

	/**
	 * Check for the dictionary for the token and return its vocabulary id <br/>
	 * If the token does not exist, create a new vocabulary and set the df to 0 <br/>
	 * 
	 * @param token
	 * @return vocabularyID, -1 if the vocabulary is not found or the Dictionary
	 *         hasn't initialised/has been closed <br/>
	 */
	public int checkAndAddWord(String token) {
		if (_isInitialized) {
			// Create a new temporary vocabulary
			Vocabulary newVocabulary = new Vocabulary(token, 0);

			int vocabularyId = -1;

			// Find the similar vocabulary in the Dictionary
			// If found the increase the df of that word and return its index
			synchronized (_dictionary) {
				for (Vocabulary vocabulary : _dictionary) {
					if (vocabulary.compareTo(newVocabulary) == 0) {
						vocabularyId = _dictionary.indexOf(vocabulary);
						break;
					}
				}

				// Cannot be found: add newVocaburary into the dictionary and
				// return
				// its index
				if (vocabularyId == -1) {
					_dictionary.add(newVocabulary);
					vocabularyId = _dictionary.indexOf(newVocabulary);
				}
			}

			return vocabularyId;

		} else {
			return -1;
		}
	}

	/**
	 * Fetch the dictionary from file and store into local memory <br/>
	 * 
	 * @throws IOEcxeption
	 *             when the file cannot be opened
	 */
	private synchronized void fetchDictionary() throws IOException {
		// Initialise the Stream readers
		FileInputStream fis = new FileInputStream(DICTONARY_FILENAME);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);

		// Empty the dictionary
		// TODO should not initialise the dictionary (same as documentList's problem)
		_dictionary.clear();

		// Fetch data from file into local dictionary
		String nextLine = null;
		do {
			nextLine = br.readLine();
			Vocabulary newVocabulary = Vocabulary.parseToVocabulary(nextLine);
			if (newVocabulary != null) {
				_dictionary.add(newVocabulary);
			}
		} while (nextLine != null);

		// Close stream readers
		br.close();
		isr.close();
		fis.close();
	}

	/**
	 * Update the local Dictionary to file
	 * 
	 * @throws IOException
	 *             when the file cannot be openned
	 */
	private synchronized void writeToFile() throws IOException {
		// Initialise the Stream writers
		FileOutputStream fos = new FileOutputStream(DICTONARY_FILENAME, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);

		// Write each vocabulary to each line of the file.
		for (Vocabulary vocabulary : _dictionary) {
			bw.write(vocabulary.toString() + "\n");
		}

		// Close stream writers
		bw.close();
		osw.close();
		fos.close();
	}

	/**
	 * Increase the df of a particular document
	 * 
	 * @param docId
	 *            of the document
	 */
	public void increaseDocFreq(int docId) {
		synchronized (_dictionary) {
			Vocabulary vocabulary = getVocabulary(docId);
	
			if (vocabulary != null) {
				vocabulary.increaseDocFreq();
			}
		}

	}
}
