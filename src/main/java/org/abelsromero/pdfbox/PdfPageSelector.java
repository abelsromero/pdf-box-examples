package org.abelsromero.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.abelsromero.pdfbox.utils.LocalUtils.getFileFromClassPath;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * Created by ABEL.SALGADOROMERO on 20/11/2017.
 * <p>
 * Allows to generate a PDF selecting some of the pages of another.
 */
public class PdfPageSelector {

    public static final String SRC_FILE = "BG2-TOB.pdf";

    public static void main(String[] args) throws IOException, URISyntaxException {
        final File source = getFileFromClassPath(SRC_FILE);

        final File result = new PdfPageSelector().processFile(source, 1, 2, 3, 3, 4, 25);

        assert result.exists() == true;
        assert PDDocument.load(result).getNumberOfPages() == 5;
    }

    /**
     * @pages pages to extract in that order. counting from 1
     */
    public File processFile(File sourceFile, int... pages) throws IOException {
        return processFile(sourceFile, sourceFile.getParentFile(), pages);
    }

    public File processFile(File sourceFile, File targetDirectory, int... pages) throws IOException {
        final PDDocument document = PDDocument.load(sourceFile);
        int pagesCount = document.getNumberOfPages();

        for (int pageIndex : pages) {
            if (pageIndex <= 0 || pageIndex > pagesCount)
                throw new IllegalArgumentException(String.format("Invalid page index: %s", pageIndex));
        }

        final PDDocument target = new PDDocument();
        Arrays.stream(pages)
            .map(i -> i - 1)
            .mapToObj(document::getPage)
            .forEach(target::addPage);

        System.out.println(target.getNumberOfPages());

        final File out = new File(targetDirectory, getBaseName(sourceFile.getName()) + "_out.pdf");
        target.save(out);
        return out;
    }

}
