package com.example.absensi.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.absensi.MainActivity
import com.example.absensi.R
import com.example.absensi.databinding.ActivitySplashBinding
import com.example.absensi.ui.auth.LoginActivity
import com.example.absensi.util.NetworkUtils
import com.example.absensi.util.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        setupAnimation()
        checkAuthAndNavigate()
    }

    private fun setupAnimation() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.logoImage.startAnimation(fadeIn)
        binding.appNameText.startAnimation(fadeIn)
    }

    private fun checkAuthAndNavigate() {
        lifecycleScope.launch {
            // Add minimum delay for splash screen
            delay(2000)

            // Check if user is logged in
            if (preferenceManager.isLoggedIn()) {
                // Verify token validity if needed
                if (NetworkUtils.isNetworkAvailable(this@SplashActivity)) {
                    validateToken()
                } else {
                    // Offline mode - proceed with cached data
                    startMainActivity()
                }
            } else {
                startLoginActivity()
            }
        }
    }

    private fun validateToken() {
        // TODO: Implement token validation with backend
        // For now, just proceed to main activity
        startMainActivity()
    }

    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
    }

    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.logoImage.clearAnimation()
        binding.appNameText.clearAnimation()
    }
}
