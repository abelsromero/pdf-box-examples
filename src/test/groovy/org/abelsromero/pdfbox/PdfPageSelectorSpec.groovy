package org.abelsromero.pdfbox

import org.abelsromero.pdfbox.api.PdfPageSelector
import org.abelsromero.pdfbox.api.SelectorOptions
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.preflight.PreflightDocument
import org.apache.pdfbox.preflight.ValidationResult
import org.apache.pdfbox.preflight.parser.PreflightParser
import spock.lang.Ignore
import spock.lang.Specification

import static org.abelsromero.pdfbox.TestUtils.createTestDirectory
import static org.abelsromero.pdfbox.utils.LocalUtils.getFileFromClassPath

/**
 * @author asalgadr
 */
class PdfPageSelectorSpec extends Specification {

    def "should extract a single page"() {
        given:
        File pdf = getFileFromClassPath('asciidoctor-example-manual.pdf')
        String outputFilename = 'output.pdf'
        File outputDir = createTestDirectory()

        when:
        PdfPageSelector.builder()
            .file(pdf)
            .pages(2)
            .writeTo(new File(outputDir, outputFilename))

        then:
        File output = new File(outputDir, outputFilename)
        output.exists()
        PDDocument.load(output).pages.size() == 1
    }

    def "should fail on invalid page index"() {
        given:
        File pdf = getFileFromClassPath('asciidoctor-example-manual.pdf')
        String outputFilename = 'output.pdf'
        File outputDir = createTestDirectory()

        when:
        PdfPageSelector.builder()
            .file(pdf)
            .pages(-1)
            .writeTo(new File(outputDir, outputFilename))

        then:
        thrown(IllegalArgumentException)
    }

    def "should extract 3 unordered pages"() {
        given:
        File pdf = getFileFromClassPath('asciidoctor-example-manual.pdf')
        String outputFilename = 'output.pdf'
        File outputDir = createTestDirectory()

        when:
        PdfPageSelector.builder()
            .file(pdf)
            .pages(3, 2, 1)
            .writeTo(new File(outputDir, outputFilename))

        then: 'pdf with pages 3 2 1 in that order is generated'
        File output = new File(outputDir, outputFilename)
        output.exists()
        PDDocument.load(output).pages.size() == 3
    }

    @Ignore("uses private document not present in the repo")
    def "should extract a page and remove PDF/A nature"() {
        given:
        File sourcePdf = getFileFromClassPath('pdfa/Data_00000001.pdf')
        String outputFilename = 'output.pdf'
        File outputDir = createTestDirectory()

        when:
        PdfPageSelector.builder()
            .file(sourcePdf)
            .pages(2,)
            .options(SelectorOptions.CREATE_NEW)
            .writeTo(new File(outputDir, outputFilename))

        then:
        // isPDFA(sourcePdf)
        File outputFile = new File(outputDir, outputFilename)
        outputFile.exists()
        isPDFA(outputFile)
        PDDocument.load(outputFile).pages.size() == 2
    }

    @Ignore("uses private document not present in the repo")
    def "should extract the same page and keep PDF/A nature"() {
        given:
        File sourcePdf = getFileFromClassPath('pdfa/Data_00000001.pdf')
        String outputFilename = 'output.pdf'
        File outputDir = createTestDirectory()

        when:
        PdfPageSelector.builder()
            .file(sourcePdf)
            .pages(2, 2)
            .options(SelectorOptions.with().copyMetadata().copyIntents())
            .writeTo(new File(outputDir, outputFilename))

        then:
        isPDFA(sourcePdf)
        File outputFile = new File(outputDir, outputFilename)
        outputFile.exists()
        isPDFA(outputFile)
        PDDocument.load(outputFile).pages.size() == 2
    }

    @Ignore("uses private document not present in the repo")
    def "should extract 1 page and keep PDF/A nature"() {
        given:
        File pdf = getFileFromClassPath('pdfa/example_065.pdf')
        String outputFilename = 'output.pdf'
        File outputDir = createTestDirectory()
        isPDFA(pdf)

        when:
        PdfPageSelector.builder()
            .file(pdf)
            .pages(1)
            .options(SelectorOptions.COPY_PDFA_PROPERTIES)
            .writeTo(new File(outputDir, outputFilename))

        then:
        File outputFile = new File(outputDir, outputFilename)
        outputFile.exists()
        isPDFA(outputFile)
        PDDocument.load(outputFile).pages.size() == 1
    }

    def "should extract 3 page and keep PDFA nature"() {
        given:
        File pdf = getFileFromClassPath('pdfa/Webinar_PDFA.pdf')
        String outputFilename = 'output.pdf'
        File outputDir = createTestDirectory()
        isPDFA(pdf)

        when:
        PdfPageSelector.builder()
            .file(pdf)
            .pages(1, 3, 7, 8, 9, 9, 9)
            .writeTo(new File(outputDir, outputFilename))

        then:
        File outputFile = new File(outputDir, outputFilename)
        outputFile.exists()
        isPDFA(outputFile)
        PDDocument.load(outputFile).pages.size() == 7
    }

    def "should extract a single page to an Stream"() {
        given:
        File pdf = getFileFromClassPath('asciidoctor-example-manual.pdf')
        OutputStream outputPdf = new ByteArrayOutputStream()

        when:
        PdfPageSelector.builder()
            .file(pdf)
            .pages(2, 7, 2)
            .writeTo(outputPdf)

        then:
        PDDocument.load(outputPdf.toByteArray()).pages.size() == 3
    }

    def "should not fail when processing a non PDFA with PDFA options"() {
        given:
        File pdf = getFileFromClassPath('asciidoctor-example-manual.pdf')
        String outputFilename = 'output.pdf'
        File outputDir = createTestDirectory()

        when:
        PdfPageSelector.builder()
            .file(pdf)
            .pages(3, 1, 4, 1, 5)
            .options(SelectorOptions.COPY_PDFA_PROPERTIES)
            .writeTo(new File(outputDir, outputFilename))

        then:
        File outputFile = new File(outputDir, outputFilename)
        outputFile.exists()
        PDDocument.load(outputFile).pages.size() == 5
    }

    // TODO look for alternative method, not sure this is 100% reliable
    boolean isPDFA(File file) {
        PreflightParser parser = new PreflightParser(file)
        parser.parse()

        PreflightDocument document = parser.getPreflightDocument()
        document.validate()

        final ValidationResult result = document.getResult()
        document.close()

        // display validation result
        if (result.isValid()) {
            System.out.println("The file $file.name is a valid PDF/A-1b file")
        } else {
            System.out.println("The file $file.name is not valid, error(s) :")
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                System.out.println(error.getErrorCode() + " : " + error.getDetails())
            }
        }
        return result.isValid()
    }
}
