package org.abelsromero.pdfbox.api.internal;

import org.abelsromero.pdfbox.api.Image;
import org.abelsromero.pdfbox.ex.PdfProcessingException;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Processor to extract images from a PDF and get information about them.
 *
 * @author abelsromero on 22/11/2016.
 */
public class ImageExtractor extends PDFStreamEngine {

    private final PDDocument pdfDocument;

    // Path to export images
    private final File output;
    // Base name for the images
    private final String basename;

    // Images information
    private List<Image> images;

    private static final String INVOKE_OPERATOR = "Do";

    private int currentPage;
    private int count = 1;


    public ImageExtractor(PDDocument document, File output, String basename) {
        images = new ArrayList<>();
        this.pdfDocument = document;
        this.output = output;
        this.basename = basename;
    }

    /**
     * Saves all images found in a PDF in a directory.
     */
    public ImageExtractor process() {
        try {
            currentPage = 0;
            for (PDPage page : pdfDocument.getPages()) {
                currentPage++;
                processPage(page);
            }

            pdfDocument.close();
        } catch (IOException e) {
            throw new PdfProcessingException(e);
        }
        return this;
    }


    /**
     * This is used to handle an operation.
     *
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if (INVOKE_OPERATOR.equals(operation)) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);
            if (xobject instanceof PDImageXObject) {

                PDImageXObject image = (PDImageXObject) xobject;
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();

                Image im = new Image();
                im.setPage(currentPage);
                im.setXPosition(ctmNew.getTranslateX());
                im.setYPosition(ctmNew.getTranslateY());
                im.setOriginalHeight(image.getHeight());
                im.setOriginalWidth(image.getWidth());
                im.setRenderedWidth(Math.round(ctmNew.getScaleX()));
                im.setRenderedHeight(Math.round(ctmNew.getScaleY()));
                images.add(im);

                if (!output.exists()) output.mkdirs();
                // TODO enable option to set the output format. right now it uses original which means: tiff needs extra dependency and tiff are HUGE!
                // String extension = "png";
                String extension = image.getSuffix();
                File out = new File(output, basename + "-" + currentPage + "-" + count + "." + extension);

                ImageIO.write(image.getImage(), extension, new FileOutputStream(out));
                count++;

            } else if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                showForm(form);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

    /**
     * Returns the images found after invoking {@link #process()}  method.
     */
    public List<Image> getImages() {
        return images;
    }

}
