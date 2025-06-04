package com.example.absensi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.absensi.MainActivity
import com.example.absensi.R
import com.example.absensi.databinding.ActivityRegisterBinding
import com.example.absensi.model.ApiResponse
import com.example.absensi.model.RegisterRequest
import com.example.absensi.model.LoginResponse
import com.example.absensi.network.ApiClient
import com.example.absensi.util.NetworkUtils
import com.example.absensi.util.PreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        setupListeners()
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        binding.loginButton.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.nameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()
        val employeeId = binding.employeeIdInput.text.toString()

        if (name.isEmpty()) {
            binding.nameInput.error = getString(R.string.error_field_required)
            return false
        }

        if (email.isEmpty()) {
            binding.emailInput.error = getString(R.string.error_field_required)
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = getString(R.string.error_invalid_email)
            return false
        }

        if (password.isEmpty()) {
            binding.passwordInput.error = getString(R.string.error_field_required)
            return false
        }

        if (password.length < 6) {
            binding.passwordInput.error = getString(R.string.error_password_short)
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInput.error = getString(R.string.error_field_required)
            return false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordInput.error = getString(R.string.error_passwords_dont_match)
            return false
        }

        if (employeeId.isEmpty()) {
            binding.employeeIdInput.error = getString(R.string.error_field_required)
            return false
        }

        return true
    }

    private fun performRegistration() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.registerButton.isEnabled = false

        val request = RegisterRequest(
            name = binding.nameInput.text.toString(),
            email = binding.emailInput.text.toString(),
            password = binding.passwordInput.text.toString(),
            phone = binding.phoneInput.text.toString(),
            employeeId = binding.employeeIdInput.text.toString()
        )

        ApiClient.service.register(request).enqueue(object : Callback<ApiResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<LoginResponse>>,
                response: Response<ApiResponse<LoginResponse>>
            ) {
                binding.progressBar.visibility = View.GONE
                binding.registerButton.isEnabled = true

                if (response.isSuccessful) {
                    response.body()?.data?.let { loginResponse ->
                        // Save auth token and user info
                        preferenceManager.saveAuthToken(loginResponse.token)
                        preferenceManager.saveUser(loginResponse.user)

                        // Navigate to MainActivity
                        startMainActivity()
                    } ?: run {
                        Toast.makeText(
                            this@RegisterActivity,
                            R.string.error_occurred,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.error_occurred,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginResponse>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.registerButton.isEnabled = true
                Toast.makeText(
                    this@RegisterActivity,
                    NetworkUtils.getErrorMessage(t),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
    }
}
