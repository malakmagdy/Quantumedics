package com.example.qproject

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.qproject.databinding.UploadBreastImageBinding
import com.example.qproject.ml.QuantumCTbreast
import com.example.qproject.ml.QuantumMRI
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit


class UploadBreastActivity : AppCompatActivity() {
    private lateinit var binding: UploadBreastImageBinding
    private var selectedImageUri: Uri? = null
    private var capturedImageBitmap: Bitmap? = null

    private var image_bitmap: Bitmap? = null

    private val resultDescription = MutableLiveData<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UploadBreastImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Image upload function
        binding.btnUpload.setOnClickListener {
            var intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent,100)
        }

        //Open Camera function
        binding.btnCamera.setOnClickListener {
            // handling Camera permissions
            checkandGetpermissions()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 200)
        }


        var labels = application.assets.open("brain_mri_labels.txt").bufferedReader().readLines()

        var imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(28, 28, ResizeOp.ResizeMethod.BILINEAR))
            //.add(NormalizeOp(0.0f,255.0f))
            .add(TransformToGrayscaleOp())
            .build()

        binding.btnGetResults.setOnClickListener {

            var diagnosisResult: String? = null


            // Loading screen appears
            binding.llMainScreen.visibility = View.GONE
            binding.llLoadingScreen.visibility = View.VISIBLE

            var resultText: String? = null

            var tensorImage = TensorImage(DataType.FLOAT32) // ----------------------------
            tensorImage.load(image_bitmap)

            tensorImage = imageProcessor.process(tensorImage)
            val model = QuantumCTbreast.newInstance(this)

            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 28, 28, 1), DataType.FLOAT32)
            inputFeature0.loadBuffer(tensorImage.buffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray


            // Get the class probabilities
            val probabilities = getMax(outputFeature0)

            // Display the top two class probabilities
            val firstClass = labels[probabilities[0].first]
            val firstConfidence = probabilities[0].second * 100
            val secondClass = labels[probabilities[1].first]
            val secondConfidence = probabilities[1].second * 100
            resultText = "${firstClass}: ${
                String.format(
                    "%.2f",
                    firstConfidence
                )
            }% | ${secondClass}: ${String.format("%.2f", secondConfidence)}%"


            /*val probabilities = mutableListOf<Float>()
            for (probability in outputFeature0) {
                probabilities.add(probability)
            }
            for (i in labels.indices) {

                if (i < labels.size) {
                    val className = labels[i]
                    val probability = probabilities[i]
                    val probabilityPercentage = (probability * 100).toInt()
                    resultText = "$className: $probabilityPercentage%"
                }

                binding.tvResult.append(resultText)

                if (i != labels.size - 1) {
                    binding.tvResult.append("\n")
                }
            }
 */
            model.close()




            val i = Intent(this, ReportActivity::class.java)
            if (selectedImageUri != null) {
                i.putExtra("imageUri", selectedImageUri.toString())
            } else if (capturedImageBitmap != null) {
                val stream = ByteArrayOutputStream()
                capturedImageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()
                i.putExtra("imageBitmap", byteArray)
            }

            var firstClassDescription: String? = null
            lifecycleScope.launch {

                firstClassDescription = fetchClassDescription(firstClass)
                Log.d("API_DEBUG", "First class description: $firstClassDescription")
                resultDescription.value = firstClassDescription
                /*$resultText \n Diagnosis: $firstClass\n" +
                        "\n" +*/


                //setContentView(binding.root)

                if (firstClass == "Yes"){
                    i.putExtra("yes_or_no", "Breast Cancer Detected")

                } else if (firstClass == "No"){
                    i.putExtra("yes_or_no", "Breast Cancer Free")

                }
                // Loading screen appears
                binding.llMainScreen.visibility = View.VISIBLE
                binding.llLoadingScreen.visibility = View.GONE


                diagnosisResult = "Result: $resultText. \n Diagnosis: $firstClassDescription "
                //binding.tvResult.setText(diagnosisResult)
                //make the api in the report screen with respect to which brain,chest,...

                i.putExtra("description", resultDescription.value)
                startActivity(i)

            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // request code = 100 means we request to upload image
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            if (selectedImageUri != null) {
                binding.btnGetResults.visibility = View.VISIBLE   // Image was uploaded, show the "Submit" button
                binding.ivBreast.setImageURI(selectedImageUri)   // Load the selected image into the ImageView

                val imageStream: InputStream? = contentResolver.openInputStream(selectedImageUri!!)
                image_bitmap = BitmapFactory.decodeStream(imageStream)
            }
        }

        // Request code = 200 means we request to  open camera
        else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            capturedImageBitmap = data?.extras?.get("data") as Bitmap
            if (capturedImageBitmap != null){
                binding.ivBreast.setImageBitmap(capturedImageBitmap)     //change image to captured image
                binding.btnGetResults.visibility = View.VISIBLE   //make the results button visible

                image_bitmap = capturedImageBitmap
            }
        }
    }

    fun checkandGetpermissions() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 200)
        }
        // Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
    }


    fun getMax(arr: FloatArray): List<Pair<Int, Float>> {
        val results = mutableListOf<Pair<Int, Float>>()

        // Loop through the array to get the probabilities for each class
        for (i in arr.indices) {
            results.add(Pair(i, arr[i]))
        }

        // Sort the results by probability in descending order
        results.sortByDescending { it.second }

        return results
    }


    fun centerCrop(bitmap: Bitmap): Bitmap {
        val dimension = Math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - dimension) / 2
        val y = (bitmap.height - dimension) / 2
        return Bitmap.createBitmap(bitmap, x, y, dimension, dimension)
    }
    private suspend fun fetchClassDescription(predictedClass: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Increase connection timeout to 30 seconds
                .readTimeout(30, TimeUnit.SECONDS) // Increase read timeout to 30 seconds
                .build()

            val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val gson = Gson()
            var content = ""
            if (predictedClass == "Yes"){
                content = "what the patient should do if he/she diagnosed as Breast Cancer? Write this in 5 points each point no more than 3 lines"
            } else if (predictedClass == "No"){
                content = "what the patient should do to protect themselves from Breast Cancer? Write this in 5 points each point no more than 3 lines"
            }
            val requestBody = gson.toJson(
                mapOf(
                    "model" to "gpt-3.5-turbo",
                    "messages" to listOf(
                        mapOf(
                            "role" to "user",
                            "content" to content
                            //"content" to "what the patient should do if he/she diagnosed as Breast cancer? $predictedClass. write this in 5 points each point no more than 3 lines. If no write how to protect themselves"
                        )
                    )
                )
            ).toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader(
                    "Authorization",
                    "Bearer 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"
                )
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                Log.d("API_DEBUG", "Response: $response")
                val responseBody = response.body?.string()
                Log.d("API_DEBUG", "ResponseBody: $responseBody")
                val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                val description = jsonResponse.getAsJsonArray("choices")
                    .get(0)
                    .asJsonObject
                    .getAsJsonObject("message")
                    .get("content")
                    .asString
                    .trim()

                description
            } catch (e: IOException) {
                e.printStackTrace()
                "Failed to fetch description."
            }
        }
    }


}

