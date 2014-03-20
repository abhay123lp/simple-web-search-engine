package search_engine.searcher;

/**
 * This class is used to store objects we use in Relevant class
 * 
 * @author Ludvig Kratz
 */
class DataStructure {
	private String name;
	private int Id;
	private double score;

	public DataStructure(String u, int di, double s) {
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
