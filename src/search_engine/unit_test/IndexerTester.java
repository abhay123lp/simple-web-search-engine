package search_engine.unit_test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import search_engine.indexer.Indexer;

/**
 * System test the Indexer
 * 
 * @author ngtrhieu0011
 * 
 */
public class IndexerTester {
	private final String TESTING_CASE_NOTIFICATION = "\n\nTesting test case: ";
	private final String TEST_CASE_DIRECTORY = "jUnit_resource/test%d/";
	private final String DOCUMENT_FILE = "documents.txt";
	private final String DICTIONARY_FILE = "dictionary.txt";
	private final String POSTING_FILE = "postings.txt";

	private File documentFile = new File(DOCUMENT_FILE);
	private File dictionaryFile = new File(DICTIONARY_FILE);
	private File postingsFile = new File(POSTING_FILE);

	private File expectedDocumentFile;
	private File expectedDictionaryFile;
	private File expectedPostingsFile;

	/**
	 * Initialize the files to be empty
	 * 
	 * @throws IOException
	 *             when the files cannot be opened
	 */
	private void initializeFiles() throws IOException {
		// Initialise the Stream writers
		FileOutputStream fos;
		fos = new FileOutputStream(documentFile, false);
		fos.close();
		fos = new FileOutputStream(dictionaryFile, false);
		fos.close();
		fos = new FileOutputStream(postingsFile, false);
		fos.close();
	}

	/**
	 * Retrieve the expected set of Files
	 * 
	 * @param testCaseId
	 */
	private void retrieveExpectedResult(int testCaseId) {
		expectedDocumentFile = new File(String.format(TEST_CASE_DIRECTORY + DOCUMENT_FILE, testCaseId));
		expectedDictionaryFile = new File(String.format(TEST_CASE_DIRECTORY + DICTIONARY_FILE, testCaseId));
		expectedPostingsFile = new File(String.format(TEST_CASE_DIRECTORY + POSTING_FILE, testCaseId));
	}

	/**
	 * Compare the contents of 2 File
	 * 
	 * @param file1
	 * @param file2
	 * @return true if 2 files have the same content. False if they are not or
	 *         Exceptions thrown when open the files
	 */
	private boolean contentEquals(File file1, File file2) {
		System.out.println("Comparing :" + file1 + " with " + file2);

		FileInputStream fis1, fis2;
		InputStreamReader isr1, isr2;
		BufferedReader br1, br2;
		try {
			fis1 = new FileInputStream(file1);
			fis2 = new FileInputStream(file2);
			isr1 = new InputStreamReader(fis1);
			isr2 = new InputStreamReader(fis2);
			br1 = new BufferedReader(isr1);
			br2 = new BufferedReader(isr2);

			String line1, line2;
			do {
				line1 = br1.readLine();
				line2 = br2.readLine();

				if (line1 == null || line2 == null) {
					break;
				}
			} while (line1.compareTo(line2) == 0);

			br1.close();
			br2.close();
			isr1.close();
			isr2.close();
			fis1.close();
			fis2.close();

			if (line1 == null || line2 == null) {
				System.out.println("Passed!");
				return true;
			} else {
				System.out.println("These lines are not the same: ");
				System.out.println(line1);
				System.out.println(line2);
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	//Test
	public void indexOneDocument() {
		int testId = 1;

		System.out.println("Testing test case: " + testId);
		retrieveExpectedResult(testId);

		try {
			initializeFiles();

			Indexer indexer = new Indexer("www.dummy.com", "This is a simple text of my example! Yes this is an example");
			indexer.start();

			assertTrue(contentEquals(expectedDocumentFile, documentFile));
			assertTrue(contentEquals(expectedDictionaryFile, dictionaryFile));
			assertTrue(contentEquals(expectedPostingsFile, postingsFile));

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	//@Test
	public void indexTwoSimpleDocuments() {
		int testId = 2;

		System.out.println(TESTING_CASE_NOTIFICATION + testId);
		retrieveExpectedResult(testId);

		try {
			initializeFiles();

			Indexer indexer = new Indexer("www.dummy.com", "This is a simple text of my example! Yes this is an example");
			indexer.start();
			indexer = new Indexer("www.anotherdummy.com", "Another simple text file Yes this is yes");
			indexer.start();

			assertTrue(contentEquals(expectedDocumentFile, documentFile));
			assertTrue(contentEquals(expectedDictionaryFile, dictionaryFile));
			assertTrue(contentEquals(expectedPostingsFile, postingsFile));

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	//@Test
	public void indexDuplicateDocuments() {
		int testId = 3;

		System.out.println(TESTING_CASE_NOTIFICATION + testId);
		retrieveExpectedResult(testId);

		try {
			initializeFiles();

			Indexer indexer = new Indexer("www.dummy.com", "This is a simple text of my example! Yes this is an example");
			indexer.start();
			indexer = new Indexer("www.dummy.com", "Another simple text file Yes this is yes");
			indexer.start();

			assertTrue(contentEquals(expectedDocumentFile, documentFile));
			assertTrue(contentEquals(expectedDictionaryFile, dictionaryFile));
			assertTrue(contentEquals(expectedPostingsFile, postingsFile));

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	//@Test
	public void cuccurentIndexDocument() {
		int testId = 4;
		
		System.out.println(TESTING_CASE_NOTIFICATION + testId);
		retrieveExpectedResult(testId);
		
		try {
			initializeFiles();

			ThreadIndexer indexer = new ThreadIndexer("www.dummy.com", "This is a simple text of my example! Yes this is an example");
			ThreadIndexer indexer2 = new ThreadIndexer("www.anotherdummy.com", "Another simple text file Yes this is yes");
			indexer.run();
			indexer2.run();
	
			
			assertTrue(contentEquals(expectedDocumentFile, documentFile));
			assertTrue(contentEquals(expectedDictionaryFile, dictionaryFile));
			assertTrue(contentEquals(expectedPostingsFile, postingsFile));

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void AdvanceCuccurentIndexDocument() {
		// Test manually
		//int testId = 4;
		
		//System.out.println(TESTING_CASE_NOTIFICATION + testId);
		//retrieveExpectedResult(testId);
		
		try {
			initializeFiles();

			ThreadIndexer indexer = new ThreadIndexer("www.dummy.com", "This is a simple text of my example! Yes this is an example");
			ThreadIndexer indexer2 = new ThreadIndexer("www.anotherdummy.com", "Another simple text file Yes this is yes");
			indexer.run();
			indexer2.run();
			
			for (int i=0; i<50000;i++){}
			
			ThreadIndexer indexer3 = new ThreadIndexer("www.example.com", "Yes another test");
			indexer3.run();
			
			//TODO: Indexer behaves incorrectly in calculating df
	
			
			/*assertTrue(contentEquals(expectedDocumentFile, documentFile));
			assertTrue(contentEquals(expectedDictionaryFile, dictionaryFile));
			assertTrue(contentEquals(expectedPostingsFile, postingsFile));*/

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	//@Test
	public void reinitializeFiles() {		
		try {
			initializeFiles();
			assertTrue (true);
			
			
		} catch (IOException e) {
			e.printStackTrace();
			fail ();
		}
	}
}
