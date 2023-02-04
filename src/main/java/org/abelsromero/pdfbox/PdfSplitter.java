package org.abelsromero.pdfbox;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.abelsromero.pdfbox.utils.LocalUtils.getFileFromClassPath;

/**
 * Note: Splitter was located in package `org.apache.pdfbox.util` in PDFBox versions previous to 2.0.0
 *
 * @author abelsromero
 */
public class PdfSplitter {

    public static final String SRC_FILE = "20160706092157753.pdf";

    public static void main(String[] args) throws IOException {
        final File source = getFileFromClassPath(SRC_FILE);

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
            doc.save(new File(targetDirectory, "%s-%s".formatted(sourceFile.getName(), i) + ".pdf"));
        }
    }
}
