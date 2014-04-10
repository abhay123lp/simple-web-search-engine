package search_engine.searcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
     * @throws java.io.IOException
     */
    public ArrayList<String> getRelevant(String queryFromUser) throws IOException {
        String[] temp;
        ArrayList<String> query = new ArrayList<String>();
        //TODO: Tokenize, stem, stopword filter to get the query from queryFromUser
        WhitespaceTokenizer tokenizer = WhitespaceTokenizer.INSTANCE;
        IStemmer stemmer = new Stemmer();
        StopwordFilter filter = new StopwordFilter(stemmer, "stopwords.txt");
        filter.initialize();

        
        /**Tokenize, stem and remove stop word from query
         */
        temp = tokenizer.tokenize(queryFromUser);

        for(int i = 0; i < temp.length; i++){
            temp[i] = temp[i].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            temp[i] = stemmer.stem(temp[i]);
            if(!filter.isStopword(temp[i])){
                query.add(temp[i]);
            }
        }
        /**Record tf for query
         */
        int count = 0;
        ArrayList<String> list = new ArrayList<String>();
        for (String s : query) {
            if (!list.contains(s)) {
                list.add(s);
            }
        }
        double[] queryVector = new double [list.size()];
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < query.size(); j++) {
                if (list.get(i).equals(query.get(i))) {
                    count++;
                }
            }
            queryVector[i] = count;
            count = 0;
        }


        ArrayList<String> document = createDoc();
        ArrayList<Object> queryId =  createQuery(list);
        queryVector = createQueryVector(queryVector,document,queryId);


        Collections.sort(queryId, new CustomComparatorObject());

        double[][] scores = createScores(queryId,document);

        ArrayList<FinalScore> finalScore = createFinal(document, queryId,queryVector, scores);
        Collections.sort(finalScore, new CustomComparatorFinalScore());

        ArrayList<String> relevantDoc = createRelevant(finalScore,document);

        return relevantDoc;

    }

    /**Calculate the angle between two vectors
      *Used to calculate the score for each document
      *based on query
      */
    public double getAngle(double[] q, double[] d) {
        double cosAngle,mag1 = 0.0,mag2 = 0.0,scalar = 0;
        for (int i = 0; i < q.length; i++) {
            scalar += q[i]*d[i];
            if (q[i] == 0 || d[i] == 0) {
                return 0;
            }
            mag1 += Math.pow(q[i], 2);
            mag2 += Math.pow(d[i], 2);
        }
        mag1 = Math.sqrt(mag1);
        mag2 = Math.sqrt(mag2);

        cosAngle = (scalar)/(mag1*mag2);
        return cosAngle;
    }

    
    /**Returns the tuple given a id
      */
    public Tuple getTuple(int id, ArrayList<Tuple> t){
        for(int i = 0; i < t.size(); i++){
            if(t.get(i).getId() == id)
                return t.get(i);
        }
        return null;
    }

    /**Custom sort function for Object, sort based on ID
      */
    public class CustomComparatorObject implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            return o1.getId() - (o2.getId());
        }
    }

    /**Custom sort function for FinalScore, sort based on score
     */
    public class CustomComparatorFinalScore implements Comparator<FinalScore> {
        @Override
        public int compare(FinalScore f1, FinalScore f2) {
            if (f1.getScore() < f2.getScore()){
                return 1;
            }else if(f1.getScore() == f2.getScore()){
                return 0;
            }else{
                return -1;
            }
        }
    }
    /*Get df and vocabularyID for each query token using DICTIONARYLIST text file
	    if word not in the dictionary, ignore it. */
    public ArrayList<Object> createQuery(ArrayList<String> list)throws IOException{
        String tempQuery;
        int vocabularyId,df;
        ArrayList<Object> queryId = new ArrayList<Object>();

        for (int i = 0; i < list.size(); i++) {
            vocabularyId = 0;

            // Initialise the Stream readers
            FileInputStream fis = new FileInputStream("dictionary.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            do {
                line = br.readLine();
                String[] tokens = line.split(" ");

                if (line != null) {
                    tempQuery = tokens[0].toLowerCase();
                    df = Integer.parseInt(tokens[1]);

                    list.set(i, list.get(i).toLowerCase());

                    if (tempQuery.compareTo(list.get(i)) == 0) {
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
        return queryId;
    }

    // Read the Document List file and store into document
    public ArrayList<String> createDoc()throws IOException{
        ArrayList<String> document = new ArrayList<String>();

        FileInputStream fis = new FileInputStream("documents.txt");
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
        return document;
    }



    /**Creates the query vector
      */
    public double[] createQueryVector(double[] queryVector,ArrayList<String> document, ArrayList<Object> queryId)throws IOException{
        for (int i = 0; i < queryVector.length; i++){
            queryVector[i] = (1+Math.log10(queryVector[i]))*(document.size()/queryId.get(i).getScore());
        }
        return queryVector;
    }


    // For each document, calculate the relevant score
    public double[][] createScores(ArrayList<Object> queryId,ArrayList<String> document)throws IOException{
        double[][] scores = new double[document.size()][queryId.size()];
        FileInputStream fis = new FileInputStream("postings.txt");
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        int lineNumber = -1;

        // record down the tf for all the document
        for (int i=0; i<queryId.size(); i++) {
            Object token = queryId.get(i);
            String line;
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

            ArrayList<Tuple> postingList = new ArrayList<Tuple>();

            String[] post = line.split(" ");
            for (int j = 0; j < post.length; j++){
                String[] idNscore = post[j].split(",");
                Tuple t = new Tuple(Integer.parseInt(idNscore[0]),Integer.parseInt(idNscore[1]));
                postingList.add(t);
            }
            for (int _docId=0; _docId<document.size(); _docId++) {
                if(getTuple(_docId,postingList) != null){
                    scores[_docId][i] = (1+Math.log10(getTuple(_docId,postingList).getScore()))*(Math.log(1+document.size()/queryId.get(i).getScore()));
                }else{
                    scores[ _docId][i] = 0;
                }
            }
        }
        br.close();
        isr.close();
        fis.close();
        return scores;
    }

    /**Creates the FnialScore vector
      */
    public ArrayList<FinalScore> createFinal(ArrayList<String> document,ArrayList<Object> queryId,double[] queryVector, double[][] scores){
        ArrayList<FinalScore> finalScore = new ArrayList<FinalScore>();
        for (int i=0; i<document.size(); i++) {
            double[] documentVector = scores[i];
            if(queryId.size() == 1){
                finalScore.add(new FinalScore(scores[i][0],i));
            }else{
                finalScore.add(new FinalScore (getAngle(documentVector, queryVector), i));
            }
        }
        return finalScore;
    }

    /**Creates the vecotr with all the relevant documents
      */
    public ArrayList<String> createRelevant(ArrayList<FinalScore> finalScore, ArrayList<String> document){
        int K = 10;
        ArrayList<String> relevantDoc = new ArrayList<String>();
        for (int i = 0; i < K && finalScore.get(i).getScore() > 0; i++){
            relevantDoc.add(document.get(finalScore.get(i).getId()));
        }
        return relevantDoc;
    }

}
