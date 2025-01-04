package com.bangkit.storyapp.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.bangkit.storyapp.R
import com.bangkit.storyapp.databinding.ActivitySignupBinding
import com.bangkit.storyapp.view.ViewModelFactory

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    private val signupViewModel by viewModels<SignupViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        setupObservers()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val nameTextView =
            ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(100)
        val edRegisterName =
            ObjectAnimator.ofFloat(binding.edRegisterName, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val edRegisterEmail =
            ObjectAnimator.ofFloat(binding.edRegisterEmail, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val edRegisterPassword =
            ObjectAnimator.ofFloat(binding.edRegisterPassword, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                edRegisterName,
                emailTextView,
                edRegisterEmail,
                passwordTextView,
                edRegisterPassword,
                signup
            )
            startDelay = 100
        }.start()
    }

    private fun setupAction() {
        val inputs = listOf(binding.edRegisterName, binding.edRegisterEmail, binding.edRegisterPassword)

        // Tambahkan listener untuk setiap input
        inputs.forEach { input ->
            input.addTextChangedListener {
                binding.signupButton.updateButtonState(inputs)
            }
        }

        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (binding.signupButton.isEnabled) {
                binding.signupButton.isEnabled = false
                signupViewModel.register(name, email, password)
            } else {
                showAlertDialog(
                    getString(R.string.error),
                    getString(R.string.fill_field_warning)
                )
            }
        }
    }

    private fun setupObservers() {
        signupViewModel.apply {
            isLoading.observe(this@SignupActivity) { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.signupButton.isEnabled = !isLoading
            }

            registerStatus.observe(this@SignupActivity) { user ->
                user?.let {
                    showAlertDialog(getString(R.string.register_success), getString(R.string.account_success)) {
                        finishAfterTransition()
                    }
                }
            }

            isRegister.observe(this@SignupActivity) { isRegister ->
                if (!isRegister) {
                    showAlertDialog(
                        getString(R.string.error),
                        getString(R.string.email_taken)
                    )
                }
            }

            isNetworkError.observe(this@SignupActivity) { isNetworkError ->
                if (isNetworkError) {
                    showAlertDialog(
                        getString(R.string.error),
                        getString(R.string.error_register)
                    )
                }
            }
        }
    }


    private fun showAlertDialog(title: String, message: String, onPositive: (() -> Unit)? = null) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(getString(R.string.ok)) { _, _ -> onPositive?.invoke() }
            create()
            show()
        }
    }
}
