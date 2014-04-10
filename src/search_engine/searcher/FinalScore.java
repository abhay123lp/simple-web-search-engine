package search_engine.searcher;

/**
 * Created by vokey on 3/4/14.
 */
public class FinalScore {
    private double score;
    private int docId;

    public FinalScore(double s, int i){
        score = s;
        docId = i;
    }

    public double getScore(){ return score; }
    public int getId(){ return docId;}
}
