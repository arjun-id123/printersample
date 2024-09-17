package com.identity.printersample

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.os.CancellationSignal
import android.print.PageRange
import android.print.PrintAttributes
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class PdfDocumentAdapter(private val context: Context, private val uri: Uri) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: android.os.Bundle?
    ) {
        // If the print operation is cancelled, stop here
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        // Inform the system that we are ready to print, no need for a preview update
        val info = PrintDocumentInfo.Builder("selected_pdf.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        // Notify that the layout is finished, and it's ready for printing
        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<PageRange?>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            // Open the input stream from the provided URI
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Write the PDF to the print destination (printer or file)
                FileOutputStream(destination?.fileDescriptor).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int

                    // Read from the input stream and write directly to the output stream
                    while (inputStream.read(buffer).also { bytesRead = it } != -1 &&
                        cancellationSignal?.isCanceled != true) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    // Check if print job was cancelled mid-process
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onWriteCancelled()
                    } else {
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    }
                }
            }
        } catch (e: IOException) {
            // In case of an error, log it and notify the system
            e.printStackTrace()
            callback?.onWriteFailed(e.toString())
        }
    }
}
