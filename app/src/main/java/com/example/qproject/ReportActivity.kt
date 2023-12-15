package com.example.qproject

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.itextpdf.text.pdf.ColumnText

import com.example.qproject.databinding.ReportBinding
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import java.util.*

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
//import com.itextpdf.kernel.pdf.PdfWriter

import java.io.*


import java.text.SimpleDateFormat


//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ReportBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val storage_code = 1001
    // Declare PdfWriter variable
    private lateinit var pdf_writer: PdfWriter
    private lateinit var uploadedImageUri: Uri

    // Get Patient data from Firebase
    private lateinit var patientFirstName: String
    private lateinit var patientLastName: String
    private lateinit var patientAge:String
    private lateinit var diagnosisReport:String
    private var share: Boolean = false
    private var yes_no: String = ""

    // on below line we are creating a
    // constant code for runtime permissions.
    var PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ReportBinding.inflate(layoutInflater)

        setContentView(binding.root)
        diagnosisReport = intent.getStringExtra("description").toString()

        yes_no = intent.getStringExtra("yes_or_no").toString()
        val imageUriString = intent.getStringExtra("imageUri")


        var imageUri: Uri? = if (imageUriString != null) Uri.parse(imageUriString) else null

        val byteArray = intent.getByteArrayExtra("imageBitmap")
        val bitmap: Bitmap? = if (byteArray != null) BitmapFactory.decodeByteArray(
            byteArray,
            0,
            byteArray.size
        ) else null

        if (imageUri != null) {
            // Use the imageUri to display the image
            binding.ivReportImage.setImageURI(imageUri)

        } else if (bitmap != null) {
            binding.ivReportImage.setImageBitmap(bitmap)

            // Use the bitmap to display the image
        }

        binding.tvReport.setText(yes_no+". \n"+diagnosisReport)

        FirebaseApp.initializeApp(this)
        val database = FirebaseDatabase.getInstance().getReference("Users")
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            // Get a reference to the "firstName" field for the user
            val firstNameRef = database.child(userId).child("firstName")
            val lastNameRef = database.child(userId).child("lastName")
            val birthdateRef = database.child(userId).child("birthdate")
            // Add a ValueEventListener to the "firstName" field reference

            firstNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get the value of the "firstName" field
                    val firstName = snapshot.getValue(String::class.java)

                    // Check if the value is not null
                    //if (firstName != null) {
                    // Update the UI to display the user's first name
                    binding.tvPatientName.text = "Patient name: $firstName"
                    patientFirstName = firstName.toString()
                    //}
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error case appropriately
                    Log.w(MainActivity.TAG, "Failed to read value.", error.toException())
                }
            })

            lastNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get the value of the "lastName" field
                    val lastName = snapshot.getValue(String::class.java)
                    binding.tvPatientlastName.text = "$lastName"
                    patientLastName = lastName.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error case appropriately
                    Log.w(MainActivity.TAG, "Failed to read value.", error.toException())
                }
            })

            birthdateRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get the value of the "firstName" field
                    val birthdate = snapshot.getValue(String::class.java)
                    if (birthdate != null) {
                        val age = calculateAge(birthdate)
                        binding.tvPatientAge.text = "Patient age: $age years."
                        patientAge = age.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error case appropriately
                    Log.w(MainActivity.TAG, "Failed to read value.", error.toException())
                }
            })

        }
        uploadedImageUri = getImageUri(binding.ivReportImage)
        binding.btnSavePdf.setOnClickListener {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Medical Report")
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)

            share = false
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_TITLE, "MedicalReport.pdf")
            startActivityForResult(intent, storage_code)
        }
        binding.btnShare.setOnClickListener {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Medical Report")
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_TITLE, "MedicalReport.pdf")
            share = true
            startActivityForResult(intent, storage_code)


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == storage_code && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                //val imageUri = data.getParcelableExtra<Uri>("imageUri") // Retrieve the image URI
                try {
                    val outputStream = contentResolver.openOutputStream(uri)
                    // Initialize the PdfWriter with the outputStream
                    val document = Document()
                    //pdf_writer =
                    PdfWriter.getInstance(document, outputStream)
                    document.open()
                    document.addAuthor("Quantumedics")
                    addDataIntoPdf(document)
                    document.close()
                    Toast.makeText(this, "Medical Report Created", Toast.LENGTH_SHORT).show()

                    if (share){
                        sharePdfDocument(uri)  //share the pdf document
                    }

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sharePdfDocument(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/pdf"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share Medical Report"))

    }

    fun addDataIntoPdf(document: Document) {
        val paraGraph = Paragraph()

        // Title
        val headingFont = Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD)
        val title = Paragraph("Quantumedics Report", headingFont)
        title.font.color = BaseColor(20, 227, 195)
        title.alignment = Element.ALIGN_LEFT
        title.spacingAfter = 10F
        paraGraph.add(title)
        addEmptyLines(paraGraph, 1)
        addEmptyLines(paraGraph, 1)
        // Add the LOGO from drawable
        val drawableImage = R.drawable.logo_left_slogan  // Replace with your drawable image ID
        val tempFile = copyImageToTempFile(this, drawableImage)
        val image2 = Image.getInstance(tempFile.absolutePath)
        image2.setAbsolutePosition(360F, 755F)
        image2.scaleToFit(200F, 200F)
        document.add(image2)

        // Add the image to the PDF
        val image = Image.getInstance(uploadedImageUri.toString())
        //image.alignment = Element.ALIGN_RIGHT
        //image.spacingBefore = 100F
        // 410,585
        image.setAbsolutePosition(200F, 505F)  // Set the position of the image
        image.scaleToFit(200F, 200F)  // Set the width and height of the image
        image.spacingAfter = 10f
        document.add(image)

        var pData = Paragraph(
            "")
        pData.alignment = Element.ALIGN_JUSTIFIED  // Set the alignment to justified
        paraGraph.add(pData)

        var text1 = "Patient name:"
        var text2 = " $patientFirstName $patientLastName."

        var coloredText = Chunk(text1)
        coloredText.font = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
        //coloredText.setUnderline(0.1f, -2f)
        //coloredText.setBackground(BaseColor.YELLOW)
        //coloredText.font.color = BaseColor.RED
        pData.add(coloredText)
        pData.add(text2)
        pData.spacingAfter = 10F

        addEmptyLines(paraGraph, 1)

        pData = Paragraph(
            "")
        pData.alignment = Element.ALIGN_JUSTIFIED  // Set the alignment to justified
        pData.spacingAfter = 150F
        paraGraph.add(pData)
        text1 = "Patient age: "
        text2 = " $patientAge years."

        coloredText = Chunk(text1)
        coloredText.font = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
        pData.add(coloredText)
        pData.add(text2)
        addEmptyLines(paraGraph, 1)
        addEmptyLines(paraGraph, 1)
        // Paragraphs
        /*
        val youtubeParagraph = Paragraph("Youtube")
        youtubeParagraph.alignment = Element.ALIGN_LEFT  // Set the alignment to left
        youtubeParagraph.spacingBefore = 100F
        paraGraph.add(youtubeParagraph)
        addEmptyLines(paraGraph, 1)*/



        //val contentParagraph = Paragraph("Diagnoses: $diagnosisReport")
        val contentParagraph = Paragraph()
        val boldText = Chunk("Diagnosis: $yes_no. ")
        boldText.font = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)

        val normalText = Chunk(diagnosisReport)
        normalText.font = Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL)

        contentParagraph.add(boldText)
        contentParagraph.add(normalText)

        contentParagraph.spacingBefore = 70F
        contentParagraph.alignment = Element.ALIGN_LEFT  // Set the alignment to justified
        contentParagraph.multipliedLeading = 1.5f  // Set the line spacing multiplier (adjust as needed)

        addEmptyLines(paraGraph, 1)
        addEmptyLines(paraGraph, 1)
/*
        val table = PdfPTable(1)
        table.totalWidth = 150F
        table.widthPercentage = 70F
        table.horizontalAlignment = Element.ALIGN_LEFT
        table.defaultCell.borderWidth = 0F  // Set border width to 0
        table.defaultCell.borderColor = BaseColor.WHITE  // Set border color to transparent

        table.addCell(contentParagraph)


 */
        /*val columnWidth = 300F

        // Create a column with the specified width
        val columnText = ColumnText(document.pdf_writer)
        columnText.addElement(contentParagraph)
        columnText.setSimpleColumn(36F, 36F, 36F + columnWidth, 806F) // Adjust the coordinates and height as needed

        // Render the paragraph with the limited width
        columnText.go()*/



/*
        val pfont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
        val pName = Paragraph("Patient name: ",pfont)
        pName.alignment = Element.ALIGN_LEFT
        paraGraph.add(pName)
        val manname =  ""
        pName.add(manname)*/


        addEmptyLines(paraGraph, 1)
        paraGraph.add(contentParagraph) //table
        document.add(paraGraph)

    }

    fun addEmptyLines(paragraph: Paragraph, lineCount: Int) {
        for (i in 0 until lineCount) {
            paragraph.add(Paragraph(""))
        }
    }

    // -------------------- For Image -> PDF
    private fun getImageUri(imageView: ImageView): Uri {
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(imagesDir, "image.jpg")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Uri.fromFile(file)
        /*
            val drawable = imageView.drawable
            val bitmap: Bitmap = if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            val uri = getImageUriFromBitmap(bitmap)
            return uri

         */
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(imagesDir, "image.jpg")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Uri.fromFile(file)
    }

    private fun copyImageToTempFile(context: Context, drawableId: Int): File {
        val inputStream: InputStream = context.resources.openRawResource(drawableId)
        val tempDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val tempFile = File(tempDir, "image_temp.jpg")
        val outputStream = FileOutputStream(tempFile)
        val buffer = ByteArray(4 * 1024) // 4KB buffer size
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
        return tempFile
    }

    fun calculateAge(dateString: String): Int {
        // Define the date format for parsing
        val format = SimpleDateFormat("d/M/yyyy", Locale.US)

        // Parse the input string into a Date object
        val birthDate = format.parse(dateString)

        // Get the current date
        val currentDate = Date()

        // Calculate the difference between the current date and the birth date
        val diff = currentDate.time - birthDate.time

        // Calculate the age in years
        val ageInMillis = diff
        val ageInYears = ageInMillis / (1000L * 60 * 60 * 24 * 365)

        return ageInYears.toInt()
    }

    }









