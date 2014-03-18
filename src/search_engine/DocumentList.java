package search_engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

/**
 * This class provides the implementation of DocumentList <br/>
 * A DocumentList is a list of documentName, each is then assigned a docId upon
 * adding <br/>
 * The docId is simply the index of the documentName in the list <br/>
 * 
 * @author ngtrhieu0011
 * 
 */

class DocumentList {
	private final String DOCUMENT_FILENAME = "documents.txt";
	private LinkedList<String> _documentList;

	/**
	 * Fetch the documentList from file and store into local memory <br/>
	 * 
	 * @throws IOEcxeption
	 *             when the file cannot be opened
	 */
	public synchronized void fetchDictionary() throws IOException {
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
	public synchronized void writeToFile() throws IOException {
		// Initialise the Stream writers
		FileOutputStream fos = new FileOutputStream(DOCUMENT_FILENAME, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);

		// Write each document to each line of the file.
		for (String docName : _documentList) {
			bw.write(docName + "/n");
		}

		// Close stream writers
		bw.close();
		osw.close();
		fos.close();
	}

	/**
	 * Add a document into the documentList and return its docId <br/>
	 * If the document has been already added, it will not be added again <br/>
	 * 
	 * @param docName
	 * @return docId of the added document
	 */
	public int addDocument(String docName) {
		int docId = getDocId(docName);
		if (docId != -1) {
			_documentList.add(docName);
			return _documentList.size() - 1;
		} else {
			return docId;
		}
	}

	/**
	 * Return the docId of a document based on its name
	 * 
	 * @param docName
	 * @return docId of the document, or -1 is the document is not found
	 */
	public int getDocId(String docName) {
		return _documentList.indexOf(docName);
	}

	/**
	 * Return the docName of a document based on its docId
	 * 
	 * @param docId
	 * @return docName of the document, null if the document is not found
	 */
	public String getDocName(int docId) {
		try {
			return _documentList.get(docId);
		} catch (Exception e) {
			return null;
		}
	}
}
