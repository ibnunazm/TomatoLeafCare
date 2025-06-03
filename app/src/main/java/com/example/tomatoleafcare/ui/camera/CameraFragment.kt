package com.example.tomatoleafcare.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Debug
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
import java.io.*
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
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import kotlin.math.roundToInt


class CameraFragment : Fragment() {

    private var _binding: CameraFragmentBinding? = null

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
    private val credentials = "rahasia:tomat"
    private val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

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
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        binding.btnGaleri.setOnClickListener {
            openGallery()
        }

        binding.btnPindai.setOnClickListener {
            if (::imageUri.isInitialized) {
                if (apiAvailable) {
                    sendImageToApi(imageUri)
                } else {
                    runLocalModel(imageUri, requireContext())
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


    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Izin kamera diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runLocalModel(uri: Uri, context: Context) {
        val startTime = System.currentTimeMillis()
        val beforeMemory = Debug.getNativeHeapAllocatedSize() / 1024.0

        val model = Interpreter(loadModelFile("ResNet-50_tomato-leaf-disease.tflite", context))
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                input[0][y][x][0] = Color.red(pixel).toFloat()
                input[0][y][x][1] = Color.green(pixel).toFloat()
                input[0][y][x][2] = Color.blue(pixel).toFloat()
            }
        }

        val output = Array(1) { FloatArray(classLabels.size) }
        model.run(input, output)

        val probabilities = output[0]
        val predictedIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        val prediction = classLabels[predictedIndex]

        val endTime = System.currentTimeMillis()
        val afterMemory = Debug.getNativeHeapAllocatedSize() / 1024.0
        val duration = endTime - startTime
        val memoryUsed = (afterMemory - beforeMemory).roundToInt()

//        Toast.makeText(context, "Local: $prediction\nTime: $duration ms\nMemory: $memoryUsed KB", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "Local: $prediction", Toast.LENGTH_LONG).show()
        Log.d("MODEL_METRICS", "Local - Time: $duration ms, Memory: $memoryUsed KB")

        navigateToNoteFragment(prediction, uri)
    }

    private fun loadModelFile(modelName: String, context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun sendImageToApi(uri: Uri) {
        val startTime = System.currentTimeMillis()
        val beforeMemory = Debug.getNativeHeapAllocatedSize() / 1024.0

        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val imageBytes = inputStream.readBytes()
        val requestBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull(), 0)
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

                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    val afterMemory = Debug.getNativeHeapAllocatedSize() / 1024.0
                    val memoryUsed = (afterMemory - beforeMemory).roundToInt()

                    requireActivity().runOnUiThread {
//                        Toast.makeText(requireContext(), "Online: $prediction\nTime: $duration ms\nMemory: $memoryUsed KB", Toast.LENGTH_LONG).show()
                        Toast.makeText(requireContext(), "Online: $prediction", Toast.LENGTH_LONG).show()
                        Log.d("MODEL_METRICS", "Online - Time: $duration ms, Memory: $memoryUsed KB")
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


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateNetworkStatusRunnable)
        _binding = null
    }
}
