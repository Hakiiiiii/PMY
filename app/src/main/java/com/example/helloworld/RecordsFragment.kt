package com.example.helloworld

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.helloworld.MyApplication

class RecordsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.records_fragment, container, false)

        val lvRecords = view.findViewById<ListView>(R.id.lvRecords)

        Thread {
            val scores = MyApplication.database.appDao().getAllScores()
            requireActivity().runOnUiThread {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, scores.map { "${it.userName} - Очки: ${it.score} (Сложность: ${it.difficulty}, Дата: ${it.date})" })
                lvRecords.adapter = adapter
            }
        }.start()

        return view
    }
}