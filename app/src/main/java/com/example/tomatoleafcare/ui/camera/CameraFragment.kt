package com.example.tomatoleafcare.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.tomatoleafcare.DiseaseDetailFragment
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.repository.DiseaseMatcher
import com.example.tomatoleafcare.model.History
import com.example.tomatoleafcare.databinding.CameraFragmentBinding
import com.example.tomatoleafcare.helper.HistoryDatabaseHelper
import okhttp3.*
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import android.util.Base64
import android.util.Log

class CameraFragment : Fragment() {

    private var _binding: CameraFragmentBinding? = null
//    private val binding get() = _binding!!

    private val binding get() = _binding ?: throw IllegalStateException("Binding belum diinisialisasi")


    private lateinit var imageUri: Uri
    private lateinit var photoFile: File

    private val classLabels = arrayOf(
        "bacterial_spot",
        "healthy",
        "late_blight",
        "leaf_curl_virus",
        "leaf_mold",
        "mosaic_virus",
        "septoria_leaf_spot"
    )

    private var apiAvailable = false

    val credentials = "rahasia:tomat"
    val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val tempFile = File.createTempFile("gallery_", ".jpg", requireContext().cacheDir)
                val outputStream = FileOutputStream(tempFile)

                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                imageUri = Uri.fromFile(tempFile)
                binding.imageView.setImageURI(imageUri)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Gagal memuat gambar dari galeri", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.imageView.setImageURI(imageUri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CameraFragmentBinding.inflate(inflater, container, false)

        binding.btnKamera.setOnClickListener {
            openCamera()
        }

        binding.btnGaleri.setOnClickListener {
            openGallery()
        }

        binding.btnPindai.setOnClickListener {
            if (::imageUri.isInitialized) {
                if (apiAvailable) {
                    sendImageToApi(imageUri)
                } else {
                    runLocalModel(imageUri)
                }
            } else {
                Toast.makeText(requireContext(), "Silakan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
        handler.post(updateNetworkStatusRunnable)
        return binding.root
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        try {
            photoFile = File.createTempFile("photo_", ".jpg", requireContext().cacheDir)
            imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
            cameraLauncher.launch(imageUri)
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Gagal membuat file foto", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun sendImageToApi(uri: Uri) {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return

        val imageBytes = inputStream.readBytes()
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
        val body = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

        val requestBodyMultipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(body)
            .build()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://robotika.upnvj.ac.id:8000/tomatoleafcare/classify")
            .post(requestBodyMultipart)
            .addHeader("Authorization", basicAuth)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Respons tidak valid dari server", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val jsonObject = JSONObject(responseBody)
                    val predictedClassObj = jsonObject.getJSONObject("predictedClass")
                    val prediction = predictedClassObj.getString("class")

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Online: $prediction", Toast.LENGTH_LONG).show()
                        navigateToNoteFragment(prediction, uri)
                    }

                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Gagal parsing data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun runLocalModel(uri: Uri) {
        try {
            val bitmap = getBitmapFromUri(uri)
            val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val input = convertBitmapToByteBuffer(resized)

            val modelBuffer = loadModelFile("ResNet-50_tomato-leaf-disease.tflite")
            val interpreter = Interpreter(modelBuffer)

            val output = Array(1) { FloatArray(classLabels.size) }
            interpreter.run(input, output)

            val resultIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            val confidence = output[0][resultIndex]
            val className = classLabels.getOrNull(resultIndex) ?: "Tidak diketahui"

            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Lokal: $className", Toast.LENGTH_LONG).show()
                navigateToNoteFragment(className, uri)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Gagal menjalankan model lokal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = 224
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val safeBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
        safeBitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in intValues) {
            val r = ((pixel shr 16) and 0xFF).toFloat() / 255.0f
            val g = ((pixel shr 8) and 0xFF).toFloat() / 255.0f
            val b = (pixel and 0xFF).toFloat() / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    private fun loadModelFile(modelName: String): ByteBuffer {
        val fileDescriptor = requireContext().assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun navigateToNoteFragment(className: String, imageUri: Uri) {

        val matchedDisease = DiseaseMatcher.matchDisease(className)

        val dbHelper = HistoryDatabaseHelper(requireContext())
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = formatter.format(Date())

        val historyItem = History(
            diseaseName = matchedDisease?.name ?: "Unknown",
            date = now,
            imagePath = imageUri.toString()
        )
        dbHelper.insertHistory(historyItem)

        val bundle = Bundle().apply {
            putString("disease_name", matchedDisease?.name)
            putString("disease_description", matchedDisease?.description)
            putString("disease_symptoms", matchedDisease?.symptoms)
            putString("disease_causes", matchedDisease?.cause)
            putString("disease_impact", matchedDisease?.impact)
            putString("disease_solution", matchedDisease?.solution)
            putString("image_uri", imageUri.toString())
        }

        val diseaseDetailFragment = DiseaseDetailFragment().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, diseaseDetailFragment)
            .addToBackStack(null)
            .commit()
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 1000

    private val updateNetworkStatusRunnable = object : Runnable {
        override fun run() {
            checkApiAvailability()
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun checkApiAvailability() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://robotika.upnvj.ac.id:8000/tomatoleafcare/check")
            .get()
            .addHeader("Authorization", basicAuth)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                apiAvailable = false
                updateApiStatus()
                Log.e("API_RESPONSE", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                apiAvailable = response.isSuccessful
                updateApiStatus()
            }
        })
    }

    private fun updateApiStatus() {
        if (_binding == null || !isAdded) return

        requireActivity().runOnUiThread {
            _binding?.let { binding ->
                if (apiAvailable) {
                    binding.textStatus.text = getString(R.string.online)
                    binding.textStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    binding.ledStatus.setBackgroundResource(R.drawable.bg_led_online)
                } else {
                    binding.textStatus.text = getString(R.string.offline)
                    binding.textStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                    binding.ledStatus.setBackgroundResource(R.drawable.bg_led_offline)
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateNetworkStatusRunnable)
        _binding = null
    }
}
