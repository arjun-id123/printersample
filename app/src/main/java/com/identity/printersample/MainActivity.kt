package com.identity.printersample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.identity.printersample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Declare the binding variable
    private lateinit var binding: ActivityMainBinding

    // Register a callback to handle the PDF file once selected
    private val getPdfFromStorage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Get the selected PDF's URI
            val uri: Uri? = result.data?.data
            uri?.let {
                printPDF(it) // Start the print process with the selected URI
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set onClickListener to open the file picker when the print button is clicked
        binding.printButton.setOnClickListener {
            openPdfPicker()
        }
    }

    // Method to open the PDF picker using the system's file picker
    private fun openPdfPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf" // Filter for PDF files only
        }
        getPdfFromStorage.launch(intent) // Launch the file picker
    }

    // Method to initiate the print process with the selected PDF URI
    private fun printPDF(pdfUri: Uri) {
        // Obtain the PrintManager service
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager

        // Create a custom PrintDocumentAdapter instance using the selected PDF's URI
        val printAdapter: PrintDocumentAdapter = PdfDocumentAdapter(this, pdfUri)

        // Define print job attributes like page size and color mode
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4) // Set paper size to A4
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR) // Set color mode to color
            .build()

        // Start the print job with a user-friendly name, the adapter, and the print attributes
        printManager.print("Print PDF Document", printAdapter, printAttributes)
    }
}
