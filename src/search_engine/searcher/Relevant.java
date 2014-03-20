package search_engine.searcher;

import java.util.ArrayList;
import java.util.Set;

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
	 */
	public ArrayList<String> getRelevant(ArrayList<String> query) {
		int K = 10; //Defined by us
	    
	    String tempQuery,tempDoc,tempPost;
	    int docId = 0, vocabularyId;
	    int df,tf = 0;
	    ArrayList<String> document;
	    ArrayList<Object> queryId;
	    ArrayList<Object> queryVector, docVector;
	    double vocaScore,frequencyFactor = 0,uniquenessFactor = 0, cosAngleScore;
	    ArrayList<Object> relevantObject;
	    ArrayList<String> relevantDoc;
	    
		// Temp return
	    return query;

	}

	public double getAngle(ArrayList<Object> q, ArrayList<Object> d) {
		// Temp return
		return 0;
	}

}
