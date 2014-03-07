package search_engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.uima.tokenize.Tokenizer;

/**
 * This class provide index service to index a single document to the database of the search engine. <br/>
 * Indexer accepts documents as a single file or a list of lines from its Instantiator. <br/>
 * <br/>
 * <b>Note:</b> <br/>
 * 1. This class need to explicitly trigger by called start() method after instantiated to begin indexing <br/>
 * <br/>
 * <u>For example: </u><br/>
 * <pre>{@code
 * Indexer indexer = new Indexer (docName, document);
 *indexer.start ();
 * }</pre> <br/>
 * 
 * 2. As this class may take a very long time to finish indexing one document, and 2 Indexers cannot run concurrently, Indexer is recommended to run in a separate Thread to avoid blocking the Instantiator <br/>
 * 
 * @author ngtrhieu0011
 */
public class Indexer {
	/**
	 * The private implementation of Vocabulary in Dictionary which basically a implementation of {@code <word, df>}
	 * 		where <b>word</b> is the stemmed token and <b>df</b> is the number of documents the word apprears in<br/>
	 * The class supports <b>Comparable</b>, which basically compare the word of the Vocabulary <br/>
	 * @author ngtrhieu0011
	 */
	private class Vocabulary implements Comparable<Vocabulary> {
		private String _word;
		private int _df;
		/**
		 * Create a new Vocabulary <br/>
		 * The df is set to 1 when initialised <br/>
		 * @param word
		 */
		public Vocabulary (String word) {
			_word = word;
			_df = 1;
		}
		/**
		 * Create a new Vocabulary and set the document frequency (df)<br/>
		 * @param word
		 * @param df
		 */
		public Vocabulary (String word, int df) {
			_word = word;
			_df = df;
		}
		/**
		 * Getter for word
		 * @return word
		 */
		public String word () {
			return _word;
		}
		/**
		 * Getter for document frequency (df)
		 * @return df
		 */
		public int df () {
			return _df;
		}
		/**
		 * Comparator
		 */
		public int compareTo(Vocabulary target) {
			return _word.compareTo(target.word());
		}
		/**
		 * Increase Document Frequency (df) <br/>
		 * This is called when you register new document <br/>
		 */
		public void increaseDocFreq () {
			_df ++;
		}
	}
	
	private final String DICTONARY_FILENAME = "dictionary.txt";
	private final String DOCUMENT_FILENAME = "documents.txt";
	private final String POSTING_FILENAME = "postings.txt";
	
	private String _docName;
	private ArrayList<String> _indexDocument;
	
	private ArrayList<String> _documentList;
	private ArrayList<Vocabulary> _dictionary;
	private ArrayList<ArrayList<Integer>> _localPosting;
	
	/**
	 * Standard Constructor. <br/>
	 * Pass the document to the Indexer as a file. <br/>
	 * The indexer should only be noticed when the file is ready to use. <br/>
	 * 
	 * @param	docName		specifies the name of the document. In the search engine, this should be the link to the website.<br/>
	 * @param	fileName	specifies the name of the file storing the document. This file should be able to open by the Indexer.<br/>
	 * @throws IOException	when the file cannot be opened or some IO errors happened when reading the file
	 */
	public Indexer (String docName, String fileName) throws IOException {
		// Save the docName into local field
		_docName = docName;
		
		// Initialise the Stream readers
		FileInputStream fis = new FileInputStream (fileName);
		InputStreamReader isr = new InputStreamReader (fis);
		BufferedReader br = new BufferedReader (isr); 
		
		// Initialise empty document
		_indexDocument = new ArrayList<String>();
		
		// Read from stream line by line and store into local document
		String nextLine = null;
		
		do {
			nextLine = br.readLine();
			if (nextLine != null) {
				_indexDocument.add(nextLine);
			}
		} while (nextLine != null);
		
		// Close all the stream reader
		br.close();
		isr.close();
		fis.close();
	}
	
	/**
	 * Recommended Constructor <br/>
	 * This constructor is safer to use, thus recommended. <br/>
	 * Pass the document to the Indexer as a List of String. <br/>
	 * 
	 * @param	docName		specifies the name of the document. In the search engine, this should be the link to the website.<br/>
	 * @param	document	the List of String, each String represents a line in the document. <br/>
	 */
	public Indexer (String docName, List<String> document) {
		// Save the references into local fields
		_docName = docName;
		_indexDocument = (ArrayList<String>) document;
	}

	/**
	 * Start method <br/>
	 * This method must be explicitly triggered in order for the Indexer to start indexing documents <br/>
	 * @throws	IOException	when IO error occurs
	 */
	public void start () throws IOException {
		initializeLocalPosting ();
		fetchDictionary ();
		fetchDocumentList ();
		
		indexDocument ();
		
		updateDictionary ();
		updateDocumentList ();
		updatePosting ();
	}
	
	/**
	 * Initialise Local Posting <br/>
	 */
	private void initializeLocalPosting () {
		_localPosting = new ArrayList<ArrayList<Integer>>();
	}
	
	/**
	 * fetch the dictionary from file and store into _dictionary <br/> 
	 * @throws	IOEcxeption	when the file cannot be opened
	 */
	private synchronized void fetchDictionary () throws IOException {
		// Initialise the Stream readers
		FileInputStream fis = new FileInputStream (DICTONARY_FILENAME);
		InputStreamReader isr = new InputStreamReader (fis);
		BufferedReader br = new BufferedReader (isr);
		
		// Initialise empty dictionary
		_dictionary = new ArrayList<Vocabulary>();
		
		// Fetch data from file into local dictionary
		String nextLine = null;
		do {
			nextLine = br.readLine ();
			Vocabulary newVocabulary = parseToVocabulary (nextLine);
			if (newVocabulary != null) {
				_dictionary.add (newVocabulary);
			}
		} while (nextLine != null);
		
		// Close stream readers
		br.close ();
		isr.close ();
		fis.close ();
	}
	
	/**
	 * Parse a line from dictionary file into a Vocabulary <br/> 
	 * @param	line				from dictionary
	 * @return	<b>Vocabulary</b>	the parsed Vocabulary <br/>
	 * 			<b>null</b>			if the <b>line</b> is syntactically incorrect
	 */
	private Vocabulary parseToVocabulary (String line) {
		Vocabulary vocabulary = null;
		try {
			String[] tokens = line.split(" ");
			String word = tokens[0];
			int df = Integer.parseInt (tokens[1]);
			vocabulary = new Vocabulary (word, df);
		} catch (Exception e) {
			// Catch Parsing Error
			vocabulary = null;
		}
		return vocabulary;
	}
	
	/**
	 * fetch the list of document names from file and store into _documentList <br/>
	 * @throws	IOException	when the file cannot be opened 
	 */
	private synchronized void fetchDocumentList () throws IOException {
		// Initialise the Stream readers
		FileInputStream fis = new FileInputStream (DOCUMENT_FILENAME);
		InputStreamReader isr = new InputStreamReader (fis);
		BufferedReader br = new BufferedReader (isr);
		
		// Initialise empty dictionary
		_documentList = new ArrayList<String>();
		
		// Fetch data from file into local documentList
		String nextLine = null;
		do {
			nextLine = br.readLine ();
			if (nextLine != null) {
				_documentList.add (nextLine);
			}
		} while (nextLine != null);
		
		// Close stream readers
		br.close ();
		isr.close ();
		fis.close ();
	}
	
	/**
	 * fetch the specific line of posting from file <br/>
	 * @throws 	IOException when the file cannot be opened
	 * @return 	the array of int <br/>
	 * 			null if the the the index is bigger then the number of lines in the file <br/>
	 */
	private int[] fetchPosting (int index) throws IOException {
		String postingAsString = fetchPostingAsString (index);
		
		if (postingAsString != null) {
			String[] postingAsArray = postingAsString.split(" ");
			int[] postings = new int[postingAsArray.length];
			for (int i=0; i < postingAsArray.length; i++) {
				postings[i] = Integer.parseInt(postingAsArray[i]);
			}
			return postings;
		} else {
			return null;
		}
	}
	/**
	 * fetch the specific line of posting from file <br/>
	 * @throws 	IOException when the file cannot be opened
	 * @return 	the posting as a String of numbers. This is to save time as this method is synchronised <br/>
	 * 			null if the the index is bigger then the number of lines in the file <br/>
	 */
	private synchronized String fetchPostingAsString (int index) throws IOException {
		// Initialise the Stream readers
		FileInputStream fis = new FileInputStream (POSTING_FILENAME);
		InputStreamReader isr = new InputStreamReader (fis);
		BufferedReader br = new BufferedReader (isr);
		
		// Fetch data from file into local documentList
		String nextLine = null;
		int i = 0;
		do {
			nextLine = br.readLine ();
			if (nextLine != null) {
				_documentList.add (nextLine);
				i++;
			}
		} while (nextLine != null || i == index);
		
		// Close stream readers
		br.close ();
		isr.close ();
		fis.close ();
		
		return nextLine;
	}
	
	/**
	 * index the _indexDocument into _documentList, _dictionary and _postings
	 */
	private void indexDocument () {
		// Adding new document into _documentList
		int docID = registerNewDocument (_docName);
		
		// Initialise the tokenizer, stemmer and filter
		WhitespaceTokenizer tokenizer = WhitespaceTokenizer.INSTANCE;
		Stemmer stemmer = new Stemmer ();
		StopwordFilter filter = new StopwordFilter (stemmer);
		try {
			filter.initialize();
		} catch (IOException e) {
			System.out.println ("IO error when initialize the Filter");
			e.printStackTrace();
			return;
		}
		
		for (String line : _indexDocument) {
			String[] tokens = tokenizer.tokenize(line);
			for (String token : tokens) {
				token = stemmer.stem(token);
				if (!filter.isStopword(token)) {
					int vocabularyID = checkAndAddWordIntoDictionary (token);
					updateToLocalPosting (vocabularyID, docID);
				}
			}
		}
	}
	
	/**
	 * Register new document into the _documentList and return its new docID <br/>
	 * Return -1 if the document is already in the list <br/>
	 * @return	docID	the new docID assigned to the new document
	 * 			-1		if the document is already indexed in the _documentList
	 * 
	 */
	private int registerNewDocument (String docName) {
		int docID;
		if (! _documentList.contains(docName)) {
			// The docName is not found in the _documentList
			// add the docName into the documentList and return its new index
			_documentList.add(docName);
			docID = _documentList.indexOf(docName);
		} else {
			// The docName is already in the _documentList
			// return -1
			docID = -1;
		}
		return docID;
	}
	
	/**
	 * Check for the dictionary for the token <br/>
	 * If the token exists in the dictionary, increase its df <br/>
	 * Otherwise, create a new vocabulary and set the df to 1 <br/>
	 * Return the vocabularyID of the token in the dictionary <br/> 
	 * @param token
	 * @return vocabularyID
	 */
	private int checkAndAddWordIntoDictionary (String token) {
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
		
		// Cannot be found: add newVocaburary into the dictionary and return its index
		if (vocabularyId == -1) {
			_dictionary.add (newVocabulary);
			vocabularyId = _dictionary.indexOf(newVocabulary);
		}
		
		return vocabularyId;
	}
	
	/**
	 * TODO: write description
	 * @param vocabularyID
	 * @param docID
	 */
	private void updateToLocalPosting (int vocabularyID, int docID) {
		// TODO: implement this next time
	}
	
	private synchronized void updateDictionary () {
		
	}
	
	private synchronized void updateDocumentList () {
		
	}
	
	private synchronized void updatePosting () {
		
	}
	
	
}
