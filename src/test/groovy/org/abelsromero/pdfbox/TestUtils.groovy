package org.abelsromero.pdfbox

class TestUtils {

    private static final String OUTPUT_DIR = "out"

    /**
     * Generates a unique directory in the build area.
     */
    static File createTestDirectory(String root = null) {
        String timestamp = new Date().format('HHmmss-SSS')
        def dir = new File("$OUTPUT_DIR/output-${root ? "$root-" : ''}$timestamp")
        dir.mkdirs()
        return dir
    }

}
