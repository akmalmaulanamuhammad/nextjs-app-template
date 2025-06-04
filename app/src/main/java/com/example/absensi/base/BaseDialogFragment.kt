package com.example.absensi.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.example.absensi.util.NetworkUtils
import com.google.android.material.snackbar.Snackbar

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialog()
        initViews()
        observeData()
    }

    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected open fun setupDialog() {
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    protected open fun initViews() {
        // Override in child classes to initialize views
    }

    protected open fun observeData() {
        // Override in child classes to observe LiveData
    }

    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(binding.root, message, duration)
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action.invoke() }
        }
        snackbar.show()
    }

    protected fun checkNetworkAndProceed(action: () -> Unit) {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            action.invoke()
        } else {
            showSnackbar(
                "No internet connection",
                Snackbar.LENGTH_INDEFINITE,
                "Retry"
            ) {
                checkNetworkAndProceed(action)
            }
        }
    }

    protected fun setDialogSize(widthRatio: Float = 0.9f, heightRatio: Float? = null) {
        dialog?.window?.apply {
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * widthRatio).toInt()
            val height = heightRatio?.let {
                (displayMetrics.heightPixels * it).toInt()
            } ?: ViewGroup.LayoutParams.WRAP_CONTENT

            setLayout(width, height)
        }
    }

    protected fun enableDismissOnTouchOutside(enable: Boolean) {
        dialog?.setCanceledOnTouchOutside(enable)
    }

    protected fun enableCancellation(enable: Boolean) {
        dialog?.setCancelable(enable)
    }

    protected fun dismissDialog() {
        dismiss()
    }

    protected fun dismissDialogWithResult(requestKey: String, result: Bundle) {
        parentFragmentManager.setFragmentResult(requestKey, result)
        dismiss()
    }

    protected fun setFragmentResultListener(
        requestKey: String,
        listener: (requestKey: String, bundle: Bundle) -> Unit
    ) {
        parentFragmentManager.setFragmentResultListener(requestKey, this, listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BaseDialogFragment"
    }
}
