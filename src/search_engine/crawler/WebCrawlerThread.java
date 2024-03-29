package search_engine.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import search_engine.indexer.Indexer;

/**
 * This is a single web crawler thread that will crawl a page for content to
 * index and send more crawlers in if possible (if the next page is unvisited
 * and the max depth is not yet reached)
 * 
 * @author ngtrhieu0011
 */

public class WebCrawlerThread extends WebCrawler {
	private static int no_instances = 0;
	private static Object lock = new Object();

	private static Stack<WebLink> crawlingPool = new Stack<WebLink>();

	private static final int MAX_INSTANCES = 5;

	private final long CRAWLING_IO_ERROR = -1;
	private final long CRAWLING_TIMEOUT_ERROR = -2;
	private final long SOCKET_TIMEOUT_ERROR = -3;
	private final long HOST_ERROR = -4;
	private final long CRAWLING_TIMEOUT = 10000; // in milliseconds
	private final int SO_TIMEOUT = 5000; // in milliseconds

	// Port number for web server
	private final int DEFAULT_PORT_NUMBER_USED = 80;

	// Delay between 2 consecutive crawling, in milliseconds
	private final int CRAWLING_RATE = 500;
	// Used to extract redirect links
	private final String LOCATION_FIELD = "LOCATION:";

	// Used to store the crawled documents
	private final String CRAWLED_FOLDER = "CrawledDocs/doc%d.txt";
	private static int docId = 0;

	private String startingURL;
	private Document pageContent;
	private ArrayList<String> headerContent;
	private int depth, maxDepth, portNumber;
	private Socket httpSocket;

	/**
	 * Partial constructor: assume depth is 0, portNumber is default
	 * 
	 * @param startingURL
	 * @param maxDepth
	 */
	public WebCrawlerThread(String startingURL, int maxDepth) {
		this.startingURL = startingURL;
		this.depth = 0;
		this.maxDepth = maxDepth;
		this.portNumber = DEFAULT_PORT_NUMBER_USED;
	}

	/**
	 * Complete constructor
	 * 
	 * @param startingURL
	 * @param depth
	 * @param maxDepth
	 * @param portNumber
	 */
	public WebCrawlerThread(String startingURL, int depth, int maxDepth, int portNumber) {

		this.startingURL = startingURL;
		this.depth = depth;
		this.maxDepth = maxDepth;
		this.portNumber = portNumber;
	}

	/**
	 * Called when the thread is started Check the depth: if reach maximum depth
	 * then quit Else try load the starting url and report
	 */
	public void run() {
		synchronized (lock) {
			no_instances++;
			System.out.println("++ Current thread count: " + no_instances + ". Pool size: " + crawlingPool.size());
		}
		
		if (depth < maxDepth) {
			System.out.println("Attempt to crawl " + startingURL);
			// not reaching the maxDepth
			// trying to load the page, time the response
			long responseTime = loadPageAndGetResponseTime(startingURL);

			// if responseTime is not an error code
			if (responseTime >= 0) {
				// No error: report crawl result
				WebCrawler.ReportCrawlResult(startingURL, responseTime);

				// Further process only when the page is English
				if (isContentLanguage(headerContent, "en")) {
					// Check for redirection and check crawlers in the
					// redirected page
					checkRedirection(headerContent);

					// Retrieve links in the page content
					retrieveLinks(pageContent);
				} else {
					System.out.println("Page is not English");
				}
			} else if (responseTime == CRAWLING_IO_ERROR) {
				// IO Error: Report back
			} else if (responseTime == CRAWLING_TIMEOUT_ERROR) {
				// Timeout Error: Report back
			} else if (responseTime == SOCKET_TIMEOUT_ERROR) {
				// Timeout Error: Report back
			} else if (responseTime == HOST_ERROR) {
				// Timeout Error: Report back
			}
			
			sendCrawlers();
		}

		synchronized (lock) {
			no_instances--;
			System.out.println("-- Current thread count: " + no_instances + ". Pool size: " + crawlingPool.size());
		}
	}

	/**
	 * Start sending crawlers into links that in the pool <br/>
	 * The sending rate is controlled by CRAWLING_RATE, which defines the time
	 * between 2 consecutive sends <br/>
	 * The number of active threads is limited by MAX_INSTANCES, so if the
	 * maximum thread count is reach, no thread spawned. Otherwise it would
	 * spawn 5 times<br/>
	 */
	private void sendCrawlers() {
		boolean isCongested;
		synchronized (lock) {
			isCongested = no_instances >= MAX_INSTANCES;
		}

		if (isCongested) {
			// congested: spawn no thread
		} else {
			// no congested, spawn 5 threads
			synchronized (crawlingPool) {
				try {
					for (int i = 0; i < 5; i++) {
						WebLink webLink = crawlingPool.pop();
						sendCrawlerIntoLink(webLink.getLink(), webLink.getDepth());
					}
				} catch (Exception e) {
					// Empty stack. Do nothing
				}
			}
		}
	}

	/**
	 * Try to load the page and store all the page content inside the
	 * pageContent field At the same time measure the time taken to load the
	 * full page
	 * 
	 * @param link
	 *            : the url link of the page
	 * @return the time taken to load the full page. INVALID_NUMBER is returned
	 *         if an error occured
	 */
	private long loadPageAndGetResponseTime(String link) {
		try {
			String hostName = getHostName(startingURL);
			String pathName = getPathName(startingURL);

			try {
				httpSocket = new Socket(hostName, portNumber);
			} catch (Exception e) {
				return HOST_ERROR;
			}
			httpSocket.setSoTimeout(SO_TIMEOUT);
			PrintWriter writer = new PrintWriter(httpSocket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpSocket.getInputStream()));

			// Send HTTP Get Request
			writer.println("GET " + pathName + " HTTP/1.1\r");
			writer.println("Host: " + hostName + "\r");
			writer.println("Connection: close\r");
			writer.println("Accept-Language: en\r");
			writer.println();

			long startingTime = System.currentTimeMillis(); // Start timing
			long responseTime = -1;

			String newLine;
			String htmlString = "";
			headerContent = new ArrayList<String>();

			boolean isReadingResponseReader = true;

			do {
				newLine = reader.readLine(); // read each line in

				if (responseTime == -1) {
					// Response time taken when the first few bytes are
					// available in the reader stream
					responseTime = System.currentTimeMillis();
				}

				if ((newLine != null) && (newLine.compareTo("") == 0)) {
					isReadingResponseReader = false; // now reading to the
														// package content
				}

				if (newLine == null) {
					break;
				}

				if (!isReadingResponseReader) {
					htmlString += newLine; // concat to the htmlString
				} else {
					headerContent.add(newLine); // concat to the headerString
				}

				// calculate the elapsed time
				// if too long then just break (Timeout)
				long elapsedTime = System.currentTimeMillis() - startingTime;
				if (elapsedTime > CRAWLING_TIMEOUT) {
					reader.close();
					return CRAWLING_TIMEOUT_ERROR;
				}

				// consider fixing problem when server "forget" to close the
				// input stream
			} while (newLine != null && !newLine.toLowerCase().endsWith("</html>"));

			reader.close();

			// parse the htmlString into HTML Document
			pageContent = (Document) Jsoup.parse(htmlString);

			return responseTime - startingTime;
		} catch (SocketTimeoutException e1) {
			// Socket timeout: readLine() has been blocked for SO_TIMEOUT
			// milliseconds
			return SOCKET_TIMEOUT_ERROR;
		} catch (IOException e) {
			// Error when set up input stream and reader
			return CRAWLING_IO_ERROR;
		}
	}

	/**
	 * Scan through the HTML Document, retrieve all the href field in a tag Then
	 * send crawlers into that link.
	 * 
	 * @param pageContent
	 *            : the HTML Document of the page
	 */
	private void retrieveLinks(Document pageContent) {
		Elements links = pageContent.getElementsByTag("a"); // Get list of a
															// tags

		// Scan through the list of a tag
		for (int i = 0; i < links.size(); i++) {
			Element link = links.get(i);
			String linkHref = link.attr("href"); // retrieve the href field
			WebLink newLink = new WebLink(linkHref, depth + 1);
			if (newLink.getDepth() < maxDepth) {
				synchronized (crawlingPool) {
					crawlingPool.push(newLink);
				}
			}
		}

		String text;
		try {
			text = pageContent.body().text();
		} catch (Exception e) {
			return;
		}

		// TODO: Choose either to do one
		indexDocument(startingURL, text);
		// writeDocument(startingURL, text);
	}

	/**
	 * Index the document <br/>
	 * 
	 * @param docName
	 *            the name of the document <br/>
	 * @param docContent
	 *            the content of the document <br/>
	 */
	private void indexDocument(String docName, String docContent) {
		Indexer indexer = new Indexer(docName, docContent);
		try {
			indexer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a unique docId <br/>
	 * 
	 * @return a unique docId number
	 */
	private static synchronized int getDocId() {
		return docId++;
	}

	/**
	 * Write the document out to the file <br/>
	 * This can be used to save the document for crawling later, or for
	 * inspection purposes <br/>
	 * 
	 * @param docName
	 *            the name of the document <br/>
	 * @param docContent
	 *            the content of the document <br/>
	 */
	private void writeDocument(String docName, String docContent) {
		try {
			int docId = getDocId();

			// Create new one if it doesn't exist
			File outputFile = new File(String.format(CRAWLED_FOLDER, docId));
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}

			// Initialise the Stream writers
			FileOutputStream fos = new FileOutputStream(outputFile, false);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			bw.write(docName + "\n");
			bw.write(docContent);

			// Close stream writers
			bw.close();
			osw.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check the header for Content language of a specific language <br/>
	 * Only return false when the page header explicitly states the language are
	 * not supported <br/>
	 * 
	 * @param the
	 *            headerContent
	 * @param language
	 *            the language code (i.e: "en" for English)
	 * @return false if the Content-Language in the header does not the language
	 *         code, otherwise true
	 */
	private boolean isContentLanguage(List<String> headerContent, String language) {
		try {
			for (String line : headerContent) {
				if (line.contains("Content-Language") && !line.contains(language)) {
					return false;
				}
			}
		} catch (Exception e) {
			System.out.println("Cannot read content header for page " + startingURL);
		}

		return true;
	}

	/**
	 * Check the header for redirection If recieve a 3xx, get the link from
	 * LOCATION field and follow that link
	 * 
	 * @param headerContent
	 */
	private void checkRedirection(List<String> headerContent) {
		try {
			String firstHeaderLine = headerContent.get(0);
			String[] firstHeaderLineSplit = firstHeaderLine.split(" ");
			int httpCode = Integer.parseInt(firstHeaderLineSplit[1]);

			if (299 < httpCode && httpCode < 400) { // HTTP Code = 3xx
				for (String headerLine : headerContent) {
					String[] headerLineSplit = headerLine.split(" ");
					if (headerLineSplit[0].equalsIgnoreCase(LOCATION_FIELD)) {
						String redirectedURL = headerLineSplit[1];
						WebLink newLink = new WebLink(redirectedURL, depth + 1);
						if (newLink.getDepth() < maxDepth) {
							synchronized (crawlingPool) {
								crawlingPool.push(newLink);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Cannot read content header for page " + startingURL);
		}
	}

	/**
	 * Send crawler to the domain of the link provided Don't send if the domain
	 * have already visited or the max depth has been reached
	 * 
	 * @param link
	 */
	private void sendCrawlerIntoLink(String link, int depth) {
		String currentHostName = getHostName(startingURL);
		String linkHostName = getHostName(link);
		if (linkHostName != null) {
			link = reparseLink(link);
		} else {
			link = reparseLink(currentHostName + link);
		}

		if (WebCrawler.CheckAndAddLink(link)) { // check whether the link has
												// been crawled
			if (depth < maxDepth - 1) {
				// instantiate a new crawler, wait a while then send it into the
				// retrieved link
				// false = not Daemon: the application will wait until the
				// children crawlers finish before terminating
				Timer crawlingTimer = new Timer("crawlingTimer" + depth, false);
				crawlingTimer.schedule(new WebCrawlerThread(link, depth, maxDepth, DEFAULT_PORT_NUMBER_USED), CRAWLING_RATE);
			}
		}
	}

	/**
	 * Get the Host Name from an URL
	 * 
	 * @param link
	 * @return the host name of the URL
	 */
	private String getHostName(String link) {
		// TODO: crawl to null sometimes
		if (link == null) {
			return "";
		}
		
		if (!link.startsWith("http") && !link.startsWith("https")) {
			if (link.startsWith("//")) {
				link = "http:" + link;
			} else {
				link = "http://" + link;
			}
		}

		URI uri;
		try {
			uri = new URI(link);
		} catch (URISyntaxException e) {
			return "";
		}

		return uri.getHost();
	}

	/**
	 * Get the Host Name from an URL
	 * 
	 * @param link
	 * @return the host name of the URL
	 */
	private String getPathName(String link) {
		if (link == null) {
			return "";
		}
		if (!link.startsWith("http") && !link.startsWith("https")) {
			if (link.startsWith("//")) {
				link = "http:" + link;
			} else {
				link = "http://" + link;
			}
		}

		URI uri;
		try {
			uri = new URI(link);
		} catch (URISyntaxException e) {
			return "";
		}

		String pathName = uri.getRawPath();
		if (pathName.compareTo("") == 0) {
			pathName = "/";
		}

		return pathName;
	}

	/**
	 * Re-parse Method This method will extract the domain part of the link and
	 * remove other.
	 * 
	 * @param link
	 *            : the raw link
	 * @return the formated link
	 */
	private String reparseLink(String link) {
		URI uri;
		try {
			uri = new URI(link);
		} catch (URISyntaxException e) {
			return "";
		}

		if (uri.getHost() == null) {
			return uri.getRawPath();
		} else {
			return uri.getHost() + uri.getRawPath();
		}
	}
}
