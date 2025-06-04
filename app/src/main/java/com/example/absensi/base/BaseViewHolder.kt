package com.example.absensi.base

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseViewHolder<T>(
    private val binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    protected val context: Context
        get() = itemView.context

    abstract fun bind(item: T)

    protected fun getString(resId: Int): String {
        return context.getString(resId)
    }

    protected fun getString(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    protected fun setOnClickListener(view: View, action: () -> Unit) {
        view.setOnClickListener { action.invoke() }
    }

    protected fun setOnLongClickListener(view: View, action: () -> Boolean) {
        view.setOnLongClickListener { action.invoke() }
    }

    // Utility function to handle visibility
    protected fun View.show() {
        visibility = View.VISIBLE
    }

    protected fun View.hide() {
        visibility = View.GONE
    }

    protected fun View.invisible() {
        visibility = View.INVISIBLE
    }

    // Utility function to handle enabled state
    protected fun View.enable() {
        isEnabled = true
        alpha = 1.0f
    }

    protected fun View.disable() {
        isEnabled = false
        alpha = 0.5f
    }

    // Utility function to handle selection state
    protected fun View.select() {
        isSelected = true
    }

    protected fun View.deselect() {
        isSelected = false
    }

    // Function to handle item click events
    protected fun handleItemClick(item: T, onClick: (T) -> Unit) {
        itemView.setOnClickListener {
            onClick(item)
        }
    }

    // Function to handle item long click events
    protected fun handleItemLongClick(item: T, onLongClick: (T) -> Boolean) {
        itemView.setOnLongClickListener {
            onLongClick(item)
        }
    }

    // Function to handle multiple view click events
    protected fun handleMultipleClicks(vararg pairs: Pair<View, () -> Unit>) {
        pairs.forEach { (view, action) ->
            view.setOnClickListener { action.invoke() }
        }
    }

    // Function to set multiple view visibility
    protected fun setVisibility(visibility: Int, vararg views: View) {
        views.forEach { it.visibility = visibility }
    }

    // Function to enable/disable multiple views
    protected fun setEnabled(enabled: Boolean, vararg views: View) {
        views.forEach { 
            it.isEnabled = enabled
            it.alpha = if (enabled) 1.0f else 0.5f
        }
    }

    // Function to set selected state for multiple views
    protected fun setSelected(selected: Boolean, vararg views: View) {
        views.forEach { it.isSelected = selected }
    }

    // Function to handle view animations
    protected fun animateView(view: View, animation: android.view.animation.Animation) {
        view.startAnimation(animation)
    }

    // Function to clear animations
    protected fun clearAnimations(vararg views: View) {
        views.forEach { it.clearAnimation() }
    }

    // Function to handle view tag
    protected fun <T> View.setTag(key: Int, value: T) {
        setTag(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T> View.getTag(key: Int): T? {
        return getTag(key) as? T
    }

    // Function to update partial content
    open fun updatePartial(item: T, payloads: List<Any>) {
        // Override in child classes to handle partial updates
    }

    // Function to clean up resources
    open fun unbind() {
        // Override in child classes to clean up resources
        clearAnimations(itemView)
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
    }
}
