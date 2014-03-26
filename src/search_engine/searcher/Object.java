package search_engine.searcher;

/**
 * This class is used to store objects we use in Relevant class
 * 
 * Similar to the Vocabulary
 * 
 * @author Ludvig Kratz
 */
class Object {
	private String name;
	private int Id;
	private double score;

	public Object(String u, int di, double s) {
		name = u;
		Id = di;
		score = s;
	}

	public double getScore () {
		return score;
	}
	
	public int getId () {
		return Id;
	}
	
	public String getName () {
		return name;
	}
}
