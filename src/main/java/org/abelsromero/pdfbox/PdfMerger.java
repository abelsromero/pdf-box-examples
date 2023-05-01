package org.abelsromero.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PdfMerger {

    public static final String SRC_DIR = "/src/main/resources/";
    public static final String TARGET_FILE = "output-%s.pdf";

    private final DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyMMdd-HHmmss");

    public static void main(String[] args) throws IOException {
        final List<File> sortedPDFs = Arrays.stream(new File(SRC_DIR)
            .listFiles((dir, name) -> name.endsWith("pdf")))
            .sorted()
            .collect(Collectors.toList());

        new PdfMerger()
            .processFiles(sortedPDFs);
    }

    private void processFiles(List<File> pdfs) throws IOException {
        PDDocument document = new PDDocument();

        for (File pdfFile : pdfs) {
            PDDocument pdDoc = PDDocument.load(pdfFile);
            for (PDPage page : pdDoc.getPages()) {
                document.addPage(page);
            }
        }

        File file = outputFile(TARGET_FILE);
        document.save(file);
    }

    private File outputFile(String template) {
        final String timestamp = LocalDateTime.now().format(dt);
        return new File(SRC_DIR, template.formatted(timestamp));
    }
}
