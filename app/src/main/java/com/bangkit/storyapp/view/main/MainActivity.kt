package com.bangkit.storyapp.view.main

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.paging.LoadingStateAdapter
import com.bangkit.storyapp.databinding.ActivityMainBinding
import com.bangkit.storyapp.helper.StoryAdapter
import com.bangkit.storyapp.view.ViewModelFactory
import com.bangkit.storyapp.view.createstory.CreateStoryActivity
import com.bangkit.storyapp.view.detail.DetailActivity
import com.bangkit.storyapp.view.maps.MapsActivity
import com.bangkit.storyapp.view.welcome.WelcomeActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.nav_toolbar))

        setupRecyclerView()
        observeViewModel()
        createStoryButton()
        setupRefreshButton()
        playAnimation()
    }

    private fun setupRecyclerView() {
        adapter = StoryAdapter { story ->
            Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_NAME, story.name)
                putExtra(DetailActivity.EXTRA_DESCRIPTION, story.description)
                putExtra(DetailActivity.EXTRA_PHOTO_URL, story.photoUrl)
            }.also { startActivity(it) }
        }
        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
    }

    private fun observeViewModel() {
        // Mengamati status sesi pengguna
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                navigateToWelcomeActivity()
            }
        }

        // Mengamati PagingData untuk daftar cerita
        viewModel.story.observe(this) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
        }

        // Mengamati status pemuatan PagingData
        adapter.addLoadStateListener { loadState ->
            // Tampilkan ProgressBar saat sedang memuat data
            binding.progressBar.isVisible = loadState.refresh is LoadState.Loading

            when {
                // Ketika terjadi error saat memuat data
                loadState.refresh is LoadState.Error -> {
                    val errorMessage = (loadState.refresh as LoadState.Error).error.localizedMessage
                    Snackbar.make(binding.root, errorMessage ?: getString(R.string.error_loading_data), Snackbar.LENGTH_LONG).show()

                    binding.ivFailed.isVisible = true  // Pastikan ini dijalankan
                    Glide.with(this)
                        .asGif()
                        .load(R.drawable.failed) // Pastikan ini adalah file .gif Anda
                        .into(binding.ivFailed)
                    binding.rvStory.isVisible = false
                    binding.tvErrorMessage.isVisible = true
                }

                // Ketika data kosong setelah loading selesai
                loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0 -> {
                    binding.ivFailed.isVisible = true
                    Glide.with(this)
                        .asGif()
                        .load(R.drawable.failed) // Pastikan ini adalah file .gif Anda
                        .into(binding.ivFailed)
                    binding.rvStory.isVisible = false
                    binding.tvErrorMessage.isVisible = true
                }

                // Ketika data berhasil dimuat
                else -> {
                    binding.ivFailed.isVisible = false
                    binding.rvStory.isVisible = true
                    binding.tvErrorMessage.isVisible = false
                }
            }
        }
    }

    private fun navigateToWelcomeActivity() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                viewModel.logout()
                true
            }
            R.id.menu_map -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private val createStoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh data dan scroll ke atas setelah story berhasil ditambahkan
            adapter.refresh()
            adapter.addOnPagesUpdatedListener {
                binding.rvStory.smoothScrollToPosition(0) // Scroll ke posisi teratas
            }
        }
    }

    private fun createStoryButton() {
        binding.createStory.setOnClickListener {
            val intent = Intent(this, CreateStoryActivity::class.java)
            createStoryLauncher.launch(intent) // Ganti startActivity dengan createStoryLauncher
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageViewDicoding, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }

    private fun setupRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            adapter.refresh()
            binding.ivFailed.isVisible = false
            binding.tvErrorMessage.isVisible = false
            binding.rvStory.isVisible = true

            // Listener untuk memantau kapan data selesai di-refresh
            adapter.addOnPagesUpdatedListener {
                binding.rvStory.smoothScrollToPosition(0) // Scroll ke posisi teratas
            }
        }
    }


}