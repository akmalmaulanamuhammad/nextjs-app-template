package com.example.absensi.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    protected lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                bottomSheetBehavior = BottomSheetBehavior.from(it)
                setupBottomSheet(bottomSheetBehavior)
            }
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
        initViews()
        observeData()
    }

    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected open fun setupBottomSheet(bottomSheetBehavior: BottomSheetBehavior<FrameLayout>) {
        // Default bottom sheet setup
        bottomSheetBehavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
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

    protected fun setBottomSheetState(state: Int) {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.state = state
        }
    }

    protected fun setPeekHeight(heightDp: Int) {
        if (::bottomSheetBehavior.isInitialized) {
            val heightPx = (resources.displayMetrics.density * heightDp).toInt()
            bottomSheetBehavior.peekHeight = heightPx
        }
    }

    protected fun setDraggable(draggable: Boolean) {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.isDraggable = draggable
        }
    }

    protected fun setSkipCollapsed(skip: Boolean) {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.skipCollapsed = skip
        }
    }

    protected fun addBottomSheetCallback(
        onStateChanged: ((bottomSheet: View, newState: Int) -> Unit)? = null,
        onSlide: ((bottomSheet: View, slideOffset: Float) -> Unit)? = null
    ) {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    onStateChanged?.invoke(bottomSheet, newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    onSlide?.invoke(bottomSheet, slideOffset)
                }
            })
        }
    }

    protected fun dismissSheet() {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        dismiss()
    }

    protected fun dismissWithResult(requestKey: String, result: Bundle) {
        parentFragmentManager.setFragmentResult(requestKey, result)
        dismissSheet()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BaseBottomSheetDialogFragment"
    }
}
