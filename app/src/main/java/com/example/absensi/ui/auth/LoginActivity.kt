package com.example.absensi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.absensi.MainActivity
import com.example.absensi.databinding.ActivityLoginBinding
import com.example.absensi.model.LoginRequest
import com.example.absensi.model.ApiResponse
import com.example.absensi.model.LoginResponse
import com.example.absensi.network.ApiClient
import com.example.absensi.util.PreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            startMainActivity()
            finish()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        binding.registerButton.setOnClickListener {
            // Navigate to RegisterActivity
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if (email.isEmpty()) {
            binding.emailInput.error = "Email is required"
            return false
        }

        if (password.isEmpty()) {
            binding.passwordInput.error = "Password is required"
            return false
        }

        return true
    }

    private fun performLogin() {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val loginRequest = LoginRequest(email, password)

        ApiClient.service.login(loginRequest).enqueue(object : Callback<ApiResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<LoginResponse>>,
                response: Response<ApiResponse<LoginResponse>>
            ) {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true

                if (response.isSuccessful) {
                    response.body()?.data?.let { loginResponse ->
                        // Save auth token and user info
                        preferenceManager.saveAuthToken(loginResponse.token)
                        preferenceManager.saveUser(loginResponse.user)

                        startMainActivity()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Invalid email or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginResponse>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
