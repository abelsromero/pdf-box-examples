package org.abelsromero.pdfbox;

import org.abelsromero.pdfbox.api.PdfPageSelector;
import org.abelsromero.pdfbox.api.SelectorOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.abelsromero.pdfbox.TestUtils.createTestDirectory;
import static org.abelsromero.pdfbox.utils.LocalUtils.getFileFromClassPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author asalgadr
 */
class PdfPageSelectorTest {

    @Test
    void should_extract_a_single_page() throws IOException {
        File pdf = getFileFromClassPath("asciidoctor-example-manual.pdf");
        String outputFilename = "output.pdf";
        File outputDir = createTestDirectory();

        PdfPageSelector.builder()
            .file(pdf)
            .pages(2)
            .writeTo(new File(outputDir, outputFilename));

        File output = new File(outputDir, outputFilename);
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages()).hasSize(1);
    }

    @Test
    void should_fail_on_invalid_page_index() {
        File pdf = getFileFromClassPath("asciidoctor-example-manual.pdf");
        String outputFilename = "output.pdf";
        File outputDir = createTestDirectory();

        Throwable throwable = catchThrowable(() -> {
            PdfPageSelector.builder()
                .file(pdf)
                .pages(-1)
                .writeTo(new File(outputDir, outputFilename));
        });

        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_extract_3_unordered_pages() throws IOException {
        File pdf = getFileFromClassPath("asciidoctor-example-manual.pdf");
        String outputFilename = "output.pdf";
        File outputDir = createTestDirectory();

        PdfPageSelector.builder()
            .file(pdf)
            .pages(3, 2, 1)
            .writeTo(new File(outputDir, outputFilename));

        // pdf with pages 3 2 1 in that order is generated
        File output = new File(outputDir, outputFilename);
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages()).hasSize(3);
    }

    @Disabled("uses private document not present in the repo")
    void should_extract_a_page_and_remove_PDFA_nature() throws IOException {
        File sourcePdf = getFileFromClassPath("pdfa/Data_00000001.pdf");
        String outputFilename = "output.pdf";
        File outputDir = createTestDirectory();

        PdfPageSelector.builder()
            .file(sourcePdf)
            .pages(2)
            .options(SelectorOptions.CREATE_NEW)
            .writeTo(new File(outputDir, outputFilename));

        File output = new File(outputDir, outputFilename);
        assertThat(output).exists();
        assertThat(isPDFA(output)).isTrue();
        assertThat(PDDocument.load(output).getPages()).hasSize(2);
    }

    @Disabled("uses private document not present in the repo")
    void should_extract_the_same_page_and_keep_PDFA_nature() throws IOException {
        File sourcePdf = getFileFromClassPath("pdfa/Data_00000001.pdf");
        String outputFilename = "output.pdf";
        File outputDir = createTestDirectory();

        PdfPageSelector.builder()
            .file(sourcePdf)
            .pages(2, 2)
            .options(SelectorOptions.with().copyMetadata().copyIntents())
            .writeTo(new File(outputDir, outputFilename));

        assertThat(isPDFA(sourcePdf)).isTrue();
        File output = new File(outputDir, outputFilename);
        assertThat(output).exists();
        assertThat(isPDFA(outputDir)).isTrue();
        assertThat(PDDocument.load(output).getPages()).hasSize(2);
    }

    @Disabled("uses private document not present in the repo")
    void should_extract_1_page_and_keep_PDFA_nature() throws IOException {
        File pdf = getFileFromClassPath("pdfa/example_065.pdf");
        String outputFilename = "output.pdf";
        File outputDir = createTestDirectory();

        assertThat(isPDFA(pdf)).isTrue();

        PdfPageSelector.builder()
            .file(pdf)
            .pages(1)
            .options(SelectorOptions.COPY_PDFA_PROPERTIES)
            .writeTo(new File(outputDir, outputFilename));

        File output = new File(outputDir, outputFilename);
        assertThat(output).exists();
        assertThat(isPDFA(output)).isTrue();
        assertThat(PDDocument.load(output).getPages()).hasSize(1);
    }

    @Disabled("uses private document not present in the repo")
    void should_extract_3_page_and_keep_PDFA_nature() throws IOException {
        File pdf = getFileFromClassPath("pdfa/Webinar_PDFA.pdf");
        String outputFilename = "output.pdf";
        File outputDir = createTestDirectory();

        assertThat(isPDFA(pdf)).isTrue();

        PdfPageSelector.builder()
            .file(pdf)
            .pages(1, 3, 7, 8, 9, 9, 9)
            .writeTo(new File(outputDir, outputFilename));

        File output = new File(outputDir, outputFilename);
        assertThat(output).exists();
        assertThat(isPDFA(output)).isTrue();
        assertThat(PDDocument.load(output).getPages()).hasSize(7);
    }

    @Test
    void should_extract_a_single_page_to_an_Stream() throws IOException {
        File pdf = getFileFromClassPath("asciidoctor-example-manual.pdf");
        var outputPdf = new ByteArrayOutputStream();

        PdfPageSelector.builder()
            .file(pdf)
            .pages(2, 7, 2)
            .writeTo(outputPdf);

        assertThat(PDDocument.load(outputPdf.toByteArray()).getPages()).hasSize(3);
    }

    @Test
    void should_not_fail_when_processing_a_non_PDFA_with_PDFA_options() throws IOException {
        File pdf = getFileFromClassPath("asciidoctor-example-manual.pdf");
        String outputFilename = "output.pdf";
        File outputDir = createTestDirectory();

        PdfPageSelector.builder()
            .file(pdf)
            .pages(3, 1, 4, 1, 5)
            .options(SelectorOptions.COPY_PDFA_PROPERTIES)
            .writeTo(new File(outputDir, outputFilename));

        File output = new File(outputDir, outputFilename);
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages()).hasSize(5);
    }

    // TODO look for alternative method, not sure this is 100% reliable
    boolean isPDFA(File file) {
        try {
            PreflightParser parser = new PreflightParser(file);

            parser.parse();

            PreflightDocument document = parser.getPreflightDocument();
            document.validate();

            final ValidationResult result = document.getResult();
            document.close();

            // display validation result
            if (result.isValid()) {
                System.out.println("The file $file.name is a valid PDF/A-1b file");
            } else {
                System.out.println("The file $file.name is not valid, error(s) :");
                for (ValidationResult.ValidationError error : result.getErrorsList()) {
                    System.out.println(error.getErrorCode() + " : " + error.getDetails());
                }
            }
            return result.isValid();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
