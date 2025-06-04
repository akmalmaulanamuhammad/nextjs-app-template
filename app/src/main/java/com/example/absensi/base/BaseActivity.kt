package com.example.absensi.base

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.absensi.AbsensiApplication
import com.example.absensi.ui.auth.LoginActivity
import com.example.absensi.util.NetworkUtils
import com.example.absensi.util.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {
    protected val preferenceManager: PreferenceManager by lazy {
        AbsensiApplication.getPreferenceManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Setup activity
        setupActivity()
        
        // Check network connectivity
        checkNetworkConnectivity()
        
        // Initialize views
        initializeViews()
        
        // Setup observers
        setupObservers()
        
        // Log activity creation in debug mode
        Timber.d("Activity Created: ${javaClass.simpleName}")
    }

    protected open fun setupActivity() {
        // Enable back button in action bar if not the main activity
        if (this !is com.example.absensi.MainActivity) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    protected open fun initializeViews() {
        // Override in child activities to initialize views
    }

    protected open fun setupObservers() {
        // Override in child activities to setup observers
    }

    private fun checkNetworkConnectivity() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoNetworkDialog()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected fun showLoading(message: String? = null) {
        // Override in child activities if custom loading is needed
    }

    protected fun hideLoading() {
        // Override in child activities if custom loading is needed
    }

    protected fun showError(message: String, action: (() -> Unit)? = null) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).apply {
            action?.let {
                setAction("Retry") { it.invoke() }
            }
        }.show()
    }

    protected fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showNoNetworkDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("Retry") { _, _ ->
                if (NetworkUtils.isNetworkAvailable(this)) {
                    onNetworkRestored()
                } else {
                    showNoNetworkDialog()
                }
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    protected open fun onNetworkRestored() {
        // Override in child activities to handle network restoration
    }

    protected fun handleSessionExpired() {
        lifecycleScope.launch {
            preferenceManager.clearSession()
            
            val intent = Intent(this@BaseActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            
            showError("Session expired. Please login again.")
        }
    }

    protected fun handleApiError(error: NetworkUtils.NetworkResult.Error) {
        when {
            NetworkUtils.isTokenExpired(error) -> handleSessionExpired()
            !NetworkUtils.isNetworkAvailable(this) -> showNoNetworkDialog()
            else -> showError(error.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Activity Destroyed: ${javaClass.simpleName}")
    }

    companion object {
        private const val TAG = "BaseActivity"
    }
}
