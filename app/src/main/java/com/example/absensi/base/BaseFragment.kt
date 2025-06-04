package com.example.absensi.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absensi.AbsensiApplication
import com.example.absensi.ui.auth.LoginActivity
import com.example.absensi.util.NetworkUtils
import com.example.absensi.util.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseFragment : Fragment() {
    protected val preferenceManager: PreferenceManager by lazy {
        AbsensiApplication.getPreferenceManager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        initializeViews(view)
        
        // Setup observers
        setupObservers()
        
        // Log fragment creation in debug mode
        Timber.d("Fragment Created: ${javaClass.simpleName}")
    }

    protected open fun initializeViews(view: View) {
        // Override in child fragments to initialize views
    }

    protected open fun setupObservers() {
        // Override in child fragments to setup observers
    }

    protected fun showLoading(message: String? = null) {
        (activity as? BaseActivity)?.showLoading(message)
    }

    protected fun hideLoading() {
        (activity as? BaseActivity)?.hideLoading()
    }

    protected fun showError(message: String, action: (() -> Unit)? = null) {
        view?.let { rootView ->
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).apply {
                action?.let {
                    setAction("Retry") { it.invoke() }
                }
            }.show()
        }
    }

    protected fun showSuccess(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun showNoNetworkDialog() {
        context?.let { ctx ->
            MaterialAlertDialogBuilder(ctx)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("Retry") { _, _ ->
                    if (NetworkUtils.isNetworkAvailable(ctx)) {
                        onNetworkRestored()
                    } else {
                        showNoNetworkDialog()
                    }
                }
                .setNegativeButton("Close") { _, _ ->
                    activity?.finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    protected open fun onNetworkRestored() {
        // Override in child fragments to handle network restoration
    }

    protected fun handleSessionExpired() {
        lifecycleScope.launch {
            preferenceManager.clearSession()
            
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            activity?.finish()
            
            showError("Session expired. Please login again.")
        }
    }

    protected fun handleApiError(error: NetworkUtils.NetworkResult.Error) {
        when {
            NetworkUtils.isTokenExpired(error) -> handleSessionExpired()
            !NetworkUtils.isNetworkAvailable(requireContext()) -> showNoNetworkDialog()
            else -> showError(error.message)
        }
    }

    protected fun checkNetworkConnectivity(): Boolean {
        return if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showNoNetworkDialog()
            false
        } else {
            true
        }
    }

    protected fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable(requireContext())
    }

    protected fun navigateBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    protected fun refreshFragment() {
        parentFragmentManager.beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("Fragment View Destroyed: ${javaClass.simpleName}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Fragment Destroyed: ${javaClass.simpleName}")
    }

    companion object {
        private const val TAG = "BaseFragment"
    }
}
