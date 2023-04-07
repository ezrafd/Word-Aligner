import java.io.*;
import java.util.*;

public class WordAligner {
    protected HashMap<String, Double> engCounts; // counts for english words
    protected HashMap<String, HashMap<String, Double>> pairCounts; // outer word is engWord and inner word is frWord
    protected HashMap<String, HashMap<String, Double>> probs; // outer word is engWord and inner word is frWord
    protected HashMap<String, HashMap<String, Double>> prevProbs;
    protected HashMap<String, Double> wordTally; // the literal count of how many times each english word occurs in the text


    public WordAligner(String english_sentences, String foreign_sentences, int iterations, double probability_threshold) throws IOException {

        //make first iteration method
        //then make another method for another iteration which we can have in a for loop in the constructor

        engCounts = new HashMap<>();
        pairCounts = new HashMap<>();
        probs = new HashMap<>();

        firstIteration(english_sentences, foreign_sentences);

        for (int i = 0; i < iterations - 1; i++) {
            prevProbs = new HashMap<>();
            // Create a deep copy of probs
            for (String engWord : probs.keySet()) {
                prevProbs.put(engWord, new HashMap<>());
                for (String frWord : probs.get(engWord).keySet()) {
                    prevProbs.get(engWord).put(frWord, probs.get(engWord).get(frWord));
                }
            }

            nextIteration(english_sentences, foreign_sentences);

            calculateDifference();
        }

        //printProbs(probability_threshold);

    }

//    public void initializeWordTally(String english_sentences) throws IOException {
//        // Read English sentences from file
//        File engFile = new File(english_sentences);
//        BufferedReader engBr = new BufferedReader(new FileReader(engFile));
//        String engLine = engBr.readLine();
//
//        // Loop through each pair of English sentences
//        while (engLine != null) {
//            // Split English sentence into words
//            String[] engWords = engLine.split("\\s+");
//
//            for (String engWord : engWords) {
//                if (!wordTally.containsKey(engWord)) {
//                    wordTally.put(engWord, 0.0);
//                }
//
//                wordTally.put(engWord, wordTally.get(engWord) + 1.0);
//            }
//
//            engLine = engBr.readLine();
//        }
//    }

    /**
     * Processes the first iteration of the EM algorithm.
     *
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
            engLine = "yasharmut " + engLine;

            // Split English and foreign sentences into words
            String[] engWords = engLine.split("\\s+");
            String[] frWords = frLine.split("\\s+");

            // Update counts for each English word and its corresponding foreign words
            for (String engWord : engWords) {
                if (!pairCounts.containsKey(engWord)) {
                    // Initialize a new HashMap for the English word if it hasn't been seen before
                    pairCounts.put(engWord, new HashMap<>());
                    engCounts.put(engWord, 0.0);
                }
                for (String frWord : frWords) {
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

        // Compute the probability for each foreign word given each English word
        for (String engWord : pairCounts.keySet()) {
            probs.put(engWord, new HashMap<>());
            for (String frWord : pairCounts.get(engWord).keySet()) {
                prob = pairCounts.get(engWord).get(frWord) / engCounts.get(engWord);
                probs.get(engWord).put(frWord, prob);
            }
        }

        //System.out.println(probs);
    }


    /**
     * @param english_sentences
     * @param foreign_sentences
     * @throws IOException
     */
    public void nextIteration(String english_sentences, String foreign_sentences) throws IOException {

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

        HashMap<String, HashMap<String, Double>> alignmentProbs; // outer word is frWord and inner word is engWord
        double denominator;
        double pairCount;
        double engCount;

        // Loop through each pair of English and foreign sentences
        while (engLine != null && frLine != null) {
            engLine = "yasharmut " + engLine;

            // Split English and foreign sentences into words
            String[] engWords = engLine.split("\\s+");
            String[] frWords = frLine.split("\\s+");

            alignmentProbs = new HashMap<>();

            for (String engWord : engWords) {
                for (String frWord : frWords) {
                    denominator = 0.0;

                    alignmentProbs.put(frWord, new HashMap<>());

                    for (String eWord : engWords) {
                        denominator += probs.get(eWord).get(frWord);
                    }

                    alignmentProbs.get(frWord).put(engWord, probs.get(engWord).get(frWord) / denominator);

                    pairCount = pairCounts.get(engWord).get(frWord) + alignmentProbs.get(frWord).get(engWord);
                    pairCounts.get(engWord).put(frWord, pairCount);

                    engCount = engCounts.get(engWord) + alignmentProbs.get(frWord).get(engWord);
                    engCounts.put(engWord, engCount);
                }
            }

            // Read the next pair of English and foreign sentences
            engLine = engBr.readLine();
            frLine = frBr.readLine();
        }

        double prob;

        // Compute the probability for each foreign word given each English word
        for (String engWord : pairCounts.keySet()) {
            for (String frWord : pairCounts.get(engWord).keySet()) {
                prob = pairCounts.get(engWord).get(frWord) / engCounts.get(engWord);
                probs.get(engWord).put(frWord, prob);
            }
        }

        //System.out.println(probs);
    }


    public void printProbs(double probability_threshold) {
        Set<String> engWords = engCounts.keySet();
        List<String> sortedEngWords = new ArrayList<>(engWords);
        Collections.sort(sortedEngWords);

        for (String engWord : sortedEngWords) {
            Set<String> frWords = probs.get(engWord).keySet();
            List<String> sortedFrWords = new ArrayList<>(frWords);
            Collections.sort(sortedFrWords);


            for (String frWord : frWords) {
                if (probs.get(engWord).get(frWord) >= probability_threshold) {
                    if (engWord.equals("yasharmut")) {
                        System.out.println("NULL\t" + frWord + "\t" + probs.get(engWord).get(frWord));
                    } else {
                        System.out.println(engWord + "\t" + frWord + "\t" + probs.get(engWord).get(frWord));
                    }
                }
            }
        }
    }


    public void calculateDifference() {
        double sum = 0.0;

        for (String engWord : probs.keySet()) {
            for (String frWord : probs.get(engWord).keySet()) {
                sum += probs.get(engWord).get(frWord) - prevProbs.get(engWord).get(frWord);
            }
        }
        System.out.println("Sum of Difference: " + sum);
    }

//    public void calculateLogLikelihood() {
//        double corpProb = 1.0;
//        double maxValue;
//        double maxValueScaled;
//
//        for (String word : wordTally.keySet()) {
//            // find the maximum value from the HashMap
//            maxValue = Collections.max(probs.get(word).values());
//            maxValueScaled = maxValue * (1/Math.exp(-2.75));
//
//            corpProb *= Math.pow(maxValueScaled, wordTally.get(word));
//        }
//
//        double logLikelihood = Math.log(corpProb);
//        System.out.println("Log-Likelihood: " + logLikelihood);
//    }


//    public void calculateLogProb() {
//        double corpProb = 1.0;
//
//        for (String word : wordTally.keySet()) {
//            // find the maximum value from the HashMap
//            Double maxValue = Collections.max(probs.get(word).values());
//
//            corpProb *= Math.pow(Math.log10(maxValue + 1), wordTally.get(word));
//        }
//
//        System.out.println("Log-Likelihood: " + corpProb);
//    }


//    public void printAnalysis(){
//        //probs avg
//        double low = 1.5;
//        double high = 0.0;
//        double cur = 0.0;
//        for (String engWord : probs.keySet()){
//            for (String frWord : probs.get(engWord).keySet()) {
//                cur = probs.get(engWord).get(frWord);
//                if (cur > high){
//                    high = cur;
//                }
//                if (cur < low){
//                    low = cur;
//                }
//            }
//        }
//        double range = high-low;
//        System.out.println("range: " + range);
//    }


    public static void main(String[] args) throws IOException {
//        String enSentences = "/Users/ezraford/Desktop/School/CS 159/Word-Aligner/data/test.en";
//        String frSentences = "/Users/ezraford/Desktop/School/CS 159/Word-Aligner/data/test.fr";
        String enSentences = "/Users/talmordoch/Library/Mobile Documents/com~apple~CloudDocs/Word-Aligner/data/es-en.10k.en";
        String frSentences = "/Users/talmordoch/Library/Mobile Documents/com~apple~CloudDocs/Word-Aligner/data/es-en.10k.es";
        WordAligner test = new WordAligner(enSentences, frSentences, 10, 0.3);


    }
}