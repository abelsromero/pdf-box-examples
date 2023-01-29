package org.abelsromero.pdfbox;

import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;

/**
 * @author abelsromero
 */
public class PdfAValidator {

    public static void main(String[] args) throws Exception {

        ValidationResult result;

        PreflightParser parser = new PreflightParser(args[0]);
        try {

            /* Parse the PDF file with PreflightParser that inherits from the NonSequentialParser.
             * Some additional controls are present to check a set of PDF/A requirements.
             * (Stream length consistency, EOL after some Keyword...)
             */
            parser.parse();

            /* Once the syntax validation is done,
             * the parser can provide a PreflightDocument
             * (that inherits from PDDocument)
             * This document process the end of PDF/A validation.
             */
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();

            // Get validation result
            result = document.getResult();
            document.close();

        } catch (SyntaxValidationException e) {
            /* the parse method can throw a SyntaxValidationException
             * if the PDF file can't be parsed.
             * In this case, the exception contains an instance of ValidationResult
             */
            result = e.getResult();
        }

        // display validation result
        if (result.isValid()) {
            System.out.println("The file " + args[0] + " is a valid PDF/A-1b file");
        } else {
            System.out.println("The file" + args[0] + " is not valid, error(s) :");
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                System.out.println(error.getErrorCode() + " : " + error.getDetails());
            }
        }
    }
}
