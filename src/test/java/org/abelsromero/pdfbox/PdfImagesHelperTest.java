package org.abelsromero.pdfbox;


import org.abelsromero.pdfbox.api.Image;
import org.abelsromero.pdfbox.api.PdfImagesHelper;
import org.abelsromero.pdfbox.ex.PdfProcessingException;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.abelsromero.pdfbox.TestUtils.createTestDirectory;
import static org.abelsromero.pdfbox.utils.LocalUtils.getFileFromClassPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class PdfImagesHelperTest {


    @Test
    void should_create_a_blank_pdf_with_two_pages() throws IOException {
        final File outputDir = createTestDirectory();

        PdfImagesHelper.Builder.createEmptyPdf()
            .addPage()
            .addPage()
            .writeTo(new File(outputDir, "output.pdf"));

        File output = new File(outputDir, "output.pdf");
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages()).hasSize(2);
    }


    @Test
    void should_fail_if_input_is_not_an_image() {
        File notAnImage = getFileFromClassPath("sample.pdf");
        File outputDir = createTestDirectory();

        Throwable throwable = catchThrowable(() -> {
            PdfImagesHelper.Builder.createEmptyPdf()
                .addPage()
                .overlayImage(notAnImage, 1, 50, 50, 0)
                .writeTo(new File(outputDir, "output.pdf"));
        });

        // Unhandled exception. We don't care the exact type
        assertThat(throwable).isInstanceOf(Exception.class);
    }

    @Test
    void should_stamp_a_PNG_image_to_the_first_page_in_a_single_page_pdf() throws IOException {
        File input = getFileFromClassPath("sample.pdf");
        File image = getFileFromClassPath("ruby-icon.png");
        File outputDir = createTestDirectory("stamp");

        float x = PDRectangle.A4.getWidth() / 2f;
        float y = PDRectangle.A4.getHeight() / 2f;
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 1, x, y, "")
            .writeTo(new File(outputDir, "output.pdf"));


        File output = new File(outputDir, "output.pdf");
        assertThat(output).exists();
        assertThat(output.length()).isGreaterThan(input.length());
        assertThat(PDDocument.load(output).getPages()).hasSize(1);
    }

    @Test
    void should_stamp_a_PNG_image_to_the_second_page() throws IOException {
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf");
        File image = getFileFromClassPath("ruby-icon.png");
        File outputDir = createTestDirectory("stamp");

        float x = PDRectangle.A4.getWidth() / 2f;
        float y = PDRectangle.A4.getHeight() / 2f;
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 2, x, y, "")
            .writeTo(new File(outputDir, "output.pdf"));

        File output = new File(outputDir, "output.pdf");
        assertThat(output).exists();
        assertThat(output.length()).isGreaterThan(input.length());
        assertThat(PDDocument.load(output).getPages()).hasSizeGreaterThan(2);
    }

    @Test
    void should_overlay_a_PNG_image_to_the_second_page() throws IOException {
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf");
        File image = getFileFromClassPath("ruby-icon.png");
        File outputDir = createTestDirectory("overlay");

        PdfImagesHelper.Builder.loadPdf(input)
            .overlayImage(image, 2, 50, 50, 50)
            .writeTo(new File(outputDir, "output.pdf"));

        File output = new File(outputDir, "output.pdf");
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages()).hasSizeGreaterThan(1);
    }

    @Disabled("internal test")
    void should_overlay_a_Contract_image_to_the_second_page() throws IOException {
        File input = getFileFromClassPath("CONTRATO.pdf");
        File ruby = getFileFromClassPath("ruby-icon.png");
        File gif = getFileFromClassPath("200w_s.gif");
        File image = getFileFromClassPath("signature.jpg");
        File outputDir = createTestDirectory("contrato");

        PdfImagesHelper.Builder.loadPdf(input)
            .overlayImage(image, 1, 240, 107, 70)
            .writeTo(new File(outputDir, "CONTRATO-output-sign.pdf"));
        PdfImagesHelper.Builder.loadPdf(input)
            .overlayImage(ruby, 1, 240, 107, 70)
            .writeTo(new File(outputDir, "CONTRATO-output-ruby.pdf"));
        PdfImagesHelper.Builder.loadPdf(input)
            .overlayImage(gif, 1, 240, 107, 70)
            .writeTo(new File(outputDir, "CONTRATO-output-gif.pdf"));

        PdfImagesHelper.Builder.loadPdf(getFileFromClassPath("sample_eid.pdf"))
            .overlayImage(getFileFromClassPath("arthur_samples/sample.gif"), 1, 240, 107, 70)
            .writeTo(new File(outputDir, "sample_eid-signed.pdf"));

        List.of("bmp", "gif", "jpg", "png", "tiff")
            .stream()
            .forEach(fileExtension -> {
                var im = getFileFromClassPath("arthur_samples/sample." + fileExtension);
                try {
                    PdfImagesHelper.Builder.loadPdf(input)
                        .overlayImage(im, 1, 240, 107, 70)
                        .writeTo(new File(outputDir, "CONTRATO-output-ART-${fileExtension}.pdf"));
                } catch (Exception e) {
                    System.out.println("FAILED; " + im);
                }
            });


        File output = new File(outputDir, "CONTRATO-output.pdf");
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages()).hasSizeGreaterThan(1);
    }

    @Test
    void should_replace_a_single_paged_pdf_with_an_image() throws IOException {
        File input = getFileFromClassPath("sample.pdf");
        File image = getFileFromClassPath("ruby-icon.png");
        File outputDir = createTestDirectory("replace");

        PdfImagesHelper.Builder.loadPdf(input)
            .replaceWithImage(image, 1, 100, 100)
            .writeTo(new File(outputDir, "output.pdf"));

        // "Text page is replaced by image"
        File output = new File(outputDir, "output.pdf");
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages()).hasSize(1);
    }

    @Test
    void should_add_images_to_some_pages() throws IOException {
        File input = getFileFromClassPath("sample.pdf");
        File image = getFileFromClassPath("ruby-icon.png");
        File outputDir = createTestDirectory("replace");

        PdfImagesHelper.Builder.loadPdf(input)
            .addPage()
            .addPage()
            .replaceWithImage(image, 2, 100, 100)
            .addPage()
            .replaceWithImage(image, 4, 100, 100)
            .writeTo(new File(outputDir, "output.pdf"));

        // "1st page contains text, 2n and 4th an image, 3rd is blank"
        File output = new File(outputDir, "output.pdf");
        assertThat(output).exists();
        assertThat(PDDocument.load(output).getPages()).hasSize(4);
    }

    @Test
    void should_fail_if_image_does_not_fit_in_page() {
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf");
        File image = getFileFromClassPath("ruby-icon.png");

        float x = PDRectangle.A4.getWidth() - 10f;
        float y = PDRectangle.A4.getHeight() - 10f;

        Throwable throwable = catchThrowable(() -> {
            PdfImagesHelper.Builder.loadPdf(input)
                .stampImage(image, 1, x, y, "")
                .writeTo(new File(createTestDirectory("stamp"), "output.pdf"));
        });

        assertThat(throwable).isInstanceOf(PdfProcessingException.class);
    }

    @Test
    void should_fail_stamp_if_position_is_out_of_page__upper_limit() {
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf");
        File image = getFileFromClassPath("ruby-icon.png");

        float x = PDRectangle.A4.getWidth() * 2;
        float y = PDRectangle.A4.getHeight() * 2;
        Throwable throwable = catchThrowable(() -> {
            PdfImagesHelper.Builder.loadPdf(input)
                .stampImage(image, 1, x, y, "")
                .writeTo(new File(createTestDirectory("stamp"), "output.pdf"));

        });

        assertThat(throwable).isInstanceOf(PdfProcessingException.class);
    }

    @Test
    void should_fail_stamp_if_position_is_out_of_page__lower_limit() {
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf");
        File image = getFileFromClassPath("ruby-icon.png");

        float x = -1f;
        float y = -1f;
        Throwable throwable = catchThrowable(() -> {
            PdfImagesHelper.Builder.loadPdf(input)
                .stampImage(image, 1, x, y, "")
                .writeTo(new File(createTestDirectory("stamp"), "output.pdf"));

        });

        assertThat(throwable).isInstanceOf(PdfProcessingException.class);
    }

    @Test
    void should_fail_if_page_is_out_of_bounds__higher() {
        File input = getFileFromClassPath("sample.pdf");
        File image = getFileFromClassPath("ruby-icon.png");
        File outputDir = createTestDirectory();

        // "index page is higher"
        float x = PDRectangle.A4.getWidth() / 2f;
        float y = PDRectangle.A4.getHeight() / 2f;
        Throwable throwable = catchThrowable(() -> {
            PdfImagesHelper.Builder.loadPdf(input)
                .stampImage(image, 2, x, y, "")
                .writeTo(new File(outputDir, "output.pdf"));
        });

        // Unhandled exception. We don"t care the exact type
        assertThat(throwable).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void should_fail_if_page_is_out_of_bounds__lower() {
        File input = getFileFromClassPath("sample.pdf");
        File image = getFileFromClassPath("ruby-icon.png");
        File outputDir = createTestDirectory();

        // "index page is higher"
        float x = PDRectangle.A4.getWidth() / 2f;
        float y = PDRectangle.A4.getHeight() / 2f;
        Throwable throwable = catchThrowable(() -> {
            PdfImagesHelper.Builder.loadPdf(input)
                .stampImage(image, 0, x, y, "")
                .writeTo(new File(outputDir, "output.pdf"));
        });

        // Unhandled exception. We don"t care the exact type
        assertThat(throwable).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void should_write_all_images_to_an_output_directory() throws IOException {
        File input = getFileFromClassPath("document-with-images.pdf");
        File outputDir = createTestDirectory();
        File imagesDir = new File(outputDir, "extracted-images");

        PdfImagesHelper helper = PdfImagesHelper.Builder.loadPdf(input);
        var images = helper.writeImagesToDir(imagesDir, FilenameUtils.getName(input.getName()));

        assertThat(imagesDir.listFiles()).hasSize(2);
        assertThat(images).hasSize(2);
        Image firstImage = images.get(0);
        assertThat(firstImage.getOriginalHeight()).isEqualTo(104);
        assertThat(firstImage.getOriginalWidth()).isEqualTo(104);
        Image secondImage = images.get(1);
        assertThat(secondImage.getOriginalHeight()).isEqualTo(630);
        assertThat(secondImage.getOriginalWidth()).isEqualTo(1200);
    }

    @Test
    void should_extract_all_images() throws IOException {
        File input = getFileFromClassPath("document-with-images.pdf");

        PdfImagesHelper helper = PdfImagesHelper.Builder.loadPdf(input);
        var images = helper.getRenderedImages();

        assertThat(images).hasSize(2);
    }
}
