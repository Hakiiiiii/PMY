package com.example.helloworld

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class RegistrationFragment : Fragment() {

    private var selectedDateMillis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val rgGender = view.findViewById<RadioGroup>(R.id.rgGender)
        val spCourse = view.findViewById<Spinner>(R.id.spCourse)
        val sbDifficulty = view.findViewById<SeekBar>(R.id.sbDifficulty)
        val cvBirthDate = view.findViewById<CalendarView>(R.id.cvBirthDate)
        val ivZodiac = view.findViewById<ImageView>(R.id.ivZodiac)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val tvOutput = view.findViewById<TextView>(R.id.tvOutput)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCourse.adapter = adapter

        cvBirthDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDateMillis = calendar.timeInMillis
        }

        btnSubmit.setOnClickListener {
            val fullName = etFullName.text.toString()
            val genderId = rgGender.checkedRadioButtonId
            val gender = if (genderId == R.id.rbMale) "Мужской" else "Женский"
            val course = spCourse.selectedItem.toString()
            val difficulty = sbDifficulty.progress
            val birthDate = selectedDateMillis

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = birthDate
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val zodiac = getZodiacSign(day, month)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(calendar.time)
            tvOutput.text = "Выбрана дата: $formattedDate\nДень: $day, Месяц: $month\nЗодиак: $zodiac\nФИО: $fullName\nПол: $gender\nКурс: $course\nСложность: $difficulty"
            ivZodiac.setImageResource(getZodiacImage(zodiac))
            ivZodiac.visibility = View.VISIBLE
        }

        return view
    }

    private fun getZodiacSign(day: Int, month: Int): String {
        return when (month) {
            1 -> if (day <= 19) "Козерог" else "Водолей"
            2 -> if (day <= 18) "Водолей" else "Рыбы"
            3 -> if (day <= 20) "Рыбы" else "Овен"
            4 -> if (day <= 19) "Овен" else "Телец"
            5 -> if (day <= 20) "Телец" else "Близнецы"
            6 -> if (day <= 20) "Близнецы" else "Рак"
            7 -> if (day <= 22) "Рак" else "Лев"
            8 -> if (day <= 22) "Лев" else "Дева"
            9 -> if (day <= 22) "Дева" else "Весы"
            10 -> if (day <= 22) "Весы" else "Скорпион"
            11 -> if (day <= 21) "Скорпион" else "Стрелец"
            12 -> if (day <= 21) "Стрелец" else "Козерог"
            else -> "Неизвестно"
        }
    }

    private fun getZodiacImage(zodiac: String): Int {
        return when (zodiac) {
            "Овен" -> R.drawable.zodiac_aries
            "Телец" -> R.drawable.zodiac_telesc
            "Близнецы" -> R.drawable.zodiac_bliznecy
            "Рак" -> R.drawable.zodiac_rak
            "Лев" -> R.drawable.zodiac_lev
            "Дева" -> R.drawable.zodiac_deva
            "Весы" -> R.drawable.zodiac_vesy
            "Скорпион" -> R.drawable.zodiac_skorpion
            "Стрелец" -> R.drawable.zodiac_strelec
            "Козерог" -> R.drawable.zodiac_kozerog
            "Водолей" -> R.drawable.zodiac_vodoley
            "Рыбы" -> R.drawable.zodiac_ryby
            else -> android.R.color.transparent
        }
    }
}