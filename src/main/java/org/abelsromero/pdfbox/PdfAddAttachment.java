package org.abelsromero.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ABEL.SALGADOROMERO on 16/02/2016.
 */
public class PdfAddAttachment {

    public static final String SRC_FILE = "Test Document 1.pdf";

    public static void main(String[] args) throws IOException {

        PDDocument doc = PDDocument.load(new File("src/main/resources", SRC_FILE));

        PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

        // first create the file specification, which holds the embedded file
        PDComplexFileSpecification fs = new PDComplexFileSpecification();
        fs.setFile("Test.pdf");
        InputStream is = new PdfAddAttachment().getClass().getClassLoader().getResourceAsStream(SRC_FILE);

        PDEmbeddedFile ef = new PDEmbeddedFile(doc, is);
        // set some of the attributes of the embedded file
        ef.setSubtype("test/plain");
        ef.setSize(130759);
        ef.setCreationDate(new GregorianCalendar());
        fs.setEmbeddedFile(ef);

        // now add the entry to the embedded file tree and set in the document.
        Map efMap = new HashMap();
        efMap.put("custom_attachment", fs);
        efTree.setNames(efMap);

        // attachments are stored as part of the "names" dictionary in the document catalog
        PDDocumentNameDictionary names = new PDDocumentNameDictionary(doc.getDocumentCatalog());
        names.setEmbeddedFiles(efTree);
        doc.getDocumentCatalog().setNames(names);

        doc.save(new File("output.pdf"));

    }

}
