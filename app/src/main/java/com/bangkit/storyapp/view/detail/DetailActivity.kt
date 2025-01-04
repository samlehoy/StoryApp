package com.bangkit.storyapp.view.detail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.response.ListStoryItem
import com.bangkit.storyapp.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari Intent
        val story = ListStoryItem(
            name = intent.getStringExtra(EXTRA_NAME),
            description = intent.getStringExtra(EXTRA_DESCRIPTION),
            photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL),
            id = "", // Sesuaikan dengan data lainnya jika ada
            createdAt = null,
            lat = null,
            lon = null
        )

        // Set data ke ViewModel
        viewModel.setStory(story)

        // Observe LiveData untuk menampilkan data di UI
        viewModel.story.observe(this) { storyData ->
            binding.apply {
                tvDetailName.text = storyData.name
                tvDetailDescription.text = storyData.description
                Glide.with(this@DetailActivity)
                    .load(storyData.photoUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivDetailPhoto)
            }
        }

        // Tombol kembali
        binding.buttonBack.setOnClickListener { finish() }

        // Tambahkan animasi
        playAnimation()
    }

    private fun playAnimation() {
        // Animasi untuk gambar
        val imageAnimator = ObjectAnimator.ofFloat(binding.ivDetailPhoto, View.ALPHA, 0f, 1f).apply {
            duration = 500
        }

        // Animasi untuk judul
        val titleAnimator = ObjectAnimator.ofFloat(binding.tvDetailName, View.TRANSLATION_X, -50f, 0f).apply {
            duration = 500
        }

        // Animasi untuk deskripsi
        val descriptionAnimator = ObjectAnimator.ofFloat(binding.tvDetailDescription, View.TRANSLATION_Y, 50f, 0f).apply {
            duration = 500
        }

        // Gabungkan animasi
        AnimatorSet().apply {
            playTogether(imageAnimator, titleAnimator, descriptionAnimator)
            start()
        }
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_PHOTO_URL = "extra_photo_url"
    }
}