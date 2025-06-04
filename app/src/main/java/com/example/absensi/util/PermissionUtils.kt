package com.example.absensi.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {
    const val PERMISSION_REQUEST_CODE = 100
    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val CAMERA_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )
    private val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun hasLocationPermissions(context: Context): Boolean {
        return LOCATION_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasCameraPermissions(context: Context): Boolean {
        return CAMERA_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasStoragePermissions(context: Context): Boolean {
        return STORAGE_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    fun requestLocationPermissions(fragment: Fragment) {
        fragment.requestPermissions(
            LOCATION_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    fun requestCameraPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            CAMERA_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    fun requestCameraPermissions(fragment: Fragment) {
        fragment.requestPermissions(
            CAMERA_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    fun requestStoragePermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            STORAGE_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    fun requestStoragePermissions(fragment: Fragment) {
        fragment.requestPermissions(
            STORAGE_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    fun requestMultiplePermissions(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int = PERMISSION_REQUEST_CODE
    ) {
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            requestCode
        )
    }

    fun requestMultiplePermissions(
        fragment: Fragment,
        permissions: Array<String>,
        requestCode: Int = PERMISSION_REQUEST_CODE
    ) {
        fragment.requestPermissions(
            permissions,
            requestCode
        )
    }

    fun requestMultiplePermissions(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>
    ) {
        launcher.launch(permissions)
    }

    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun shouldShowRationale(fragment: Fragment, permission: String): Boolean {
        return fragment.shouldShowRequestPermissionRationale(permission)
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    fun checkPermissionResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        return grantResults.isNotEmpty() && grantResults.all {
            it == PackageManager.PERMISSION_GRANTED
        }
    }

    fun areAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return LOCATION_PERMISSIONS + CAMERA_PERMISSIONS + STORAGE_PERMISSIONS
    }

    fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPermanentlyDeniedPermissions(
        activity: Activity,
        permissions: Array<String>
    ): List<String> {
        return permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    fun getPermanentlyDeniedPermissions(
        fragment: Fragment,
        permissions: Array<String>
    ): List<String> {
        return permissions.filter {
            ContextCompat.checkSelfPermission(fragment.requireContext(), it) != PackageManager.PERMISSION_GRANTED &&
                    !fragment.shouldShowRequestPermissionRationale(it)
        }
    }
}
