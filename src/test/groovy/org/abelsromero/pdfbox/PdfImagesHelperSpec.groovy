package org.abelsromero.pdfbox

import org.abelsromero.pdfbox.api.PdfImagesHelper
import org.abelsromero.pdfbox.ex.PdfProcessingException
import org.apache.commons.io.FilenameUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDRectangle
import spock.lang.Ignore
import spock.lang.Specification

import static org.abelsromero.pdfbox.utils.LocalUtils.getFileFromClassPath
import static org.abelsromero.pdfbox.TestUtils.createTestDirectory

/**
 * @author asalgadr
 */
class PdfImagesHelperSpec extends Specification {

    def "should create a blank pdf with a two pages"() {
        given:
        File outputDir = createTestDirectory()

        when:
        PdfImagesHelper.Builder.createEmptyPdf()
            .addPage()
            .addPage()
            .writeTo(new File(outputDir, 'output.pdf'))

        then:
        File output = new File(outputDir, 'output.pdf')
        output.exists()
        PDDocument.load(output).pages.size() == 2
    }

    def "should fail if input is not an image"() {
        given:
        File notAnImage = getFileFromClassPath('sample.pdf')
        File outputDir = createTestDirectory()

        when:
        PdfImagesHelper.Builder.createEmptyPdf()
            .addPage()
            .overlayImage(notAnImage, 1, 50, 50)
            .writeTo(new File(outputDir, 'output.pdf'))

        then:
        // Unhandled exception. We don't care the exact type
        thrown(Exception)
    }

    def "should stamp a PNG image to the first page in a single page pdf"() {
        given:
        File input = getFileFromClassPath("sample.pdf")
        File image = getFileFromClassPath("ruby-icon.png")
        File outputDir = createTestDirectory("stamp")

        when:
        float x = PDRectangle.A4.width / 2f
        float y = PDRectangle.A4.height / 2f
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 1, x, y, "")
            .writeTo(new File(outputDir, 'output.pdf'))

        then:
        File output = new File(outputDir, 'output.pdf')
        output.exists()
        output.size() > input.size()
        PDDocument.load(output).pages.size() == 1
    }

    def "should stamp a PNG image to the second page"() {
        given:
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf")
        File image = getFileFromClassPath("ruby-icon.png")
        File outputDir = createTestDirectory("stamp")

        when:
        float x = PDRectangle.A4.width / 2f
        float y = PDRectangle.A4.height / 2f
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 2, x, y, "")
            .writeTo(new File(outputDir, 'output.pdf'))

        then:
        File output = new File(outputDir, 'output.pdf')
        output.exists()
        output.size() > input.size()
    }

    def "should overlay a PNG image to the second page"() {
        given:
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf")
        File image = getFileFromClassPath("ruby-icon.png")
        File outputDir = createTestDirectory("overlay")

        when:
        PdfImagesHelper.Builder.loadPdf(input)
            .overlayImage(image, 2, 50, 50, 50)
            .writeTo(new File(outputDir, 'output.pdf'))

        then:
        File output = new File(outputDir, 'output.pdf')
        output.exists()
        PDDocument.load(output).pages.size() > 1
    }

    @Ignore("internal test")
    def "should overlay a Contract image to the second page"() {
        given:
        File input = getFileFromClassPath("CONTRATO.pdf")
        File ruby = getFileFromClassPath("ruby-icon.png")
        File gif = getFileFromClassPath("200w_s.gif")
        File image = getFileFromClassPath("signature.jpg")
        File outputDir = createTestDirectory("contrato")

        when:
        PdfImagesHelper.Builder.loadPdf(input)
            .overlayImage(image, 1, 240, 107, 70, 50)
            .writeTo(new File(outputDir, 'CONTRATO-output-sign.pdf'))
        PdfImagesHelper.Builder.loadPdf(input)
            .overlayImage(ruby, 1, 240, 107, 70, 50)
            .writeTo(new File(outputDir, 'CONTRATO-output-ruby.pdf'))
        PdfImagesHelper.Builder.loadPdf(input)
            .overlayImage(gif, 1, 240, 107, 70)
            .writeTo(new File(outputDir, 'CONTRATO-output-gif.pdf'))
        /**/
        PdfImagesHelper.Builder.loadPdf(getFileFromClassPath('sample_eid.pdf'))
            .overlayImage(getFileFromClassPath("arthur_samples/sample.gif"), 1, 240, 107, 70)
            .writeTo(new File(outputDir, 'sample_eid-signed.pdf'))

        /**
         */
        ['bmp', 'gif', 'jpg', 'png', 'tiff'].each { ext ->
            def im = getFileFromClassPath("arthur_samples/sample.$ext")
            try {
                PdfImagesHelper.Builder.loadPdf(input)
                    .overlayImage(im, 1, 240, 107, 70)
                    .writeTo(new File(outputDir, "CONTRATO-output-ART-${ext}.pdf"))
            } catch (e) {
                println "FAILED; $im"
            }
        }



        then:
        File output = new File(outputDir, 'CONTRATO-output.pdf')
        output.exists()
        PDDocument.load(output).pages.size() > 1
    }

    def "should replace a single paged pdf with an image"() {
        given:
        File input = getFileFromClassPath("sample.pdf")
        File image = getFileFromClassPath("ruby-icon.png")
        File outputDir = createTestDirectory("replace")

        when:
        PdfImagesHelper.Builder.loadPdf(input)
            .replaceWithImage(image, 1, 100, 100)
            .writeTo(new File(outputDir, 'output.pdf'))

        then: 'Text page is replaced by image'
        File output = new File(outputDir, 'output.pdf')
        output.exists()
        PDDocument.load(output).pages.size() == 1
    }

    def "should add images to some pages"() {
        given:
        File input = getFileFromClassPath("sample.pdf")
        File image = getFileFromClassPath("ruby-icon.png")
        File outputDir = createTestDirectory("replace")

        when:
        PdfImagesHelper.Builder.loadPdf(input)
            .addPage()
            .addPage()
            .replaceWithImage(image, 2, 100, 100)
            .addPage()
            .replaceWithImage(image, 4, 100, 100)
            .writeTo(new File(outputDir, 'output.pdf'))

        then: '1st page contains text, 2n and 4th an image, 3rd is blank'
        File output = new File(outputDir, 'output.pdf')
        output.exists()
        PDDocument.load(output).pages.size() == 4
    }

    def "should fail if image does not fit in page"() {
        given:
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf")
        File image = getFileFromClassPath("ruby-icon.png")

        when:
        float x = PDRectangle.A4.width - 10f
        float y = PDRectangle.A4.height - 10f
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 1, x, y, "")
            .writeTo(new File(createTestDirectory("stamp"), 'output.pdf'))

        then:
        thrown(PdfProcessingException)
    }

    def "should fail stamp if position is out of page - upper limit"() {
        given:
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf")
        File image = getFileFromClassPath("ruby-icon.png")

        when:
        float x = PDRectangle.A4.width * 2
        float y = PDRectangle.A4.height * 2
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 1, x, y, "")
            .writeTo(new File(createTestDirectory("stamp"), 'output.pdf'))

        then:
        thrown(PdfProcessingException)
    }

    def "should fail stamp if position is out of page - lower limit"() {
        given:
        File input = getFileFromClassPath("asciidoctor-example-manual.pdf")
        File image = getFileFromClassPath("ruby-icon.png")

        when:
        float x = -1f
        float y = -1f
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 1, x, y, "")
            .writeTo(new File(createTestDirectory("stamp"), 'output.pdf'))

        then:
        thrown(PdfProcessingException)
    }

    def "should fail if page is out of bounds - higher"() {
        given:
        File input = getFileFromClassPath("sample.pdf")
        File image = getFileFromClassPath("ruby-icon.png")
        File outputDir = createTestDirectory()

        when: 'index page is higher'
        float x = PDRectangle.A4.width / 2f
        float y = PDRectangle.A4.height / 2f
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 2, x, y, "")
            .writeTo(new File(outputDir, 'output.pdf'))

        then:
        // Unhandled exception. We don't care the exact type
        thrown(IndexOutOfBoundsException)
    }

    def "should fail if page is out of bounds - lower"() {
        given:
        File input = getFileFromClassPath("sample.pdf")
        File image = getFileFromClassPath("ruby-icon.png")
        File outputDir = createTestDirectory()

        when: 'index page is higher'
        float x = PDRectangle.A4.width / 2f
        float y = PDRectangle.A4.height / 2f
        PdfImagesHelper.Builder.loadPdf(input)
            .stampImage(image, 0, x, y, "")
            .writeTo(new File(outputDir, 'output.pdf'))

        then:
        // Unhandled exception. We don't care the exact type
        thrown(IndexOutOfBoundsException)
    }

    def "should write all images to an output directory"() {
        given:
        File input = getFileFromClassPath("document-with-images.pdf")
        File outputDir = createTestDirectory()
        File imagesDir = new File(outputDir, 'extracted-images')

        when:
        PdfImagesHelper helper = PdfImagesHelper.Builder.loadPdf(input);
        def images = helper.writeImagesToDir(imagesDir, FilenameUtils.getName(input.getName()))

        then:
        imagesDir.listFiles().size() == 2
        images.size() == 2
        images[0].originalHeight == 104
        images[0].originalWidth == 104
        images[1].originalHeight == 630
        images[1].originalWidth == 1200
    }

    def "should extract all images"() {
        given:
        File input = getFileFromClassPath("document-with-images.pdf")
        File outputDir = createTestDirectory()
        File imagesDir = new File(outputDir, 'extracted-images')

        when:
        PdfImagesHelper helper = PdfImagesHelper.Builder.loadPdf(input);
        def images = helper.getRenderedImages()

        then:
        images.size() == 2
    }

}
