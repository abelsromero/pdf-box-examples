package org.abelsromero.pdfbox.ex;

/**
 * Unchecked exception used to wrap PDFBox checked ones.
 *
 * @author asalgadr on 15/11/2016.
 */
public class PdfProcessingException extends RuntimeException {

    public PdfProcessingException() {
    }

    public PdfProcessingException(String message) {
        super(message);
    }

    public PdfProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PdfProcessingException(Throwable cause) {
        super(cause);
    }

    public static void wrap(Throwable t) {
        throw new PdfProcessingException(t);
    }
}
