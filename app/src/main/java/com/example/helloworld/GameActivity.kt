package com.example.helloworld

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.helloworld.MyApplication
import com.example.helloworld.database.Score
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private var score = 0
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var gameLayout: FrameLayout
    private lateinit var btnPause: Button
    private val handler = Handler(Looper.getMainLooper())
    private val bugs = mutableListOf<ImageView>()
    private var maxBugs = 5
    private var bugInterval = 1000L
    private var isPaused = false
    private var timerSeconds = 300 // По умолчанию 5 мин (300 сек)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Чтение настроек
        val sharedPref: SharedPreferences = getSharedPreferences("game_prefs", MODE_PRIVATE)
        maxBugs = sharedPref.getInt("max_roaches", 5).coerceIn(1, 10)
        bugInterval = sharedPref.getInt("bonus_interval", 10) * 100L
        val roundMinutes = sharedPref.getInt("round_duration", 5)
        timerSeconds = roundMinutes * 60

        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        gameLayout = findViewById(R.id.gameLayout)
        btnPause = findViewById(R.id.btnPause)

        gameLayout.setOnClickListener {
            if (!isPaused) {
                score -= 5
                updateScore()
            }
        }

        btnPause.setOnClickListener {
            pauseGame()
        }

        // Добавляем начальное количество тараканов (половина от макс, но не меньше 1)
        val initialBugs = (maxBugs / 2).coerceAtLeast(1)
        repeat(initialBugs) {
            addBug()
        }

        startGame()
        startTimer()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun startGame() {
        if (!isPaused) {
            handler.postDelayed({
                if (bugs.size < maxBugs) {
                    addBug()
                }
                startGame()
            }, bugInterval)
        }
    }

    private fun addBug() {
        val bug = ImageView(this)
        bug.setImageResource(R.drawable.bug1)
        bug.layoutParams = FrameLayout.LayoutParams(150, 150)
        bug.x = Random.nextFloat() * (gameLayout.width - 150)
        bug.y = Random.nextFloat() * (gameLayout.height - 150)

        bug.setOnClickListener {
            if (!isPaused) {
                score += 10
                updateScore()
                gameLayout.removeView(bug)
                bugs.remove(bug)

                // Респаун нового таракана сразу после убийства
                if (bugs.size < maxBugs) {
                    handler.postDelayed({
                        addBug()
                    }, 500) // Задержка 0.5 сек для респауна
                }
            }
        }

        gameLayout.addView(bug)
        bugs.add(bug)

        animateBug(bug)
    }

    private fun animateBug(bug: ImageView) {
        handler.postDelayed({
            if (!isPaused) {
                bug.x = Random.nextFloat() * (gameLayout.width - 150)
                bug.y = Random.nextFloat() * (gameLayout.height - 150)
                animateBug(bug)
            }
        }, Random.nextLong(500, 1500))
    }

    private fun updateScore() {
        tvScore.text = "Очки: $score"
    }

    private fun startTimer() {
        val timerHandler = Handler(Looper.getMainLooper())
        timerHandler.postDelayed(object : Runnable {
            override fun run() {
                if (!isPaused && timerSeconds > 0) {
                    timerSeconds--
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    tvTimer.text = "Время: $minutes:${seconds.toString().padStart(2, '0')}"

                    timerHandler.postDelayed(this, 1000)
                } else if (timerSeconds <= 0) {
                    endGame()
                }
            }
        }, 1000)
    }

    private fun endGame() {
        isPaused = true
        AlertDialog.Builder(this)
            .setTitle("Игра закончена!")
            .setMessage("Ваши очки: $score")
            .setPositiveButton("OK") { dialog, _ ->
                saveScore()
                finish()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun saveScore() {
        val sharedPref = getSharedPreferences("game_prefs", MODE_PRIVATE)
        val userName = sharedPref.getString("current_user", "Anonymous") ?: "Anonymous"
        val difficulty = sharedPref.getInt("speed", 5)
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Проверяем, есть ли пользователь в базе
        Thread {
            val dao = MyApplication.database.appDao()
            val existingScore = dao.getAllScores().firstOrNull { it.userName == userName }

            if (existingScore != null) {
                // Обновляем, если новый score выше
                if (score > existingScore.score) {
                    dao.updateRecord(userName, score)
                }
            } else {
                // Добавляем новую запись, если пользователя нет
                val newScore = Score(userName = userName, score = score, difficulty = difficulty, date = date)
                dao.insertScore(newScore)
            }
        }.start()
    }

    private fun pauseGame() {
        isPaused = true

        AlertDialog.Builder(this)
            .setTitle("Игровое меню")
            .setItems(arrayOf("Настройки", "Авторы", "Правила")) { _, which ->
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("selected_tab", when (which) {
                    0 -> 3
                    1 -> 2
                    2 -> 1
                    else -> 0
                })
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Продолжить игру") { dialog, _ ->
                isPaused = false
                startGame()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                pauseGame()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}