package org.abelsromero.pdfbox.utils;

import org.abelsromero.pdfbox.PdfSplitter;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Generic methods to manipulate files.
 *
 * @author abelsromero
 */
public class LocalUtils {

    public static File getFileFromClassPath(String path) {
        try {
            return new File(PdfSplitter.class.getClassLoader().getResource(path).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
