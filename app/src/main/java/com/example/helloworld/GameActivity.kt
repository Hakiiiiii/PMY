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
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private var score = 0
    private lateinit var tvScore: TextView
    private lateinit var gameLayout: FrameLayout
    private lateinit var btnPause: Button
    private val handler = Handler(Looper.getMainLooper())
    private val bugs = mutableListOf<ImageView>()
    private var maxBugs = 5
    private var bugInterval = 1000L
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)


        val sharedPref: SharedPreferences = getSharedPreferences("game_prefs", MODE_PRIVATE)
        maxBugs = sharedPref.getInt("max_roaches", 5)
        bugInterval = sharedPref.getInt("bonus_interval", 10) * 100L

        tvScore = findViewById(R.id.tvScore)
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

        startGame()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun startGame() {
        if (!isPaused) {
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (bugs.size < maxBugs) {
                        addBug()
                    }
                    startGame()
                }
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
            }
        }

        gameLayout.addView(bug)
        bugs.add(bug)

        animateBug(bug)
    }

    private fun animateBug(bug: ImageView) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isPaused) {
                    bug.x = Random.nextFloat() * (gameLayout.width - 150)
                    bug.y = Random.nextFloat() * (gameLayout.height - 150)
                    animateBug(bug)
                }
            }
        }, Random.nextLong(500, 1500))
    }

    private fun updateScore() {
        tvScore.text = "Очки: $score"
    }


    private fun pauseGame() {
        isPaused = true

        val items = arrayOf("Настройки", "Авторы", "Правила")
        AlertDialog.Builder(this)
            .setTitle("Игровое меню")
            .setItems(items) { dialog, which ->
                val intent = Intent(this, MainActivity::class.java)
                when (which) {
                    0 -> intent.putExtra("selected_tab", 3)
                    1 -> intent.putExtra("selected_tab", 2)
                    2 -> intent.putExtra("selected_tab", 1)
                }
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