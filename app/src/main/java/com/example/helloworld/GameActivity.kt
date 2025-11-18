package com.example.helloworld

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Данные таракана — x и y меняются, поэтому var!
data class BugData(
    var x: Float,
    var y: Float,
    val tag: String
)

class GameActivity : AppCompatActivity(), SensorEventListener {

    private val viewModel: GameViewModel by viewModel()

    // UI
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var gameLayout: FrameLayout
    private lateinit var btnPause: Button
    private lateinit var bonusCoin: ImageView
    private lateinit var goldBug: ImageView

    // Система
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sensorManager: SensorManager
    private var mediaPlayer: MediaPlayer? = null
    private var isGameRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        initViews()
        observeViewModel()

        if (savedInstanceState == null) {
            loadSettings()
        }

        startGameLoop()
        startTimer()
        startBonusTimer()
        startGoldBugTimer()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.loadGoldRate()
        viewModel.goldRate.observe(this) { rate ->
            Toast.makeText(this, "Курс золота: $rate ₽", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        gameLayout = findViewById(R.id.gameLayout)
        btnPause = findViewById(R.id.btnPause)
        bonusCoin = findViewById(R.id.bonusCoin)
        goldBug = findViewById(R.id.goldBug)

        btnPause.setOnClickListener { pauseGame() }
        gameLayout.setOnClickListener { v ->

            if (v is ImageView && v.tag != null) return@setOnClickListener


            if (viewModel.isPaused.value != true && isGameRunning) {
                viewModel.subtractScore(5)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.score.observe(this) { score ->
            tvScore.text = getString(R.string.score_format, score)
        }

        viewModel.timerSeconds.observe(this) { seconds ->
            val minutes = seconds / 60
            val secs = seconds % 60
            tvTimer.text = getString(R.string.timer_format, minutes, secs.toString().padStart(2, '0'))
            if (seconds <= 0 && isGameRunning) endGame()
        }

        // ← ИСПРАВЛЕНИЕ ЗДЕСЬ! Только при изменении СПИСКА, а не позиций!
        viewModel.bugs.observe(this) { newList ->
            // Если список полностью новый (например, после поворота) — восстанавливаем
            if (gameLayout.childCount == 0 || newList.size != gameLayout.childCount) {
                restoreBugsFromViewModel()
            }
            // Иначе — просто обновляем позиции уже существующих вью (это быстро и безопасно)
            else {
                newList.forEach { bugData ->
                    val view = gameLayout.findViewWithTag<ImageView>(bugData.tag)
                    view?.x = bugData.x
                    view?.y = bugData.y
                }
            }
        }
    }

    private fun restoreBugsFromViewModel() {
        gameLayout.removeAllViews()
        viewModel.bugs.value?.forEach { bugData ->
            createBugView(bugData.x, bugData.y, bugData.tag).also {
                gameLayout.addView(it)
                animateBug(it, bugData)
            }
        }
    }

    private fun createBugView(x: Float, y: Float, tag: String): ImageView {
        return ImageView(this).apply {
            setImageResource(R.drawable.bug1)
            layoutParams = FrameLayout.LayoutParams(150, 150)
            this.x = x
            this.y = y
            this.tag = tag

            setOnClickListener {
                if (viewModel.isPaused.value == true || !isGameRunning) return@setOnClickListener
                viewModel.addScore(10)
                gameLayout.removeView(this)
                viewModel.removeBugByTag(tag)

                if ((viewModel.bugs.value?.size ?: 0) < (viewModel.maxBugs.value ?: 0)) {
                    safePostDelayed({ addBug() }, 500)
                }
            }
        }
    }

    private fun addBug() {
        if (!isGameRunning || viewModel.isPaused.value == true || isFinishing) return
        if ((viewModel.bugs.value?.size ?: 0) >= (viewModel.maxBugs.value ?: 0)) return

        val position = viewModel.getRandomPosition()
        val tag = System.currentTimeMillis().toString()

        val bugView = createBugView(position.first, position.second, tag)
        gameLayout.addView(bugView)

        val bugData = BugData(position.first, position.second, tag)
        viewModel.addBug(bugData)
        animateBug(bugView, bugData)
    }

    private fun animateBug(bug: ImageView, bugData: BugData) {
        safePostDelayed({
            if (!isGameRunning || viewModel.isPaused.value == true || isFinishing) return@safePostDelayed
            if (viewModel.bugs.value?.none { it.tag == bugData.tag } == true) return@safePostDelayed

            val newPos = viewModel.getRandomPosition()
            bug.x = newPos.first
            bug.y = newPos.second
            viewModel.updateBugPosition(bugData.tag, newPos.first, newPos.second)

            animateBug(bug, bugData.copy(x = newPos.first, y = newPos.second))
        }, Random.nextLong(3500, 6000))
    }

    // === БЕЗОПАСНЫЙ postDelayed — НИКАКИХ КРАШЕЙ ПРИ ПОВОРОТЕ И ВЫХОДЕ ===
    private fun safePostDelayed(action: () -> Unit, delayMs: Long) {
        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                action()
            }
        }, delayMs)
    }

    private fun startGameLoop() {
        isGameRunning = true
        safePostDelayed({
            if (isGameRunning && viewModel.isPaused.value != true &&
                (viewModel.bugs.value?.size ?: 0) < (viewModel.maxBugs.value ?: 0)
            ) {
                addBug()
            }
            if (isGameRunning) startGameLoop()
        }, viewModel.bugInterval.value ?: 3500L)
    }

    private fun startTimer() {
        lifecycleScope.launch {
            while (isGameRunning && viewModel.isPaused.value != true) {
                delay(1000)
                viewModel.updateTimer()
            }
        }
    }

    private fun startBonusTimer() {
        safePostDelayed({
            if (isGameRunning && viewModel.isPaused.value != true) showBonus()
            if (isGameRunning) startBonusTimer()
        }, 15_000)
    }

    private fun showBonus() {
        if (isFinishing) return
        bonusCoin.visibility = ImageView.VISIBLE
        bonusCoin.setOnClickListener {
            bonusCoin.visibility = ImageView.GONE
            viewModel.setTiltMode(true)
            playBugCry()
            Toast.makeText(this, "Наклон включён!", Toast.LENGTH_SHORT).show()

            safePostDelayed({
                if (!isFinishing) {
                    viewModel.setTiltMode(false)
                    Toast.makeText(this, "Наклон выключен", Toast.LENGTH_SHORT).show()
                }
            }, 10_000)
        }
    }

    private fun startGoldBugTimer() {
        safePostDelayed({
            if (isGameRunning && viewModel.isPaused.value != true) spawnGoldBug()
            if (isGameRunning) startGoldBugTimer()
        }, 20_000)
    }

    private fun spawnGoldBug() {
        if (goldBug.visibility == ImageView.VISIBLE || isFinishing) return
        val pos = viewModel.getRandomPosition()
        goldBug.x = pos.first
        goldBug.y = pos.second
        goldBug.visibility = ImageView.VISIBLE

        goldBug.setOnClickListener {
            goldBug.visibility = ImageView.GONE
            val bonus = ((viewModel.goldRate.value ?: 7000.0) / 1000).toInt() * 15
            viewModel.addScore(bonus)
            Toast.makeText(this, "+$bonus очков (золото)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (viewModel.isTiltMode.value != true || viewModel.isPaused.value == true || !isGameRunning) return

        val tiltX = event?.values?.get(0) ?: 0f
        val tiltY = event?.values?.get(1) ?: 0f

        // Добавляем "гравитацию" — тараканы падают вниз, если наклон вперёд
        val gravityY = 15f  // сила падения вниз

        viewModel.bugs.value?.forEach { bugData ->
            val view = gameLayout.findViewWithTag<ImageView>(bugData.tag) ?: return@forEach

            // Плавное движение с инерцией
            val newX = (view.x - tiltX * 4.5f).coerceIn(0f, gameLayout.width - 150f)
            val newY = (view.y - tiltY * 4.5f + gravityY).coerceIn(0f, gameLayout.height - 150f)

            // ПЛАВНАЯ АНИМАЦИЯ!
            view.animate()
                .x(newX)
                .y(newY)
                .setDuration(300)  // 0.3 секунды — идеально плавно
                .start()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun pauseGame() {
        viewModel.setPaused(true)
        isGameRunning = false

        AlertDialog.Builder(this)
            .setTitle("Игровое меню")
            .setItems(arrayOf("Настройки", "Авторы", "Правила")) { _, which ->
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("selected_tab", when (which) {
                        0 -> 3; 1 -> 2; 2 -> 1; else -> 0
                    })
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Продолжить игру") { _, _ ->
                viewModel.setPaused(false)
                isGameRunning = true
                startGameLoop()
            }
            .setCancelable(false)
            .show()
    }

    private fun endGame() {
        isGameRunning = false
        AlertDialog.Builder(this)
            .setTitle("Игра закончена!")
            .setMessage("Ваши очки: ${viewModel.score.value ?: 0}")
            .setPositiveButton("OK") { _, _ ->
                saveScore()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun saveScore() {
        val userName = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            .getString("current_user", "Anonymous") ?: "Anonymous"
        val difficulty = getSharedPreferences("game_prefs", Context.MODE_PRIVATE).getInt("speed", 5)
        viewModel.saveScore(userName, difficulty)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        viewModel.setMaxBugs(prefs.getInt("max_roaches", 3))
        val difficulty = prefs.getInt("speed", 5)
        val speedFactor = difficulty.toDouble() / 5.0
        viewModel.setBugInterval((prefs.getInt("bonus_interval", 10) * 100L / speedFactor).toLong())
        viewModel.setTimerSeconds(prefs.getInt("round_duration", 5) * 60)
    }

    private fun playBugCry() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.bug_cry)
        mediaPlayer?.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { pauseGame(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        sensorManager.unregisterListener(this)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}