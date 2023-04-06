import java.io.*;
import java.util.HashMap;

public class WordAligner {
    protected HashMap<String, Double> engCounts; // counts for english words
    protected HashMap<String, HashMap<String, Double>> pairCounts; // outer word is engWord and inner word is frWord
    protected HashMap<String, HashMap<String, Double>> probs; // outer word is engWord and inner word is frWord

    public WordAligner(String english_sentences, String foreign_sentences, double iterations, double probability_threshold) throws IOException {

        //make first iteration method
        //then make another method for another iteration which we can have in a for loop in the constructor

        engCounts = new HashMap<>();
        pairCounts = new HashMap<>();
        probs = new HashMap<>();

        firstIteration(english_sentences, foreign_sentences);
        nextIteration(english_sentences, foreign_sentences);
    }

    /**
     * Processes the first iteration of the EM algorithm.
     * @param english_sentences file path for the file containing English sentences.
     * @param foreign_sentences file path for the file containing foreign sentences.
     * @throws IOException if there's an error reading the input files.
     */
    public void firstIteration(String english_sentences, String foreign_sentences) throws IOException {
        double probConstant = 0.01;
        double prob;

        // Read English sentences from file
        File engFile = new File(english_sentences);
        BufferedReader engBr = new BufferedReader(new FileReader(engFile));
        String engLine = engBr.readLine();

        // Read foreign sentences from file
        File frFile = new File(foreign_sentences);
        BufferedReader frBr = new BufferedReader(new FileReader(frFile));
        String frLine = frBr.readLine();

        // Loop through each pair of English and foreign sentences
        while (engLine != null && frLine != null) {
            // Split English and foreign sentences into words
            String[] engWords = engLine.split("\\s+");
            String[] frWords = frLine.split("\\s+");

            // Update counts for each English word and its corresponding foreign words
            for (String engWord : engWords){
                if (!pairCounts.containsKey(engWord)) {
                    // Initialize a new HashMap for the English word if it hasn't been seen before
                    pairCounts.put(engWord, new HashMap<>());
                    engCounts.put(engWord, 0.0);
                }
                for (String frWord : frWords){
                    if (!pairCounts.get(engWord).containsKey(frWord)) {
                        // Initialize a count of zero for the (English, foreign) word pair if it hasn't been seen before
                        pairCounts.get(engWord).put(frWord, 0.0);
                    }

                    prob = probConstant / (probConstant * engWords.length);

                    // Increment the count for the (English, foreign) word pair
                    pairCounts.get(engWord).put(frWord, pairCounts.get(engWord).get(frWord) + prob);

                    // Increment the count for the English word
                    engCounts.put(engWord, engCounts.get(engWord) + prob);
                }
            }

            // Read the next pair of English and foreign sentences
            engLine = engBr.readLine();
            frLine = frBr.readLine();
        }

        System.out.println(pairCounts);
        System.out.println(engCounts);

        // Compute the probability for each foreign word given each English word
        for (String engWord : pairCounts.keySet()) {
            probs.put(engWord, new HashMap<>());
            for (String frWord : pairCounts.get(engWord).keySet()) {
                prob = pairCounts.get(engWord).get(frWord) / engCounts.get(engWord);
                probs.get(engWord).put(frWord, prob);
            }
        }

        System.out.println(probs);
    }


    /**
     * @param english_sentences
     * @param foreign_sentences
     * @throws IOException
     */
    public void nextIteration(String english_sentences, String foreign_sentences) throws IOException {
        double prob;

        // Clear the counts for each (English, foreign) word pair and English word
        for (String engWord : pairCounts.keySet()) {
            for (String frWord : pairCounts.get(engWord).keySet()) {
                pairCounts.get(engWord).put(frWord, 0.0);
            }
            engCounts.put(engWord, 0.0);
        }

        // Read English sentences from file
        File engFile = new File(english_sentences);
        BufferedReader engBr = new BufferedReader(new FileReader(engFile));
        String engLine = engBr.readLine();

        // Read foreign sentences from file
        File frFile = new File(foreign_sentences);
        BufferedReader frBr = new BufferedReader(new FileReader(frFile));
        String frLine = frBr.readLine();

        // Loop through each pair of English and foreign sentences
        while (engLine != null && frLine != null) {
            // Split English and foreign sentences into words
            String[] engWords = engLine.split("\\s+");
            String[] frWords = frLine.split("\\s+");

            HashMap<String, Double> total_s = new HashMap<>();
            for (String engWord : engWords) {
                total_s.put(engWord, 0.0);
            }

            for (String engWord : engWords) {
                for (String frWord : frWords) {
                    total_s.put(engWord, total_s.get(engWord) + probs.get(engWord).get(frWord));
                }
            }

            for (String engWord : engWords) {
                for (String frWord : frWords) {
                    pairCounts.get(engWord).put(frWord, pairCounts.get(engWord).get(frWord)
                            + (probs.get(engWord).get(frWord) / total_s.get(engWord)));
                    engCounts.put(engWord, engCounts.get(engWord)
                            + (probs.get(engWord).get(frWord) / total_s.get(engWord)));
                }
            }

            // Read the next pair of English and foreign sentences
            engLine = engBr.readLine();
            frLine = frBr.readLine();
        }

        System.out.println(pairCounts);
        System.out.println(engCounts);

        // Compute the probability for each foreign word given each English word
        for (String engWord : pairCounts.keySet()) {
            probs.put(engWord, new HashMap<>());
            for (String frWord : pairCounts.get(engWord).keySet()) {
                prob = pairCounts.get(engWord).get(frWord) / engCounts.get(engWord);
                probs.get(engWord).put(frWord, prob);
            }
        }

        System.out.println(probs);
    }


    public static void main(String[] args) throws IOException {
        String enSentences = "/Users/ezraford/Desktop/School/CS 159/Word-Aligner/data/test.en";
        String frSentences = "/Users/ezraford/Desktop/School/CS 159/Word-Aligner/data/test.fr";
        WordAligner test = new WordAligner(enSentences, frSentences, 1, 0.0);
    }



}