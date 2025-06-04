package com.example.absensi.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.absensi.databinding.FragmentHistoryBinding
import com.example.absensi.model.AttendanceRecord
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadAttendanceHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun loadAttendanceHistory() {
        binding.progressBar.visibility = View.VISIBLE
        
        // TODO: Replace with actual API call using Retrofit
        // ApiClient.service.getAttendanceHistory(userId).enqueue(object : Callback<List<AttendanceRecord>> {
        //     override fun onResponse(call: Call<List<AttendanceRecord>>, response: Response<List<AttendanceRecord>>) {
        //         binding.progressBar.visibility = View.GONE
        //         if (response.isSuccessful) {
        //             historyAdapter.submitList(response.body())
        //         }
        //     }
        //
        //     override fun onFailure(call: Call<List<AttendanceRecord>>, t: Throwable) {
        //         binding.progressBar.visibility = View.GONE
        //         // Show error message
        //     }
        // })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
