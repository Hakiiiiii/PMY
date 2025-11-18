package com.example.helloworld

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.BugData
import com.example.helloworld.api.CbrApiService
import com.example.helloworld.database.AppDao
import com.example.helloworld.database.Score
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.lifecycle.map
class GameViewModel(
    private val appDao: AppDao,
    private val cbrApi: CbrApiService
) : ViewModel() {

    // === LiveData ===
    private val _isPaused = MutableLiveData(false)
    val isPaused: LiveData<Boolean> = _isPaused

    private val _bugs = MutableLiveData(mutableListOf<BugData>())
    val bugs: LiveData<List<BugData>> = _bugs as LiveData<List<BugData>>

    private val _timerSeconds = MutableLiveData(300) // 5 минут по умолчанию
    val timerSeconds: LiveData<Int> = _timerSeconds

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _goldRate = MutableLiveData(7000.0)
    val goldRate: LiveData<Double> = _goldRate

    private val _maxBugs = MutableLiveData(5)
    val maxBugs: LiveData<Int> = _maxBugs

    private val _bugInterval = MutableLiveData(3000L)
    val bugInterval: LiveData<Long> = _bugInterval

    private val _tiltMode = MutableLiveData(false)
    val isTiltMode: LiveData<Boolean> = _tiltMode

    // === Управление игрой ===
    fun setPaused(paused: Boolean) { _isPaused.value = paused }

    fun addScore(points: Int) {
        _score.value = (_score.value ?: 0) + points
    }

    fun subtractScore(points: Int) {
        _score.value = (_score.value ?: 0) - points.coerceAtMost(_score.value ?: 0)
    }

    fun setTimerSeconds(seconds: Int) {
        _timerSeconds.value = seconds
    }

    fun updateTimer() {
        val current = _timerSeconds.value ?: 0
        if (current > 0) _timerSeconds.value = current - 1
    }

    fun setTiltMode(enabled: Boolean) {
        _tiltMode.value = enabled
    }

    fun setMaxBugs(count: Int) {
        _maxBugs.value = count.coerceAtLeast(1)
    }

    fun setBugInterval(interval: Long) {
        _bugInterval.value = interval.coerceIn(500L, 10000L)
    }

    fun setGoldRate(rate: Double) {
        _goldRate.value = rate
    }

    // === Работа с тараканами ===
    fun addBug(bug: BugData) {
        val list = _bugs.value?.toMutableList() ?: mutableListOf()
        list.add(bug)
        _bugs.value = list
    }

    fun removeBugByTag(tag: String) {
        val list = _bugs.value?.toMutableList() ?: return
        list.removeAll { it.tag == tag }
        _bugs.value = list
    }

    fun updateBugPosition(tag: String, x: Float, y: Float) {
        val list = _bugs.value?.toMutableList() ?: return
        list.forEach { bug ->
            if (bug.tag == tag) {
                bug.x = x
                bug.y = y
            }
        }
        _bugs.value = list
    }

    fun clearBugs() {
        _bugs.value = mutableListOf()
    }

    fun getRandomPosition(width: Float = 1080f, height: Float = 2100f): Pair<Float, Float> {
        val bugSize = 150f
        val x = Random.nextFloat() * (width - bugSize - 50f) + 25f
        val y = Random.nextFloat() * (height - bugSize - 300f) + 150f // учитываем статус-бар и кнопки
        return x to y
    }

    // === Курс золота ===
    fun loadGoldRate() {
        viewModelScope.launch {
            try {
                val response = cbrApi.getGoldRate().execute()
                if (response.isSuccessful) {
                    val xml = response.body() ?: return@launch
                    val start = xml.indexOf("<Valute CharCode=\"XAU\">")
                    if (start != -1) {
                        val block = xml.substring(start)
                        val valueTag = block.substringAfter("<Value>").substringBefore("</Value>")
                        val rate = valueTag.replace(",", ".").trim().toDoubleOrNull() ?: 7000.0
                        setGoldRate(rate)
                    }
                }
            } catch (e: Exception) {
                // Если интернет упал — оставляем 7000
                setGoldRate(7000.0)
            }
        }
    }

    // === Сохранение рекорда ===
    fun saveScore(userName: String, difficulty: Int) {
        viewModelScope.launch {
            val date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())

            val scoreEntity = Score(
                userName = userName,
                score = _score.value ?: 0,
                difficulty = difficulty,
                date = date
            )
            appDao.insertScore(scoreEntity)
        }
    }
}