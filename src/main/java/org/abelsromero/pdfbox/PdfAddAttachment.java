package org.abelsromero.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author abelsromero
 */
public class PdfAddAttachment {

    public static final String SRC_PDF_FILE = "simple-pdf.pdf";
    public static final String SRC_ATACHMENT_FILE = "sample.adoc";

    public static void main(String[] args) throws IOException {

        final PDDocument doc = PDDocument.load(new File("src/main/resources", SRC_PDF_FILE));

        // Create EmbeddedFile object (/Type /EmbeddedFile) with the actual content
        final File attachment = new File("src/main/resources", SRC_ATACHMENT_FILE);
        final PDEmbeddedFile ef = new PDEmbeddedFile(doc, new FileInputStream(attachment));
        // set some of the attributes of the embedded file
        ef.setSubtype("text/plain");
        ef.setSize((int) attachment.length());
        ef.setCreationDate(Calendar.getInstance());

        // Create the file specification (/Type /Filespec /F .. /EF), which holds the embedded file
        final PDComplexFileSpecification fs = new PDComplexFileSpecification();
        fs.setFile("sample.adoc");
        fs.setEmbeddedFile(ef);

        final PDComplexFileSpecification fs2 = new PDComplexFileSpecification();
        fs2.setFile("sample2.adoc");
        fs2.setEmbeddedFile(ef);

        // Add the entry to the embedded file tree and set in the document.
        Map efMap = new HashMap();
        efMap.put("custom_attachment", fs);
        efMap.put("custom_attachment2", fs2);
        final PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();
        efTree.setNames(efMap);

        // Attachments are stored as part of the "/Names" dictionary in the document /Catalog
        final PDDocumentNameDictionary names = new PDDocumentNameDictionary(doc.getDocumentCatalog());
        names.setEmbeddedFiles(efTree);
        doc.getDocumentCatalog().setNames(names);

        doc.save(new File("pdf-with-attachment.pdf"));

    }

}
