package search_engine.searcher;

class Tuple {

    private int docId;
    private int tf;

    public Tuple(int di, int t){
        docId = di;
        tf = t;
    }

    public double getScore () {
        return tf;
    }

    public int getId () {
        return docId;
    }


}

