package com.example.absensi.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.absensi.R
import com.example.absensi.base.BaseAdapter
import com.example.absensi.base.BaseDiffCallback
import com.example.absensi.databinding.ItemAttendanceHistoryBinding
import com.example.absensi.model.AttendanceRecord
import com.example.absensi.util.DateTimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class HistoryAdapter(
    private val onItemClick: (AttendanceRecord) -> Unit,
    private val onPhotoClick: (String) -> Unit
) : BaseAdapter<AttendanceRecord, ItemAttendanceHistoryBinding>(AttendanceDiffCallback()) {

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemAttendanceHistoryBinding {
        return ItemAttendanceHistoryBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemAttendanceHistoryBinding, item: AttendanceRecord, position: Int) {
        with(binding) {
            // Set date
            dateText.text = DateTimeUtils.formatDate(item.date)

            // Set check-in time and location
            checkInTimeText.text = item.getFormattedCheckInTime()
            checkInLocationText.text = item.checkInLocation?.address ?: "-"

            // Set check-out time and location
            checkOutTimeText.text = item.getFormattedCheckOutTime()
            checkOutLocationText.text = item.checkOutLocation?.address ?: "-"

            // Set work duration
            workDurationText.text = item.getWorkDuration()

            // Set status
            val status = item.getAttendanceStatus()
            statusText.text = status.getDisplayText()
            statusText.setTextColor(ContextCompat.getColor(root.context, status.getColorRes()))

            // Set check-in photo
            item.checkInPhoto?.let { photoUrl ->
                Glide.with(checkInPhotoImage)
                    .load(photoUrl)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.placeholder_photo)
                    .error(R.drawable.error_photo)
                    .into(checkInPhotoImage)

                checkInPhotoImage.setOnClickListener {
                    onPhotoClick(photoUrl)
                }
            } ?: run {
                checkInPhotoImage.setImageResource(R.drawable.placeholder_photo)
                checkInPhotoImage.setOnClickListener(null)
            }

            // Set check-out photo
            item.checkOutPhoto?.let { photoUrl ->
                Glide.with(checkOutPhotoImage)
                    .load(photoUrl)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.placeholder_photo)
                    .error(R.drawable.error_photo)
                    .into(checkOutPhotoImage)

                checkOutPhotoImage.setOnClickListener {
                    onPhotoClick(photoUrl)
                }
            } ?: run {
                checkOutPhotoImage.setImageResource(R.drawable.placeholder_photo)
                checkOutPhotoImage.setOnClickListener(null)
            }

            // Set notes if any
            if (!item.notes.isNullOrBlank()) {
                notesText.text = item.notes
                notesContainer.show()
            } else {
                notesContainer.hide()
            }

            // Set click listener
            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class AttendanceDiffCallback : BaseDiffCallback<AttendanceRecord>() {
        override fun compareItems(oldItem: AttendanceRecord, newItem: AttendanceRecord): Boolean {
            return oldItem.id == newItem.id
        }
    }

    fun getItemAtPosition(position: Int): AttendanceRecord? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    fun updateItems(items: List<AttendanceRecord>, isLoading: Boolean = false) {
        if (isLoading) {
            // Handle loading state if needed
            return
        }
        submitList(items)
    }

    fun addItems(items: List<AttendanceRecord>) {
        val currentList = currentList.toMutableList()
        currentList.addAll(items)
        submitList(currentList)
    }

    fun clearItems() {
        submitList(emptyList())
    }

    fun filterItems(predicate: (AttendanceRecord) -> Boolean) {
        val filteredList = currentList.filter(predicate)
        submitList(filteredList)
    }

    fun sortItems(comparator: Comparator<AttendanceRecord>) {
        val sortedList = currentList.sortedWith(comparator)
        submitList(sortedList)
    }

    companion object {
        private const val TAG = "HistoryAdapter"
    }
}
