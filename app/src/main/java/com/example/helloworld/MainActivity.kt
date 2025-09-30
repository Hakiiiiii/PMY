package com.example.helloworld

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private var selectedDateMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим все элементы интерфейса
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val spCourse = findViewById<Spinner>(R.id.spCourse)
        val sbDifficulty = findViewById<SeekBar>(R.id.sbDifficulty)
        val cvBirthDate = findViewById<CalendarView>(R.id.cvBirthDate)
        val ivZodiac = findViewById<ImageView>(R.id.ivZodiac)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val tvOutput = findViewById<TextView>(R.id.tvOutput)

        // Настраиваем Spinner для курсов
        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCourse.adapter = adapter

        // Слушатель для CalendarView
        cvBirthDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDateMillis = calendar.timeInMillis
        }

        // Обработчик кнопки
        btnSubmit.setOnClickListener {
            val fullName = etFullName.text.toString()
            val genderId = rgGender.checkedRadioButtonId
            val gender = if (genderId == R.id.rbMale) "Мужской" else "Женский"
            val course = spCourse.selectedItem.toString()
            val difficulty = sbDifficulty.progress
            val birthDate = selectedDateMillis // Используем сохранённую дату

            // Преобразуем дату
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = birthDate
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1

            val zodiac = getZodiacSign(day, month)

            // Подробный отладочный вывод
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(calendar.time)
            tvOutput.text = "Выбрана дата: $formattedDate\nДень: $day, Месяц: $month\nЗодиак: $zodiac\nФИО: $fullName\nПол: $gender\nКурс: $course\nСложность: $difficulty"
            ivZodiac.setImageResource(getZodiacImage(zodiac))
            ivZodiac.visibility = View.VISIBLE
        }
    }

    // Функция для определения знака зодиака
    private fun getZodiacSign(day: Int, month: Int): String {
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Овен"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Телец"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Близнецы"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Рак"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Лев"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Дева"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Весы"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Скорпион"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Стрелец"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Козовод"
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Водолей"
            else -> "Рыбы"
        }
    }

    // Функция для получения изображения знака зодиака
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
            "Козовод" -> R.drawable.zodiac_kozerog2
            "Водолей" -> R.drawable.zodiac_vodoley
            "Рыбы" -> R.drawable.zodiac_ryby
            else -> android.R.color.transparent
        }
    }
}