package org.abelsromero.pdfbox;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.abelsromero.pdfbox.utils.LocalUtils.getFileFromClassPath;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * Created by ABEL.SALGADOROMERO on 16/02/2016.
 * <p>
 * Note: Splitter was located in package `org.apache.pdfbox.util` in PDFBox versions previous to 2.0.0
 */
public class PdfSplitter {

    public static final String SRC_FILE = "20160706092157753.pdf";

    public static void main(String[] args) throws IOException, URISyntaxException {
        File source = getFileFromClassPath(SRC_FILE);

        PdfSplitter splitter = new PdfSplitter();
        splitter.processFile(source);
    }

    public void processFile(File sourceFile) throws IOException {
        processFile(sourceFile, sourceFile.getParentFile());
    }

    public void processFile(File sourceFile, File targetDirectory) throws IOException {
        PDDocument document = PDDocument.load(sourceFile);
        Splitter splitter = new Splitter();

        List<PDDocument> pages = splitter.split(document);
        for (int i = 0; i < pages.size(); i++) {
            PDDocument doc = pages.get(i);
            doc.save(new File(targetDirectory, getBaseName(sourceFile.getName()) + "_" + i + ".pdf"));
        }
    }

}
