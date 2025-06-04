package com.example.absensi.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T : Any, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseAdapter.BaseViewHolder<VB>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = createBinding(LayoutInflater.from(parent.context), parent)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        val item = getItem(position)
        bind(holder.binding, item, position)
    }

    protected abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup): VB

    protected abstract fun bind(binding: VB, item: T, position: Int)

    class BaseViewHolder<VB : ViewBinding>(
        val binding: VB
    ) : RecyclerView.ViewHolder(binding.root)

    // Utility function to update list with loading state
    fun updateList(newList: List<T>, isLoading: Boolean = false) {
        if (isLoading) {
            // Handle loading state if needed
            return
        }
        submitList(newList)
    }

    // Function to add items to the existing list
    fun addItems(items: List<T>) {
        val currentList = currentList.toMutableList()
        currentList.addAll(items)
        submitList(currentList)
    }

    // Function to add a single item
    fun addItem(item: T) {
        val currentList = currentList.toMutableList()
        currentList.add(item)
        submitList(currentList)
    }

    // Function to remove an item
    fun removeItem(item: T) {
        val currentList = currentList.toMutableList()
        currentList.remove(item)
        submitList(currentList)
    }

    // Function to remove item at position
    fun removeItemAt(position: Int) {
        if (position in 0 until itemCount) {
            val currentList = currentList.toMutableList()
            currentList.removeAt(position)
            submitList(currentList)
        }
    }

    // Function to clear the list
    fun clearList() {
        submitList(emptyList())
    }

    // Function to get item at position safely
    fun getItemAtPosition(position: Int): T? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    // Function to update a single item
    fun updateItem(item: T, predicate: (T) -> Boolean) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst(predicate)
        if (index != -1) {
            currentList[index] = item
            submitList(currentList)
        }
    }

    // Function to check if list is empty
    fun isEmpty(): Boolean = itemCount == 0

    // Function to get the current list as mutable
    fun getCurrentList(): MutableList<T> = currentList.toMutableList()
}

// Generic DiffUtil.ItemCallback implementation
abstract class BaseDiffCallback<T : Any> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return compareItems(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    abstract fun compareItems(oldItem: T, newItem: T): Boolean
}

// Extension function for easy adapter creation
inline fun <T : Any, VB : ViewBinding> createAdapter(
    crossinline createBinding: (LayoutInflater, ViewGroup) -> VB,
    crossinline bind: (VB, T, Int) -> Unit,
    diffCallback: DiffUtil.ItemCallback<T>
): BaseAdapter<T, VB> {
    return object : BaseAdapter<T, VB>(diffCallback) {
        override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): VB {
            return createBinding(inflater, parent)
        }

        override fun bind(binding: VB, item: T, position: Int) {
            bind(binding, item, position)
        }
    }
}
