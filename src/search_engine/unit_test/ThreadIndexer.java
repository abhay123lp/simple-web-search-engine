package search_engine.unit_test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import search_engine.indexer.Indexer;

/**
 * Allow multiple thread to run the Indexer concurrently
 * 
 * @author ngtrhieu0011
 * 
 */
class ThreadIndexer extends Thread {
	private String _docName, _docContent;

	ThreadIndexer(String docName, String docContent) {
		_docName = docName;
		_docContent = docContent;
	}

	public void run() {
		try {
			Indexer indexer = new Indexer(_docName, _docContent);
			indexer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}