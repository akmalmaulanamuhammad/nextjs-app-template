package com.example.absensi.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.absensi.databinding.FragmentProfileBinding
import com.example.absensi.model.User
import com.example.absensi.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserProfile()
        setupListeners()
    }

    private fun loadUserProfile() {
        binding.progressBar.visibility = View.VISIBLE
        
        // TODO: Replace with actual API call
        // ApiClient.service.getUserProfile().enqueue(object : Callback<ApiResponse<User>> {
        //     override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
        //         binding.progressBar.visibility = View.GONE
        //         if (response.isSuccessful) {
        //             response.body()?.data?.let { user ->
        //                 updateUI(user)
        //             }
        //         }
        //     }
        //
        //     override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
        //         binding.progressBar.visibility = View.GONE
        //         Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
        //     }
        // })
    }

    private fun updateUI(user: User) {
        binding.apply {
            nameInput.setText(user.name)
            emailInput.setText(user.email)
            phoneInput.setText(user.phone)
            employeeIdInput.setText(user.employeeId)
            
            // Set office location if available
            user.officeLocation?.let { location ->
                officeLocationText.text = location.name
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            updateProfileButton.setOnClickListener {
                updateProfile()
            }

            changePasswordButton.setOnClickListener {
                showChangePasswordDialog()
            }

            officeLocationCard.setOnClickListener {
                showOfficeLocationDialog()
            }

            logoutButton.setOnClickListener {
                logout()
            }
        }
    }

    private fun updateProfile() {
        val name = binding.nameInput.text.toString()
        val phone = binding.phoneInput.text.toString()

        if (name.isBlank()) {
            binding.nameInput.error = "Name is required"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        
        // TODO: Implement profile update API call
        // val request = UpdateProfileRequest(name = name, phone = phone)
        // ApiClient.service.updateProfile(request)...
    }

    private fun showChangePasswordDialog() {
        // TODO: Implement password change dialog
    }

    private fun showOfficeLocationDialog() {
        // TODO: Implement office location selection dialog
    }

    private fun logout() {
        // TODO: Implement logout functionality
        // Clear preferences/token
        // Navigate to login screen
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
