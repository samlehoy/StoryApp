package com.bangkit.storyapp.view.createstory

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bangkit.storyapp.R
import com.bangkit.storyapp.databinding.ActivityCreateStoryBinding
import com.bangkit.storyapp.utils.getImageUri
import com.bangkit.storyapp.utils.reduceFileImage
import com.bangkit.storyapp.utils.uriToFile
import com.bangkit.storyapp.view.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateStoryBinding
    private var currentImageUri: Uri? = null
    private val viewModel by viewModels<CreateStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        with(binding){
            galleryButton.setOnClickListener {
                startGallery() }
            cameraButton.setOnClickListener {
                startCamera() }
            buttonAdd.setOnClickListener {
                uploadImage()
            }
        }

        // Opsi tambah lokasi
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.checkBoxLoc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getCurrentLocation()
            } else {
                latitude = null
                longitude = null
            }
        }

        // Animasi elemen di halaman
        playAnimation()
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Timber.tag("Photo Picker").d("No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Timber.tag("Image URI").d("showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            val description = binding.edAddDescription.text.toString()

            if (description.isBlank()) {
                showToast(getString(R.string.empty_description_warning))
                return
            }

            showLoading(true)

            lifecycleScope.launch {
                val result = viewModel.uploadImage(
                    imageFile = imageFile,
                    description = description,
                    latitude = latitude,
                    longitude = longitude
                )
                showLoading(false)

                if (result.isSuccess) {
                    showToast(result.getOrDefault("Upload successful!"))
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showToast(result.exceptionOrNull()?.localizedMessage ?: "Upload failed!")
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun playAnimation() {
        val galleryButtonAnim = ObjectAnimator.ofFloat(binding.galleryButton, View.ALPHA, 0f, 1f).setDuration(500)
        val cameraButtonAnim = ObjectAnimator.ofFloat(binding.cameraButton, View.ALPHA, 0f, 1f).setDuration(500)
        val uploadButtonAnim = ObjectAnimator.ofFloat(binding.buttonAdd, View.ALPHA, 0f, 1f).setDuration(500)
        val descriptionAnim = ObjectAnimator.ofFloat(binding.edAddDescription, View.TRANSLATION_Y, 50f, 0f).apply {
            duration = 500
        }
        val previewImageAnim = ObjectAnimator.ofFloat(binding.previewImageView, View.SCALE_X, 0.8f, 1f).apply {
            duration = 500
        }

        val checkBoxSection = ObjectAnimator.ofFloat(binding.locationCheck, View.SCALE_X, 0.8f, 1f).apply {
            duration = 500
        }

        // Gabungkan animasi dalam AnimatorSet
        AnimatorSet().apply {
            playTogether(galleryButtonAnim, cameraButtonAnim, uploadButtonAnim, descriptionAnim, previewImageAnim, checkBoxSection)
            startDelay = 200 // Memberikan jeda sebelum animasi dimulai
            start()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
            } else {
                showToast("Unable to get current location")
            }
        }.addOnFailureListener {
            showToast("Failed to get location")
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }

}