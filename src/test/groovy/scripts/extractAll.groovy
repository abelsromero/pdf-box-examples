package scripts

import groovy.io.FileType
import org.abelsromero.pdfbox.api.PdfImagesHelper

import static org.apache.commons.io.FilenameUtils.getBaseName

/**
 * Created by asalgadr on 22/11/2016.
 */

File root = new File('C:\\home\\bin\\.babun\\cygwin\\home\\asalgadr\\images\\caixa\\target\\ok')

root.eachFile(FileType.FILES) {
    if (it.name.endsWith('.pdf')) {
        def helper = PdfImagesHelper.Builder.loadPdf(it)
        helper.writeImagesToDir(root, getBaseName(it.name))
    }
}
