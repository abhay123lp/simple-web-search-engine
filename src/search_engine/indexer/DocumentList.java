package search_engine.indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

/**
 * <b>Singleton class </b><br/>
 * <br/>
 * * This class provides the implementation of DocumentList <br/>
 * A DocumentList is a list of documentName, each is then assigned a docId upon
 * adding <br/>
 * The docId is simply the index of the documentName in the list <br/>
 * <br/>
 * <b>Note: The DocumentList needs to be close () when done to save all the
 * changes into files</b><br/>
 * 
 * @author ngtrhieu0011
 * 
 */

class DocumentList {
	private final String DOCUMENT_FILENAME = "documents.txt";

	// Singleton
	private static LinkedList<String> _documentList = new LinkedList<String>();
	private static int _no_instances = 0;

	private boolean _isInitialzed = false;

	public DocumentList() throws IOException {
		start();
	}

	/**
	 * Initialise the DocumentList <br/>
	 * Fetch the document list from the disk if the document list does not exist <br/>
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 */
	public void start() throws IOException {
		synchronized (_documentList) {
			_no_instances++;
			if (_no_instances == 1) {
				fetchDocumentList();
			}
		}
		_isInitialzed = true;
	}

	/**
	 * Close the DocumentList <br/>
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 * 
	 */
	public void close() throws IOException {
		synchronized (_documentList) {
			_no_instances--;
			if (_no_instances == 0) {
				writeToFile();
			}
			_isInitialzed = false;
		}
	}

	/**
	 * Add a document into the documentList and return its docId <br/>
	 * If the document has been already added, it will not be added again <br/>
	 * 
	 * @param docName
	 * @return docId of the added document, -1 if the docName is not found, or
	 *         the DictionaryList hasn't been initialised/has been closed
	 */
	public int addDocument(String docName) {
		if (_isInitialzed) {
			synchronized (_documentList) {
				int docId = getDocId(docName);
				if (docId == -1) {
					_documentList.add(docName);
					return _documentList.size() - 1;
				}
			}
		}
		return -1;
	}

	/**
	 * Return the docId of a document based on its name
	 * 
	 * @param docName
	 * @return docId of the document, or -1 is the document is not found, or the
	 *         DictionaryList hasn't been initialised/has been closed
	 */
	public int getDocId(String docName) {
		if (_isInitialzed) {
			synchronized (_documentList) {
				return _documentList.indexOf(docName);
			}
		} else {
			return -1;
		}
	}

	/**
	 * Return the docName of a document based on its docId
	 * 
	 * @param docId
	 * @return docName of the document, null if the document is not found
	 */
	public String getDocName(int docId) {
		if (_isInitialzed) {
			try {
				synchronized (_documentList) {
					return _documentList.get(docId);
				}
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Fetch the documentList from file and store into local memory <br/>
	 * 
	 * @throws IOEcxeption
	 *             when the file cannot be opened
	 */
	private void fetchDocumentList() throws IOException {
		// Initialise the Stream readers
		FileInputStream fis = new FileInputStream(DOCUMENT_FILENAME);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);

		// Initialise empty dictionary
		_documentList = new LinkedList<String>();

		// Fetch data from file into local dictionary
		String nextLine = null;
		do {
			nextLine = br.readLine();
			if (nextLine != null) {
				_documentList.add(nextLine);
			}
		} while (nextLine != null);

		// Close stream readers
		br.close();
		isr.close();
		fis.close();
	}

	/**
	 * Update the local Document List to file
	 * 
	 * @throws IOException
	 *             when the file cannot be opened
	 */
	private void writeToFile() throws IOException {
		// Initialise the Stream writers
		FileOutputStream fos = new FileOutputStream(DOCUMENT_FILENAME, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);

		// Write each document to each line of the file.
		for (String docName : _documentList) {
			bw.write(docName + "\n");
		}

		// Close stream writers
		bw.close();
		osw.close();
		fos.close();
	}
}
