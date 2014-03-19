package search_engine;

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
	private static ArrayList<Vocabulary> _dictionary = null;
	private static int _no_instances = 0;

	private boolean _isInitialzed = false;

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
		if (_no_instances == 0) {
			fetchDictionary();
		}
		_no_instances++;
		_isInitialzed = true;
	}

	/**
	 * Close the Dictionary <br/>
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 * 
	 */
	public void close() throws IOException {
		_no_instances--;
		if (_no_instances == 0) {
			writeToFile();
			_dictionary = null;
		}
		_isInitialzed = false;
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
		if (_isInitialzed) {
			Vocabulary target = new Vocabulary(word);
			for (int i = 0; i < _dictionary.size(); i++) {
				Vocabulary vocabulary = _dictionary.get(i);
				if (vocabulary.compareTo(target) == 0) {
					return i;
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
		if (_isInitialzed) {
			if (vocabularyId < _dictionary.size()) {
				return _dictionary.get(vocabularyId);
			}
		}
		return null;
	}

	/**
	 * Check for the dictionary for the token <br/>
	 * If the token exists in the dictionary, increase its df <br/>
	 * Otherwise, create a new vocabulary and set the df to 1 <br/>
	 * Return the vocabularyID of the token in the dictionary <br/>
	 * 
	 * @param token
	 * @return vocabularyID, -1 if the vocabulary is not found or the Dictionary
	 *         hasn't initialised/has been closed <br/>
	 */
	public int checkAndAddWord(String token) {
		if (_isInitialzed) {
			// Create a new temporary vocabulary
			Vocabulary newVocabulary = new Vocabulary(token);

			int vocabularyId = -1;

			// Find the similar vocabulary in the Dictionary
			// If found the increase the df of that word and return its index
			for (Vocabulary vocabulary : _dictionary) {
				if (vocabulary.equals(newVocabulary)) {
					vocabularyId = _dictionary.indexOf(vocabulary);
					vocabulary.increaseDocFreq();
					break;
				}
			}

			// Cannot be found: add newVocaburary into the dictionary and return
			// its
			// index
			if (vocabularyId == -1) {
				_dictionary.add(newVocabulary);
				vocabularyId = _dictionary.indexOf(newVocabulary);
			}

			return vocabularyId;

		} else {
			return -1;
		}
	}

	/**
	 * Parse a line from dictionary file into a Vocabulary <br/>
	 * Extract both the word (1st token) and df (2nd token) to create a new
	 * Vocabulary <br/>
	 * 
	 * @param line
	 *            from dictionary
	 * @return <b>Vocabulary</b> the parsed Vocabulary <br/>
	 *         <b>null</b> if the <b>line</b> is syntactically incorrect
	 */
	private Vocabulary parseToVocabulary(String line) {
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

		// Initialise empty dictionary
		_dictionary = new ArrayList<Vocabulary>();

		// Fetch data from file into local dictionary
		String nextLine = null;
		do {
			nextLine = br.readLine();
			Vocabulary newVocabulary = parseToVocabulary(nextLine);
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
		for (int vocabularyId = 0; vocabularyId < _dictionary.size(); vocabularyId++) {
			Vocabulary vocabulary = getVocabulary(vocabularyId);
			bw.write(vocabulary.word() + " " + vocabulary.df() + "/n");
		}

		// Close stream writers_isInitialzed
		bw.close();
		osw.close();
		fos.close();
	}
}
