package org.abelsromero.pdfbox;

import org.abelsromero.pdfbox.api.PdfRotator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.abelsromero.pdfbox.TestUtils.createTestDirectory;
import static org.assertj.core.api.Assertions.assertThat;

public class PdfRotatorTest {

    public static final String SRC_PDF_FILE = "asciidoctor-example-manual.pdf";

    @Test
    void should_rotate_pages_to_right() throws IOException {
        File outputDir = createTestDirectory();
        File file = new File("src/test/resources", SRC_PDF_FILE);

        PdfRotator.Builder.loadPdf(file)
            .rotateRight()
            .writeTo(new File(outputDir, "output.pdf"));

        File output = new File(outputDir, "output.pdf");
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages())
            .hasSize(7)
            .allSatisfy(page -> {
                // not 100% accurate test. The actual content does not have a way to get size
                PDRectangle cropBox = page.getCropBox();
                // horizontal layout (width > height)
                assertThat(cropBox.getWidth()).isEqualTo(843);
                assertThat(cropBox.getHeight()).isEqualTo(596);
            });
    }

}
