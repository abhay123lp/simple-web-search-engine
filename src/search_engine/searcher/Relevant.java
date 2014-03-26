package search_engine.searcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import opennlp.tools.tokenize.WhitespaceTokenizer;

import search_engine.common.IStemmer;
import search_engine.common.Stemmer;
import search_engine.common.StopwordFilter;

/**
 * Construction to create the Relevant class, nothing need to be done here
 * 
 * @author Ludvig Kratz
 * 
 */
class Relevant {
	private Set<Object> scores;
	private ArrayList<String> result;

	/**
	 * Construction to create the Relevant class, nothing need to be done here
	 */
	public Relevant() {
		

	}

	/**
	 * Function to determine the K most relevant documents based on input query <br/>
	 * PRE CONDITION: Vector consisting of query token that is tokenized,
	 * stemmed and stop-wored <br/>
	 * POST CONDITION: Returns string vector with the K most relevant documents <br/>
	 * 
	 * @param querry
	 * @return
	 * @throws IOException 
	 */
	public ArrayList<String> getRelevant(String queryFromUser) throws IOException {
		int K = 10; //Defined by us
	    
	    String tempQuery,tempDoc,tempPost;
	    int docId = 0, vocabularyId;
	    int df,tf = 0;
	    ArrayList<String> document = new ArrayList<String> ();
	    ArrayList<Object> queryId = new ArrayList<Object> ();
	    ArrayList<Object> queryVector, docVector;
	    double vocaScore,frequencyFactor = 0,uniquenessFactor = 0, cosAngleScore;
	    ArrayList<Object> relevantObject;
	    ArrayList<String> relevantDoc;
	    String[] query;
	    
	    //TODO: Tokenize, stem, stopword filter to get the query from queryFromUser
	    WhitespaceTokenizer tokenizer = WhitespaceTokenizer.INSTANCE;
	    IStemmer stemmer = new Stemmer();
	    StopwordFilter filter = new StopwordFilter(stemmer, "stopwordlist.txt");
	    filter.initialize();
	    
	    query = tokenizer.tokenize(queryFromUser);
	    if (filter.isStopword(word)) {
	    	// do sth
	    }
	    
	    
	    /*Get df and vocabularyID for each query token using DICTIONARYLIST text file
	    if word not in the dictionary, ignore it. */
	    for (int i = 0; i < query.length; i++) {
	        vocabularyId = 0;
	        
	        // Initialise the Stream readers
			FileInputStream fis = new FileInputStream("dictionaryList.txt");
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			
			String line;
			do {
				line = br.readLine();
				String[] tokens = line.split(" ");
				
				if (line != null) {
					tempQuery = tokens[0].toLowerCase();
					df = Integer.parseInt(tokens[1]);
					
					query[i] = query[i].toLowerCase();
					
					if (tempQuery.compareTo(query[i]) == 0) {
						Object q = new Object (tempQuery, vocabularyId, df);
						queryId.add(q);
						break;
					}
					
					vocabularyId ++;
				}				
			} while (line != null);
			
			br.close();
		    isr.close();
		    fis.close();
	    }
	    
	    // Read the Document List file and store into document
		FileInputStream fis = new FileInputStream("documentList.txt");
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		
		String line;
		do {
			line = br.readLine();
			if (line != null) {
				document.add(line);
			}
		} while (line != null);
		
		br.close();
		isr.close();
		fis.close();
		
		// For each document, calculate the relevant score
		fis = new FileInputStream("postingFile.txt");
		isr = new InputStreamReader (fis);
		br = new BufferedReader(isr);
		int lineNumber = -1;
		
		int[][] scores = new int[document.size()][query.length];
		int[] queryScores = new int [query.length];
		
		// TODO: sort queryId
		
		// record down the tf for all the document
		for (int i=0; i<queryId.size(); i++) {
			Object token = queryId.get(i);
			
			do {
				line = br.readLine();
				lineNumber ++;
				
				if (line !=null){
					if (token.getId() == lineNumber) {
						break;
					}
				}
			} while (line!=null);
			
			assert (line != null);
			
			// TODO: Parse the String into proper postingList
			ArrayList<Tuple> postingList = new ArrayList<Tuple>();
			
			for (int _docId=0; _docId<document.size(); _docId++) {
				// TODO: getTuple return the tuple with the docId
				score[i, _docId] = postingList.getTuple (docId).tf;
			}
		}		
		br.close();
		isr.close();
		fis.close();
		
		// record the tf for query
		// TODO: count the number of appearance of each word in the query
		// i.e: "how are how" will be <2,1> for <how,be>
		int[] queryVector = int queryVector(query.length);
		
		ArrayList<Tuple> finalScore = new ArrayList<Tuple>();
		for (int i=0; i<document.size(); i++) {
			int[] documentVector = scores[i];
			finalScore.add(new Tuple (calculateAngleVector (documentVector, queryVector), i));
		}
		
		//TODO: sort final score, convert docId into docName, return the top K results
		// Temp return
	    return null;

	}

	public double getAngle(ArrayList<Object> q, ArrayList<Object> d) {
		// Temp return
		return 0;
	}

}
