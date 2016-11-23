package org.abelsromero.pdfbox.utils;

import org.abelsromero.pdfbox.PdfSplitter;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Generic methods to manipulate files
 * <p>
 * Created by ABEL.SALGADOROMERO on 07/07/2016.
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
