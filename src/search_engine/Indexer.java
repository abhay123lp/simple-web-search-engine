package search_engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;

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
	private final String DICTONARY_FILENAME = "dictionary.txt";
	private final String DOCUMENT_FILENAME = "documents.txt";
	private final String POSTING_FILENAME = "postings.txt";
	
	private String _docName;
	private ArrayList<String> _indexDocument;
	
	private ArrayList<String> _documentList;
	private ArrayList<String> _dictionary;
	
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
		fetchDictionary ();
		fetchDocumentList ();
		
		indexDocument ();
		
		updateDictionary ();
		updateDocumentList ();
		updatePosting ();
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
		_dictionary = new ArrayList<String>();
		
		// Fetch data from file into local dictionary
		String nextLine = null;
		do {
			nextLine = br.readLine ();
			if (nextLine != null) {
				_dictionary.add (nextLine);
			}
		} while (nextLine != null);
		
		// Close stream readers
		br.close ();
		isr.close ();
		fis.close ();
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
		
	}
	
	
	
	private synchronized void updateDictionary () {
		
	}
	
	private synchronized void updateDocumentList () {
		
	}
	
	private synchronized void updatePosting () {
		
	}
	
	
}
