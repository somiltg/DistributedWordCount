package wordcount;

import java.io.*;
import java.util.*;

import static util.ApplicationUtils.getOutputFile;

public class MergerSorter {
    private static boolean DEBUG = true;

    public void execute(String location, String[] filenames, String outputFileExtension, OutputStream resultStream) {
        HashMap<String, Integer> wordCountMaster;
        log("Calling Merger for merging the worker outputs");
        wordCountMaster = merger(location, filenames, outputFileExtension);
        log("Calling sorter to sort the merged data");
        wordCountMaster = sorter(wordCountMaster);
        log("Printing the result to the stream");
        writer(wordCountMaster, resultStream);
    }

    private HashMap<String, Integer> merger(String location, String[] filenames, String outputFileExtension) {
        HashMap<String, Integer> wordCountWorker;
        HashMap<String, Integer> wordCountMaster = new HashMap<>();
        for (String filename : filenames) {
            FileInputStream fis;
            try {
                String outputFileName = getOutputFile(filename, location, outputFileExtension);
                fis = new FileInputStream(outputFileName);
                log("Reading File " + outputFileName);
                ObjectInputStream ois = new ObjectInputStream(fis);
                wordCountWorker = ((HashMap) ois.readObject());
                wordCountWorker.forEach((k, v) -> {
                    if (!wordCountMaster.containsKey(k)) {
                        wordCountMaster.put(k, v);
                    } else {
                        wordCountMaster.put(k, wordCountMaster.get(k) + v);
                    }
                });
                ois.close();
                fis.close();
                log("File " + outputFileName + " merged to main Hashmap.");
            } catch (IOException e) {
                log("IO error in reading files at Merger. Error: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                log("Error in casting to HashMap " + e.getMessage());
                e.printStackTrace();
            }
        }
        log("Merging done! Sending the merged HashMap back.");
        return wordCountMaster;
    }

    private HashMap<String, Integer> sorter(HashMap<String, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer>> list = new LinkedList<>(hm.entrySet());
        // Sort the list
        list.sort((o1, o2) -> {
            if (o1.getValue().equals(o2.getValue())) {
                return o1.getKey().compareTo(o2.getKey());
            } else {
                return Integer.compare(o2.getValue(), o1.getValue());
            }
        });
        HashMap<String, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            sorted.put(aa.getKey(), aa.getValue());
        }
        log("Sorting done! Sending the sorted HashMap back.");
        return sorted;
    }

    private void writer(HashMap<String, Integer> wordCount, OutputStream resultStream) {
        PrintStream ps = new PrintStream(resultStream);
        wordCount.forEach((k, v) -> ps.println(v + " : " + k));
        log("Written to OutputStream. Operation accomplished.");
    }

    private static void log(final String message) {
        if (DEBUG)
            System.out.println(String.format("[Master][MergerSorter][Time: %d] %s", System.nanoTime(), message));
    }
}
