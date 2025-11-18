package com.example.helloworld

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.helloworld.MyApplication
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.records_fragment, container, false)

        val lvRecords = view.findViewById<ListView>(R.id.lvRecords)

        lifecycleScope.launch {
            try {
                val scores = MyApplication.database.appDao().getAllScores()
                val list = scores.map { "${it.userName} — ${it.score} очков (${it.date})" }
                lvRecords.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки рекордов", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}