package org.abelsromero.pdfbox.api;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.io.*;

import static org.abelsromero.pdfbox.ex.PdfProcessingException.wrap;

/**
 * Provides methods to rotate PDFs 90 degrees left or right.
 *
 * @author abelsromero
 */
public class PdfRotator {

    private PDDocument pdfDocument;

    /**
     * Private constructor to prevent instantiation
     */
    private PdfRotator() {
        pdfDocument = new PDDocument();
    }

    /**
     * Private constructor to prevent instantiation
     */
    private PdfRotator(InputStream pdf) {
        try {
            pdfDocument = PDDocument.load(pdf);
        } catch (IOException e) {
            wrap(e);
        }
    }

    /**
     * Saves the PDF to a File.
     *
     * @param file target file
     */
    public void writeTo(File file) {
        try {
            pdfDocument.save(file);
        } catch (IOException e) {
            wrap(e);
        }
    }

    /**
     * Saves the PDF to an output stream.
     *
     * @param outputStream target output stream
     */
    public void writeTo(OutputStream outputStream) {
        try {
            pdfDocument.save(outputStream);
        } catch (IOException e) {
            wrap(e);
        }
    }

    public PdfRotator rotateRight() throws IOException {
        return rotate(270);
    }

    public PdfRotator rotateLeft() throws IOException {
        return rotate(90);
    }

    private PdfRotator rotate(int radius) throws IOException {
        PDPageTree pages = pdfDocument.getDocumentCatalog().getPages();
        for (PDPage page : pages) {
            PDPageContentStream cs = new PDPageContentStream(pdfDocument, page, PDPageContentStream.AppendMode.PREPEND, false, false);
            Matrix matrix = Matrix.getRotateInstance(Math.toRadians(radius), 0, 0);
            cs.transform(matrix);

            PDRectangle cropBox = page.getCropBox();
            Rectangle rectangle = cropBox.transform(matrix).getBounds();
            PDRectangle newBox = new PDRectangle((float) rectangle.getX(), (float) rectangle.getY(), (float) rectangle.getWidth(), (float) rectangle.getHeight());
            page.setCropBox(newBox);
            page.setMediaBox(newBox);
            cs.close();
        }
        return this;
    }

    public static PdfRotator loadPdf(File pdf) {
        try {
            return new PdfRotator(new FileInputStream(pdf));
        } catch (IOException e) {
            wrap(e);
        }
        // don't bother explaining...because Java
        return null;
    }

}
