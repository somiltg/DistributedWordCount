package wordcount;

import taskallocation.ITaskExecutor;
import taskallocation.Task;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WordCounter implements ITaskExecutor {

    @Override
    public void execute(final Task task) {
        log("Executing WordCounter for input file " + task.getInputFile() + " at output file " + task.getOutputFile());
        Scanner scanFile;
        Map<String, Integer> wordCount = new HashMap<>();
        try {
            Thread.sleep(50000);
            scanFile = new Scanner(new File(task.getInputFile()));
            while (scanFile.hasNext()) {
                String next = scanFile.next();
                wordCount.put(next, wordCount.getOrDefault(next, 0) + 1);
            }
        } catch (FileNotFoundException | InterruptedException e) {
            log("IO Exception occurred while reading files");
            e.printStackTrace();
        }
        log("HashMap created from file : " + task.getInputFile());
        storeWordCount(wordCount, task.getOutputFile());
    }

    private void storeWordCount(final Map<String, Integer> wordCount, final String out) {
        try {
            FileOutputStream fos = new FileOutputStream(out);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(wordCount);
            oos.close();
            fos.close();
        } catch (IOException e) {
            log("IO Exception occurred while writing to file : " + e.getMessage());
            e.printStackTrace();
        }
        log("Stored HashMap to output file : " + out);
    }

    private static void log(final String message) {
        ITaskExecutor.log(message, "WordCounter");
    }
}
