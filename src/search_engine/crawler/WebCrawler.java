package search_engine.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * This is the Web Crawler used mostly to test the responsiveness of neighbouring domains
 * It will take the starting set of domains, then crawl through neighbouring domains using
 * links provided in the responses.
 * For each domain, the response time is captured and return back.
 * Crawlers will not visit duplicated domains.
 * 
 * Arguments for the program is stated in the description of the main method
 * 
 * @author Nguyen Trung Hieu (A0088498)
 * CS3101 Project - Assignment 1
 */

public class WebCrawler extends TimerTask {
	private static String reportFileName;
	private static ArrayList<String> crawledLinks;
	
	/**
	 * Check the links whether it was visited by other domains
	 * If not visited then return true and add the links into visited domains
	 * If already visited then return false
	 * @param link
	 * @return true if not visited, false otherwise
	 */
	protected static synchronized boolean CheckAndAddLink (String link) {
		if (link == "") {
			return false;
		}
		if (WebCrawler.crawledLinks.contains(link)) {
			return false;
		} else {
			WebCrawler.crawledLinks.add(link);
			return true;
		}
	}
	
	
	/**
	 * Write the report to the output file
	 * @param link: the crawled domain
	 * @param responseTime: response time of the crawled domain
	 */
	protected static synchronized void ReportCrawlResult (String link, long responseTime) {
		try {
			FileWriter fstream = new FileWriter (reportFileName, true); // true = open to append
			BufferedWriter bufferedWriter = new BufferedWriter (fstream);
			bufferedWriter.write("Crawl to " + link + "\nResponse Time: " + responseTime + "\n"); // writeln to the file
			bufferedWriter.close();
			fstream.close ();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a new blank file
	 * Basically just open the file then close immediate
	 * @param fileName
	 * @return true if success, false if error
	 */
	protected static boolean CreateNewBlankFile (String fileName) {
		try {
			FileWriter fstream = new FileWriter (fileName);
			fstream.close ();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	/**
	 * Entry point of the program
	 * @param args:
	 * 			First argument will be the name of the output file
	 * 			Second argument will be the maximum depth the crawlers can reach
	 * 			Others will be set of valid starting domains
	 * E.g: output.txt 4 www.comp.nus.edu.sg www.google.com www.facebook.com
	 */
	public static void main (String[] args) {
		crawledLinks = new ArrayList<String>(); // The list of crawled domain
		
		reportFileName = args[0]; // the output file name
		boolean isCreateNewFileSuccess = CreateNewBlankFile (reportFileName);
		if (!isCreateNewFileSuccess) {
			System.out.println ("Cannot create report file");
			return;
		}
		
		// the max depth crawlers will dig to
		int maxDepth = Integer.parseInt(args[1]); 
		
		// send crawlers to respective domain
		for (int i=2; i<args.length; i++) {
			if (WebCrawler.CheckAndAddLink(args[i])) {
				WebCrawlerThread thread = new WebCrawlerThread(args[i], maxDepth);
				thread.run ();
			}
		}		
	}

	@Override
	public void run() {		
	}
}
