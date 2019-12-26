package util;

import java.io.File;
import java.util.Objects;

import static resource.IResourceManager.WORKER_ID_PREFIX;

/**
 * Utility functions required across the application. Should hold only generic functions required by the distributed
 * Master-Slave implementation. Applications should define their business specific utility functions elsewhere.
 *
 * @author somilgupta
 */
public class ApplicationUtils {
    /**
     * Generates output file location from the given input file absolute path, output file location and the extension.
     * It assumes that output file would have the same name as the input file but location of the file and its
     * extension would be different.
     *
     * @param absoluteInputFile Absolute path of the input file.
     * @param outputFileLoc     Location of the output file.
     * @param extension         Extension to be ended with. Send null if no extension needed.
     * @return Absolute path of the output file.
     */
    public static String getOutputFile(final String absoluteInputFile, final String outputFileLoc, final String extension) {
        String inputFileName = new File(absoluteInputFile).getName();
        assert !inputFileName.isEmpty();
        //Remove old extension
        int dotIdx = inputFileName.indexOf('.');
        inputFileName = dotIdx == -1 ? inputFileName : inputFileName.substring(0, dotIdx);
        // Add new extension
        String ext = (Objects.isNull(extension) || extension.isEmpty() ? "" :
                extension.startsWith(".") ? extension : "." + extension);
        //Modify output file location.
        String outLoc = outputFileLoc.endsWith("/") ? outputFileLoc : outputFileLoc + "/";
        return outLoc + inputFileName + ext;
    }

    /**
     * Derives workerId for the given index.
     *
     * @param idx Should be a non-negative number.
     * @return WorkerId.
     */
    public static int processId(final int idx) {
        assert idx >= 0;
        return WORKER_ID_PREFIX * 10 + idx;
    }

    private ApplicationUtils() {
    }
}
