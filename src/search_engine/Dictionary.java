package search_engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * The dictionary store all the known Vocabulary as a tuple
 * {@code <vocabulary,df>} where df is <b>document frequency</b>: number of
 * documents this word has appeared. <br/>
 */

class Dictionary {
    private final String DICTONARY_FILENAME = "dictionary.txt";

    private ArrayList<Vocabulary> _dictionary;

    /**
     * Fetch the dictionary from file and store into local memory <br/>
     * 
     * @throws IOEcxeption
     *             when the file cannot be opened
     */
    public synchronized void fetchDictionary() throws IOException {
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

    public synchronized void updateDictionary() {

    }

    /**
     * Get Vocabulary Id of a word <br/>
     * The word should be stemmed properly <br/>
     * 
     * @param word
     *            the String contains a stemmed word
     * @return id if the word is found in the list <br/>
     *         -1 if no such word
     */
    public int getVocabularyId(String word) {
	Vocabulary target = new Vocabulary(word);
	for (int i = 0; i < _dictionary.size(); i++) {
	    Vocabulary vocabulary = _dictionary.get(i);
	    if (vocabulary.compareTo(target) == 0) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Return a Vocabulary according to the vocabularyId <br/>
     * 
     * @param vocabularyId
     * @return Vocabulary if found <br/>
     *         null if not found
     */
    public Vocabulary getVocabulary(int vocabularyId) {
	if (vocabularyId < _dictionary.size()) {
	    return _dictionary.get(vocabularyId);
	} else {
	    return null;
	}
    }

    /**
     * Check for the dictionary for the token <br/>
     * If the token exists in the dictionary, increase its df <br/>
     * Otherwise, create a new vocabulary and set the df to 1 <br/>
     * Return the vocabularyID of the token in the dictionary <br/>
     * 
     * @param token
     * @return vocabularyID
     */
    public int checkAndAddWordIntoDictionary(String token) {
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

	// Cannot be found: add newVocaburary into the dictionary and return its
	// index
	if (vocabularyId == -1) {
	    _dictionary.add(newVocabulary);
	    vocabularyId = _dictionary.indexOf(newVocabulary);
	}

	return vocabularyId;
    }

    /**
     * Parse a line from dictionary file into a Vocabulary <br/>
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
}
