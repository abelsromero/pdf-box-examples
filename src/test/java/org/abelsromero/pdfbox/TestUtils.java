package org.abelsromero.pdfbox;

import org.junit.platform.commons.util.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

class TestUtils {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HHmmss-SSS");

    private static final String OUTPUT_DIR = "build";

    public static File createTestDirectory() {
        return createTestDirectory(null);
    }

    public static File createTestDirectory(String root) {
        final String formattedTimestamp = SIMPLE_DATE_FORMAT.format(new Date());
        // def dir = new File("$OUTPUT_DIR/output-${root ? "$root-" : ''}$timestamp")
        var dir = new File(String.format("%s/output-%s%s", OUTPUT_DIR, formatRoot(root), formattedTimestamp));

        dir.mkdirs();
        return dir;
    }

    private static String formatRoot(String root) {
        return StringUtils.isBlank(root) ? "" : root + "-";
    }
}
