package org.abelsromero.pdfbox.api;

import org.abelsromero.pdfbox.api.internal.ImageExtractor;
import org.abelsromero.pdfbox.ex.PdfProcessingException;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

import java.io.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.abelsromero.pdfbox.ex.PdfProcessingException.wrap;

/**
 * Provides the tools to add images to preexisting pdf
 *
 * @author asalgadr
 */
public class PdfImagesHelper {

    private static final String SAVE_GRAPHICS_STATE = "q\n";
    private static final String RESTORE_GRAPHICS_STATE = "Q\n";
    private static final String CONCATENATE_MATRIX = "cm\n";
    private static final String XOBJECT_DO = "Do\n";
    private static final String SPACE = " ";

    private static final NumberFormat formatDecimal = NumberFormat.getNumberInstance(Locale.US);

    private PDDocument pdfDocument;

    /**
     * Private constructor to prevent instantiation
     */
    private PdfImagesHelper() {
        pdfDocument = new PDDocument();
    }

    /**
     * Private constructor to prevent instantiation
     */
    private PdfImagesHelper(InputStream pdf) {
        try {
            pdfDocument = PDDocument.load(pdf);
        } catch (IOException e) {
            wrap(e);
        }
    }

    /**
     * @param image file to the image
     * @param page  page to insert, counting from 1
     * @param x     horizontal position from the lower left corner of the page
     * @param y     vertical position from the lower left corner of the page
     * @param text  (Nullable) text to add to the image as a note
     */
    public PdfImagesHelper stampImage(final File image, final int page, final float x, final float y, final String text) {

        if (page <= 0) throw new IndexOutOfBoundsException("page must be greater or equal to 1");

        PDPage pdPage = pdfDocument.getPage(page - 1);
        List<PDAnnotation> annotations = null;
        try {
            annotations = pdPage.getAnnotations();
        } catch (IOException e) {
            wrap(e);
        }
        PDAnnotationRubberStamp rubberStamp = new PDAnnotationRubberStamp();
        rubberStamp.setName(PDAnnotationRubberStamp.NAME_APPROVED);
        if (text != null && text.length() > 0) {
            rubberStamp.setRectangle(new PDRectangle(200, 100));
            rubberStamp.setContents(text);
        }

        // createEmptyPdf a PDXObjectImage with the given image file
        // if you already have the image in a BufferedImage,
        // call LosslessFactory.createFromImage() instead
        PDImageXObject ximage = null;
        try {
            ximage = PDImageXObject.createFromFileByContent(image, pdfDocument);
        } catch (IOException e) {
            wrap(e);
        }

        PDRectangle pageArea = pdPage.getCropBox();
        assertPositionAndSize(x, y, ximage, pdPage);

        // define and set the target rectangle
        int formWidth = ximage.getWidth();
        int formHeight = ximage.getHeight();
        int imgWidth = ximage.getWidth();
        int imgHeight = ximage.getHeight();

        PDRectangle rect = new PDRectangle();
        // Most cases lower x & y is 0, but better safe than sorry
        rect.setLowerLeftX(x + pageArea.getLowerLeftX());
        rect.setLowerLeftY(y + pageArea.getLowerLeftY());
        rect.setUpperRightX(x + formWidth);
        rect.setUpperRightY(y + formHeight);

        // Create a PDFormXObject
        PDFormXObject form = new PDFormXObject(pdfDocument);
        form.setResources(new PDResources());
        form.setBBox(rect);
        form.setFormType(1);

        // adjust the image to the target rectangle and add it to the stream
        try {
            OutputStream os = form.getStream().createOutputStream();
            drawXObject(ximage, form.getResources(), os, x, y, imgWidth, imgHeight);
            os.close();
        } catch (IOException e) {
            wrap(e);
        }

        PDAppearanceStream myDic = new PDAppearanceStream(form.getCOSObject());
        PDAppearanceDictionary appearance = new PDAppearanceDictionary(new COSDictionary());
        appearance.setNormalAppearance(myDic);
        rubberStamp.setAppearance(appearance);
        rubberStamp.setRectangle(rect);

        // add the new RubberStamp to the document
        annotations.add(rubberStamp);

        return this;
    }

    /**
     * Quick method to validate position and size.
     */
    private void assertPositionAndSize(float x, float y, PDImageXObject image, PDPage page) throws PdfProcessingException {
        PDRectangle pageArea = page.getCropBox();
        // Assert position is in page
        if (x < pageArea.getLowerLeftX() || x > pageArea.getUpperRightX())
            throw new PdfProcessingException("X position not valid. (" + x + "," + y + ") out of page");
        if (y < pageArea.getLowerLeftY() || y > pageArea.getUpperRightY())
            throw new PdfProcessingException("Y position not valid. (" + x + "," + y + ") out of page");

        // Assert image fits
        if (x + image.getWidth() > pageArea.getUpperRightX() ||
                y + image.getHeight() > pageArea.getUpperRightY())
            throw new PdfProcessingException("Image dos not fit in page");
    }

    // https://svn.apache.org/repos/asf/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/RubberStampWithImage.java
    private void drawXObject(PDImageXObject xobject, PDResources resources, OutputStream os,
                             float x, float y, float width, float height) throws IOException {
        // This is similar to PDPageContentStream.drawXObject()
        COSName xObjectId = resources.add(xobject);

        appendRawCommands(os, SAVE_GRAPHICS_STATE);
        appendRawCommands(os, formatDecimal.format(width));
        appendRawCommands(os, SPACE);
        appendRawCommands(os, formatDecimal.format(0));
        appendRawCommands(os, SPACE);
        appendRawCommands(os, formatDecimal.format(0));
        appendRawCommands(os, SPACE);
        appendRawCommands(os, formatDecimal.format(height));
        appendRawCommands(os, SPACE);
        appendRawCommands(os, formatDecimal.format(x));
        appendRawCommands(os, SPACE);
        appendRawCommands(os, formatDecimal.format(y));
        appendRawCommands(os, SPACE);
        appendRawCommands(os, CONCATENATE_MATRIX);
        appendRawCommands(os, SPACE);
        appendRawCommands(os, "/");
        appendRawCommands(os, xObjectId.getName());
        appendRawCommands(os, SPACE);
        appendRawCommands(os, XOBJECT_DO);
        appendRawCommands(os, SPACE);
        appendRawCommands(os, RESTORE_GRAPHICS_STATE);
    }

    private void appendRawCommands(OutputStream os, String commands) throws IOException {
        os.write(commands.getBytes("ISO-8859-1"));
    }

    /**
     * Adds a fixed transparent layer with an image on top of the selected page.
     * <p>
     * NOTE: Due to technical limitations, this operation has a relative impact in memory consumption.
     * NOTE: using addPage disables this.
     *
     * @param image     file to the image
     * @param page      page to insert, counting from 1
     * @param x         relative position from the lower left corner of the page
     * @param y         relative position from the lower left corner of the page
     * @param boxHeight height to scale the image to
     */
    public PdfImagesHelper overlayImage(final File image, final int page, final int x, final int y, final float boxHeight) {

        if (page <= 0) throw new IndexOutOfBoundsException("page must be greater or equal to 1");

        // Load image only to assert size
        PDImageXObject ximage = null;
        try {
            ximage = PDImageXObject.createFromFileByContent(image, pdfDocument);
        } catch (IOException e) {
            wrap(e);
        }
        PDPage pdPage = pdfDocument.getPage(page - 1);
        assertPositionAndSize(x, y, ximage, pdPage);

        // Create temporal 1-page PDF in memory
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // Calculate scale factor
        float imageHeight = ximage.getHeight();
        float scale = boxHeight / imageHeight;

        // FIX ME only overlay on first page works.
        // Need to try to generate a full pdf with empty pages to select a page
        // Creating only previous pages does not work. Maybe add the rest of pages behing?
        PdfImagesHelper ih = Builder.createEmptyPdf();
        ih.addPage();
        ih.replaceWithImage(image, 1, x, y, scale).writeTo(os);

        // ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        File temp = null;
        try {
            temp = File.createTempFile("pdf-overlay-", ".pdf");
            FileUtils.writeByteArrayToFile(temp, os.toByteArray());
        } catch (IOException e) {
            wrap(e);
        }

        Map<Integer, String> overlayGuide = new HashMap<Integer, String>();
        //overlayGuide.put(1, temp.getAbsolutePath());

        for (int i = 0; i < pdfDocument.getNumberOfPages(); i++) {
            overlayGuide.put(i, temp.getAbsolutePath());
            //watermark.pdf is the document which is a one page PDF with your watermark image in it.
            //Notice here, you can skip pages from being watermarked.
        }

        Overlay overlay = new Overlay();
        overlay.setInputPDF(pdfDocument);
        overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
        try {
            overlay.setFirstPageOverlayFile(temp.getAbsolutePath());
            // overlay method needs to ve invoked
            overlay.overlay(new HashMap<Integer, String>());
        } catch (IOException e) {
            wrap(e);
        }
        return this;
    }

    /**
     * Replaces the content of the page with an image.
     *
     * @param image file to the image
     * @param page  page to insert, counting from 1
     * @param x     relative position from the lower left corner of the page
     * @param y     relative position from the lower left corner of the page
     */
    public PdfImagesHelper replaceWithImage(final File image, final int page, final int x, final int y) {
        return replaceWithImage(image, page, x, y, 1f);
    }

    /**
     * Replaces the content of the page with an image.
     *
     * @param image file to the image
     * @param page  page to insert, counting from 1
     * @param x     relative position from the lower left corner of the page
     * @param y     relative position from the lower left corner of the page
     * @param scale scaling factor (1 = original size)
     */
    public PdfImagesHelper replaceWithImage(final File image, final int page, final int x, final int y, final float scale) {

        if (page <= 0) throw new IndexOutOfBoundsException("page must be greater or equal to 1");

        PDPage pdPage = pdfDocument.getPage(page - 1);
        try {
            PDImageXObject pdImage = PDImageXObject.createFromFileByContent(image, pdfDocument);

            // Load image only to assert size
            PDImageXObject ximage = null;
            try {
                ximage = PDImageXObject.createFromFileByContent(image, pdfDocument);
            } catch (IOException e) {
                wrap(e);
            }
            assertPositionAndSize(x, y, ximage, pdPage);

            PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, pdPage);
            contentStream.drawImage(pdImage, x, y, pdImage.getWidth() * scale, pdImage.getHeight() * scale);
            contentStream.close();
        } catch (IOException e) {
            wrap(e);
        }
        return this;
    }

    /**
     * Writes all images to a directory following the pattern: basename-{pageNum}-{counter}.{extension}
     */
    public List<Image> writeImagesToDir(File path, String basename) {
        ImageExtractor ie = new ImageExtractor(pdfDocument,path,basename);
        return ie.process().getImages();
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

    /**
     * Adds a black page to the PDF.
     * <p>
     * NOTE: using it disables overlayImage
     */
    public PdfImagesHelper addPage() {
        PDPage page = new PDPage();
        pdfDocument.getPages().add(page);
        return this;
    }

    public static class Builder {

        /**
         * Builder method to createEmptyPdf an empty PDF
         */
        public static PdfImagesHelper createEmptyPdf() {
            return new PdfImagesHelper();
        }

        /**
         * Builder method
         */
        public static PdfImagesHelper loadPdf(InputStream pdf) {
            return new PdfImagesHelper(pdf);
        }

        /**
         * Builder method
         *
         * @param pdf PDF file
         */
        public static PdfImagesHelper loadPdf(File pdf) {
            try {
                return new PdfImagesHelper(new FileInputStream(pdf));
            } catch (IOException e) {
                wrap(e);
            }
            // don't bother explaining...because Java
            return null;
        }
    }

    public int getPagesCount() {
        return pdfDocument.getNumberOfPages();
    }

}
