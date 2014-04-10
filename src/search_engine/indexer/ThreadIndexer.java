package search_engine.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TimerTask;

import search_engine.indexer.Indexer;

/**
 * Allow multiple thread to run the Indexer concurrently
 * 
 * @author ngtrhieu0011
 * 
 */
class ThreadIndexer extends TimerTask {
	// Used to store the crawled documents
	private static final String CRAWLED_FOLDER = "CrawledDocs/doc%d.txt";
	private static int docId = 0; 
	
	private static int no_instances = 0;
	private static Object lock = new Object();
	
	private String _docName, _docContent;

	ThreadIndexer(String docName, String docContent) {
		_docName = docName;
		_docContent = docContent;
	}

	public void run() {
		try {
			synchronized (lock) {
				no_instances ++;
			}
			Indexer indexer = new Indexer(_docName, _docContent);
			indexer.start();
			synchronized (lock) {
				no_instances --;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static synchronized int getDocId () {
		return docId++;
	}
	
	public static void main (String[] args) {
		for (int i=0; i<1600; i++) {
			try {
				File file = new File (String.format (CRAWLED_FOLDER, i));
				if (file.exists()) {
					// Initialise the Stream readers
					FileInputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis);
					BufferedReader br = new BufferedReader(isr);
					
					String docName = br.readLine();
					String docContent = br.readLine();
					
					System.out.println ("Indexing document id: " + i);
					Indexer indexer = new Indexer (docName, docContent);
					indexer.start();
		
					// Close stream readers
					br.close();
					isr.close();
					fis.close();
				}
			} catch (Exception e) {
			}
		}
	}
}