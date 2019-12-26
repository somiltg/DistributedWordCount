package taskallocation;

/**
 * Represents a single unit of task allocated by the word count master to the worker that it needs to execute and notify
 * completed.
 *
 * @author somilgupta
 */
public final class Task {
    /**
     * Absolute pathname of the file that needs to be executed upon.
     */
    private final String inputFile;
    /**
     * Absolute pathname of the file where result needs to be output.
     */
    private final String outputFile;

    public Task(String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    /**
     * Returns absolute pathname of the file that needs to be executed upon.
     *
     * @return Input file path name.
     */
    public String getInputFile() {
        return inputFile;
    }

    /**
     * Returns absolute pathname of the file where result needs to be output.
     *
     * @return Output file path name.
     */
    public String getOutputFile() {
        return outputFile;
    }
}
