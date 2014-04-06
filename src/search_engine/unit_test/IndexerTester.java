package search_engine.unit_test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;

import org.junit.Test;

import search_engine.indexer.Indexer;

/**
 * System test the Indexer
 * 
 * @author ngtrhieu0011
 * 
 */
public class IndexerTester {
	public static int no_threads = 0;
	public static Object lock = new Object();

	private final String TESTING_CASE_NOTIFICATION = "\n\nTesting test case: ";
	private final String TEST_RERUN = "\nTest re-run - ";
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

			if (line1 == null && line2 == null) {
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

	/**
	 * Loosely compare 2 files <br/>
	 * The files are loosely equal when they are <br/>
	 * 1. Having the same number of lines and <br/>
	 * 2. The lines are the same but not in order <br/>
	 * That means 2 files that are equal will be loosely equal, but not vise
	 * versa <br/>
	 * However, after reorder the lines from one file in two loosely equal
	 * files, we can get 2 files to be equal <br/>
	 * 
	 * @param file1
	 * @param file2
	 * 
	 * @return true if the files are loosely equal, false when they are not or
	 *         exceptions thrown when opening the files
	 */
	private boolean contentLooselyEquals(File file1, File file2) {
		System.out.println("Loosy comparing :" + file1 + " with " + file2);

		try {
			ArrayList<String> content1 = readContentFromFile(file1);
			ArrayList<String> content2 = readContentFromFile(file2);

			if (content1.size() != content2.size()) {
				System.out.println("Numbers of lines in each file are different");
				return false;
			}

			Collections.sort(content1);
			Collections.sort(content2);

			for (int i = 0; i < content1.size(); i++) {
				String line1 = content1.get(i);
				String line2 = content2.get(i);

				if (line1.compareTo(line2) != 0) {
					System.out.println("One of these lines does not exist in the other file:");
					System.out.println(line1);
					System.out.println(line2);
					return false;
				}
			}

			System.out.println("Pass");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Read the content from file into ArrayList <br/>
	 * Each line is corresponding to an String in ArrayList <br/>
	 * 
	 * @param file
	 *            to be read
	 * @return an ArrayList containing every lines in the file
	 * @throws IOException
	 *             when cannot open file
	 */
	private ArrayList<String> readContentFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);

		ArrayList<String> fileContent = new ArrayList<String>();
		String line;
		do {
			line = br.readLine();
			if (line == null) {
				break;
			}
			fileContent.add(line);

		} while (line != null);

		return fileContent;
	}

	// @Test
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

			initializeFiles();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// @Test
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

			initializeFiles();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// @Test
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

			initializeFiles();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// @Test
	public void concurentIndexDocument() {
		int testId = 4;

		System.out.println(TESTING_CASE_NOTIFICATION + testId);
		retrieveExpectedResult(testId);

		try {
			initializeFiles();
			no_threads = 2;

			ThreadIndexer indexer = new ThreadIndexer("www.dummy.com", "This is a simple text of my example! Yes this is an example");
			ThreadIndexer indexer2 = new ThreadIndexer("www.anotherdummy.com", "Another simple text file Yes this is yes");
			indexer.run();
			indexer2.run();

			while (no_threads != 0) {
				Thread.yield();
			}

			assertTrue(contentEquals(expectedDocumentFile, documentFile));
			assertTrue(contentEquals(expectedDictionaryFile, dictionaryFile));
			assertTrue(contentEquals(expectedPostingsFile, postingsFile));

			initializeFiles();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void delayConccurentIndexDocument() {
		int testId = 5;
		int numOfRerun = 100; // Rerun multiple times

		System.out.println(TESTING_CASE_NOTIFICATION + testId);
		retrieveExpectedResult(testId);

		try {
			// TODO: number of lines in documentList is not correct
			// This is due to 2 threads fetching the document list concurrently
			for (int i=0; i<numOfRerun; i++) {
				System.out.println (TEST_RERUN + i);
				initializeFiles();
				no_threads = 3;
	
				Timer indexTimer = new Timer("index1", false);
				Timer index2Timer = new Timer("index2", false);
				Timer index3Timer = new Timer("index3", false);
				indexTimer.schedule(new ThreadIndexer("www.dummy.com", "This is a simple text of my example! Yes this is an example"), 10);
				index2Timer.schedule(new ThreadIndexer("www.anotherdummy.com", "Another simple text file Yes this is yes"), 10);
				index3Timer.schedule(new ThreadIndexer("www.example.com", "Yes another text"), 10);
	
				while (no_threads != 0) {
					Thread.yield();
				}
	
				assertTrue(contentLooselyEquals(expectedDocumentFile, documentFile));
				assertTrue(contentLooselyEquals(expectedDictionaryFile, dictionaryFile));
			}

			// TODO: uncomment this
			// initializeFiles();

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	// @Test
	public void heavyConccurentIndexDocument() {
		int testId = 6;

		System.out.println(TESTING_CASE_NOTIFICATION + testId);
		retrieveExpectedResult(testId);

		try {
			initializeFiles();
			no_threads = 15;

			Timer indexTimer = new Timer("index1", false);
			Timer indexTimer2 = new Timer("index2", false);
			Timer indexTimer3 = new Timer("index3", false);
			Timer indexTimer4 = new Timer("index4", false);
			Timer indexTimer5 = new Timer("index5", false);
			Timer indexTimer6 = new Timer("index6", false);
			Timer indexTimer7 = new Timer("index7", false);
			Timer indexTimer8 = new Timer("index8", false);
			Timer indexTimer9 = new Timer("index9", false);
			Timer indexTimer10 = new Timer("index10", false);
			Timer indexTimer11 = new Timer("index11", false);
			Timer indexTimer12 = new Timer("index12", false);
			Timer indexTimer13 = new Timer("index13", false);
			Timer indexTimer14 = new Timer("index14", false);
			Timer indexTimer15 = new Timer("index15", false);
			indexTimer.schedule(new ThreadIndexer("www.example.com", "This is a simple text of my example! Yes this is an example"), 10);
			indexTimer2.schedule(new ThreadIndexer("www.example.com2", "Another simple text file Yes this is yes"), 10);
			indexTimer3.schedule(new ThreadIndexer("www.example.com3", "Yes another text"), 10);
			indexTimer4.schedule(new ThreadIndexer("www.example.com4", "Yes another text"), 10);
			indexTimer5.schedule(new ThreadIndexer("www.example.com5", "Yes another text"), 10);
			indexTimer6.schedule(new ThreadIndexer("www.example.com6", "Yes another text"), 10);
			indexTimer7.schedule(new ThreadIndexer("www.example.com7", "Yes another text"), 10);
			indexTimer8.schedule(new ThreadIndexer("www.example.com8", "Yes another text"), 10);
			indexTimer9.schedule(new ThreadIndexer("www.example.com9", "Yes another text"), 10);
			indexTimer10.schedule(new ThreadIndexer("www.example.com10", "Yes another text"), 10);
			indexTimer11.schedule(new ThreadIndexer("www.example.com11", "Yes another text"), 10);
			indexTimer12.schedule(new ThreadIndexer("www.example.com12", "Yes another text"), 10);
			indexTimer13.schedule(new ThreadIndexer("www.example.com13", "Yes another text"), 10);
			indexTimer14.schedule(new ThreadIndexer("www.example.com14", "Yes another text"), 10);
			indexTimer15.schedule(new ThreadIndexer("www.example.com15", "Yes another text"), 10);

			while (no_threads != 0) {
				Thread.yield();
			}

			assertTrue(contentLooselyEquals(expectedDocumentFile, documentFile));
			assertTrue(contentLooselyEquals(expectedDictionaryFile, dictionaryFile));

			initializeFiles();

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
