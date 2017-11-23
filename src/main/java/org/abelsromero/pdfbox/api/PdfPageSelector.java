package org.abelsromero.pdfbox.api;

import org.abelsromero.pdfbox.api.streams.PagesCollector;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import sun.nio.ch.SelChImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.abelsromero.pdfbox.ex.PdfProcessingException.wrap;

/**
 * Created by ABEL.SALGADOROMERO on 20/11/2017.
 * <p>
 * Allows to generate a PDF selecting some of the pages of another.
 */
public class PdfPageSelector {

    private File sourceFile;
    private int[] pages;
    private SelectorOptions options = SelectorOptions.CREATE_NEW;

    private PdfPageSelector() {
    }

    public static PdfPageSelector builder() {
        return new PdfPageSelector();
    }

    public PdfPageSelector file(File file) {
        this.sourceFile = file;
        return this;
    }

    public PdfPageSelector pages(int... pages) {
        this.pages = pages;
        return this;
    }

    public PdfPageSelector options(SelectorOptions options) {
        this.options = options;
        return this;
    }

    public void writeTo(final File outputFile) {
        try {
            selectPages()
                .save(outputFile);
        } catch (IOException e) {
            wrap(e);
        }
    }

    public void writeTo(final OutputStream outputStream) {
        try {
            selectPages()
                .save(outputStream);
        } catch (IOException e) {
            wrap(e);
        }
    }

    private PDDocument selectPages() throws IOException {

        final PDDocument document = PDDocument.load(sourceFile);

        int pagesCount = document.getNumberOfPages();
        for (int pageIndex : pages) {
            if (pageIndex <= 0 || pageIndex > pagesCount)
                throw new IllegalArgumentException(String.format("Invalid page index: %s", pageIndex));
        }

        final PDPageTree documentPages = document.getPages();
        final PDDocument output = Arrays.stream(pages)
            .map(i -> i - 1)
            .mapToObj(documentPages::get)
            .collect(new PagesCollector());

        // Copy catalog properties for PDF/A compliance

        final PDDocumentCatalog catalog = document.getDocumentCatalog();
        final PDDocumentCatalog outputCatalog = output.getDocumentCatalog();
        if (options.isCopyMetadataEnabled()) {
            outputCatalog.setMetadata(catalog.getMetadata());
        }
        if (options.isCopyIntentsEnabled()) {
            catalog.getOutputIntents()
                .forEach(outputCatalog::addOutputIntent);
        }

        return output;
    }

    private PDOutputIntent createStandardRGBIntent(PDDocument document) throws IOException {
        InputStream colorProfile = PdfPageSelector.class.getClassLoader().getResourceAsStream("sRGB.icc");
        PDOutputIntent intent = new PDOutputIntent(document, colorProfile);
        intent.setInfo("sRGB IEC61966-2.1");
        intent.setOutputCondition("sRGB IEC61966-2.1");
        intent.setOutputConditionIdentifier("sRGB IEC61966-2.1");
        intent.setRegistryName("http://www.color.org");
        return intent;
    }

    private List<PDFont> getFonts(PDPage page) {
        final List<PDFont> fonts = new ArrayList<>();
        final PDResources resources = page.getResources();
        for (COSName fontName : page.getResources().getFontNames()) {
            try {
                fonts.add(resources.getFont(fontName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fonts;
    }


}
