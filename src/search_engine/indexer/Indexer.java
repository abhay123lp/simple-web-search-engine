package search_engine.indexer;

import java.io.IOException;

import opennlp.tools.tokenize.WhitespaceTokenizer;
import search_engine.common.Stemmer;
import search_engine.common.StopwordFilter;

/**
 * This class provide index service to index a single document to the database
 * of the search engine. <br/>
 * Indexer accepts documents as a single file or a list of lines from its
 * Instantiator. <br/>
 * <br/>
 * <b>Note:</b> <br/>
 * 1. This class need to explicitly trigger by called start() method after
 * instantiated to begin indexing <br/>
 * <br/>
 * <u>For example: </u><br/>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	Indexer indexer = new Indexer(docName, document);
 * 	indexer.start();
 * }
 * </pre>
 * 
 * <br/>
 * 
 * 2. As this class may take a very long time to finish indexing one document,
 * and 2 Indexers cannot run concurrently, Indexer is recommended to run in a
 * separate Thread to avoid blocking the Instantiator <br/>
 * 
 * @author ngtrhieu0011
 */
public class Indexer {
	private String _docName;

	/**
	 * This private variable is use to store all the tokenized, stemmed words in
	 * the original document sent to the Indexer <br/>
	 */
	private String _indexDocument;

	/**
	 * The document list stores all the indexed document name (in this case, url
	 * of the document) <br/>
	 * Each document name is stored on a separated String <br/>
	 * The docID of each document is the index of the String in the list <br/>
	 */
	private DocumentList _documentList;

	private Dictionary _dictionary;
	private PostingList _localPosting;

	/**
	 * Recommended Constructor <br/>
	 * This constructor is safer to use, thus recommended. <br/>
	 * Pass the document to the Indexer as a List of String. <br/>
	 * 
	 * @param docName
	 *            specifies the name of the document. In the search engine, this
	 *            should be the link to the website.<br/>
	 * @param document
	 *            the List of String, each String represents a line in the
	 *            document. <br/>
	 */
	public Indexer(String docName, String document) {
		// Save the references into local fields
		_docName = docName;
		_indexDocument = document;
	}

	/**
	 * Start method <br/>
	 * This method must be explicitly triggered in order for the Indexer to
	 * start indexing documents <br/>
	 * 
	 * @throws IOException
	 *             when cannot open the database
	 */
	public void start() throws IOException {
		// Initialise localPosting, dictionary and documentList
		_localPosting = new PostingList();
		_dictionary = new Dictionary();
		_documentList = new DocumentList();

		// Index document
		indexDocument();

		// Update the local knowledge to file
		_documentList.close();
		_dictionary.close();
		_localPosting.writeToFile();
	}

	/**
	 * index the _indexDocument into _documentList, _dictionary and _postings
	 */
	private void indexDocument() {
		// Adding new document into _documentList and get its docId
		int docId = _documentList.addDocument(_docName);

		// Proceed if docId != -1, i.e: the document has not been indexed before
		// Otherwise don't index
		if (docId != -1) {
			// Initialise the tokenizer, stemmer and filter
			WhitespaceTokenizer tokenizer = WhitespaceTokenizer.INSTANCE;
			Stemmer stemmer = new Stemmer();
			StopwordFilter filter = new StopwordFilter(stemmer);

			// Try initialise the Filter
			try {
				filter.initialize();
			} catch (IOException e) {
				System.out.println("IO error when initialize the Filter");
				e.printStackTrace();
				return;
			}

			// Tokenize
			String[] tokens = tokenizer.tokenize(_indexDocument);
			for (String token : tokens) {
				// Lowercase and remove all non-alphabet character
				token = token.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
				// Stem
				token = stemmer.stem(token);
				// Filter stop-word
				if (!filter.isStopword(token)) {
					int vocabularyId = _dictionary.checkAndAddWord(token);
					Posting posting = _localPosting
							.addAndGetPosting(vocabularyId);
					posting.addDocId(docId);
				}
			}
		}
	}
}
