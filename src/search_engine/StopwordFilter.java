package search_engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This class provides filter service for stop words. <br/>
 * The list of stop words can be manually provided or retrieved in a default file, which contains English stop words. <br/>
 * <br/>
 * <b>Note:</b><br/>
 * 1. Require Stemmer when instantiate to be consistent <br/>
 * 2. Need to be explicitly initialise by trigger initialize() method <br/>
 * <u>For example:</u><br/>
 * <pre>
 * {@code
 * //pass the stemmer to be used by Indexer on instantiate
 * StopwordFilter filter = new StopwordFilter (stemmer);
 * filter.initialize();
 * }
 * </pre>
 * 
 * @author ngtrhieu0011
 *
 */
public class StopwordFilter {
	private final String DEFAULT_STOPWORD_FILE = "";
	
	private String _stopwordFile;
	private IStemmer _stemmer;
	private ArrayList<String> _stopwordList;
	
	/**
	 * Default Constructor <br/>
	 * Provide the stemmer for consistency <br/>
	 * Use default stop word list <br/>
	 * @param stemmer the stemmer used in Indexer
	 */
	public StopwordFilter (IStemmer stemmer) {
		_stemmer = stemmer;
		_stopwordFile = DEFAULT_STOPWORD_FILE;
		_stopwordList = new ArrayList<String>();
	}
	
	/**
	 * Secondary Constructor <br/>
	 * Provide the stemmer for consistency <br/>
	 * Use custom stop word list, provided in a file <br/>
	 * @param stemmer the stemmer used in Indexer
	 * @param stopwordFile the file contains custom stop words. These words must be separated by new line (i.e: one word per line)
	 */
	public StopwordFilter (IStemmer stemmer, String stopwordFile) {
		_stemmer = stemmer;
		_stopwordFile = stopwordFile;
		_stopwordList = new ArrayList<String>();
	}
	
	/**
	 * Initialize method <br/>
	 * Need to be explicitly triggered by the Instantiator <br/>
	 * @throws	IOException when cannot fetch the stop word list
	 */
	public void initialize () throws IOException {
		fetchStopwordList ();
		stemStopwordList ();
	}
	
	/**
	 * Fetch the stop word list from file <br/>
	 * The file can either be the default file or a custom file provided by developers <br/>
	 * @throws IOException when the file cannot be opened, or IO errors occur
	 */
	private synchronized void fetchStopwordList () throws IOException {
		// Initialise the Stream readers
		FileInputStream fis = new FileInputStream (_stopwordFile);
		InputStreamReader isr = new InputStreamReader (fis);
		BufferedReader br = new BufferedReader (isr);
		
		// Fetch data from file into local stopwordList
		String nextLine = null;
		do {
			nextLine = br.readLine ();
			if (nextLine != null) {
				_stopwordList.add (nextLine);
			}
		} while (nextLine != null);
		
		// Close stream readers
		br.close ();
		isr.close ();
		fis.close ();
	}
	
	/**
	 * Stem the stop word list using stemmer provided to ensure consistency between Filter and Indexer <br/>
	 */
	private void stemStopwordList () {
		for (String word : _stopwordList) {
			word = _stemmer.stem (word);
		}
	}
	
	/**
	 * Check whether the provided word is in the stop word list
	 * @param	word	to check whether it is a stop word 
	 * @return	true	if the word is found in the list
	 * 			false	if the word cannot be found in the list
	 */
	public boolean isStopword (String word) {
		// Ensure that the word is properly stemmed
		word = _stemmer.stem(word);
		
		boolean isStopword = _stopwordList.contains(word);
		
		return isStopword;
		
	}
}
