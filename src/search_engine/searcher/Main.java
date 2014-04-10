package search_engine.searcher;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main (String[] args) {
        String query = "";
        for (int i=0; i<args.length; i++) {
            query += args[i] + " ";
        }

        Relevant rel = new Relevant ();
        ArrayList<String> results = new ArrayList<String>();
        try {

            results = rel.getRelevant(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i=0; i<results.size(); i++) {
            System.out.println (results.get(i));
        }
    }
}
