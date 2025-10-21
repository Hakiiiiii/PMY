package com.example.helloworld

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sharedPref = requireActivity().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

        val sbSpeed = view.findViewById<SeekBar>(R.id.sbSpeed)
        val tvSpeedValue = view.findViewById<TextView>(R.id.tvSpeedValue)
        val etMaxRoaches = view.findViewById<EditText>(R.id.etMaxRoaches)
        val etBonusInterval = view.findViewById<EditText>(R.id.etBonusInterval)
        val etRoundDuration = view.findViewById<EditText>(R.id.etRoundDuration)
        val btnPlay = view.findViewById<Button>(R.id.btnPlay)

        // Загрузка сохранённых значений
        val speed = sharedPref.getInt("speed", 5)
        sbSpeed.progress = speed
        tvSpeedValue.text = "Скорость: $speed"
        etMaxRoaches.setText(sharedPref.getInt("max_roaches", 5).toString())
        etBonusInterval.setText(sharedPref.getInt("bonus_interval", 10).toString())
        etRoundDuration.setText(sharedPref.getInt("round_duration", 5).toString())

        // Обновление значения скорости рядом с ползунком и сохранение
        sbSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvSpeedValue.text = "Скорость: $progress"
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = sbSpeed.progress
                sharedPref.edit().putInt("speed", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        })

        // Сохранение при потере фокуса
        etMaxRoaches.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = etMaxRoaches.text.toString().toIntOrNull() ?: 5
                sharedPref.edit().putInt("max_roaches", value).apply()
            }
        }

        etBonusInterval.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = etBonusInterval.text.toString().toIntOrNull() ?: 10
                sharedPref.edit().putInt("bonus_interval", value).apply()
            }
        }

        etRoundDuration.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = etRoundDuration.text.toString().toIntOrNull() ?: 5
                sharedPref.edit().putInt("round_duration", value).apply()
            }
        }

        // Кнопка "Играть" для запуска игры
        btnPlay.setOnClickListener {
            val intent = Intent(requireActivity(), GameActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}