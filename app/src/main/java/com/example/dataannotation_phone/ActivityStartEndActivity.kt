package com.example.dataannotation_phone

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.util.Calendar

class ActivityStartEndActivity : AppCompatActivity() {

    private lateinit var selectedActivity: String
    private lateinit var photoPath: String
    private lateinit var csvFile: File
    private lateinit var csvWriter: BufferedWriter

    // Timer 관련 변수
    private lateinit var timerText: TextView
    private lateinit var activityNameText: TextView

    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var activityStartTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_start_end)

        // Timer TextView Activity name TextView 초기화
        timerText = findViewById(R.id.timerText)
        activityNameText = findViewById(R.id.activityNameText)

        // 인텐트에서 값 불러오기
        selectedActivity = intent.getStringExtra("activity") ?: "Unknown"
        photoPath = intent.getStringExtra("photo_path") ?: ""

        // 선택한 activity 이름 화면에 표시
        activityNameText.text = selectedActivity

        initializeCsvFile()

        // XML에 정의된 버튼 참조
        val startButton = findViewById<Button>(R.id.startButton)
        val endButton = findViewById<Button>(R.id.endButton)
        endButton.isEnabled = false

        startButton.setOnClickListener {
            logActivityStart()

            // 스타트 버튼 비활성화: 종료 버튼을 누르기 전까지 유지
            startButton.isEnabled = false
            endButton.isEnabled = true

            // 타이머 시작: 현재 시각 저장 후 주기적으로 업데이트
            activityStartTime = System.currentTimeMillis()
            startTimer()
        }

        endButton.setOnClickListener {
            logActivityEnd()
            stopTimer()
            showSurveyDialog()

            startButton.isEnabled = true
            endButton.isEnabled = false
        }
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - activityStartTime
                timerText.text = formatElapsedTime(elapsed)

                // 1초마다 업데이트
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerRunnable?.let { timerHandler.post(it) }
    }

    private fun stopTimer() {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    private fun formatElapsedTime(elapsedMillis: Long): String {
        val seconds = (elapsedMillis / 1000) % 60
        val minutes = (elapsedMillis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun initializeCsvFile() {
        // SubjectSettingActivity에서 저장한 CSV 파일 경로를 SharedPreferences에서 불러옴
        val csvPath = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("csv_path", null) ?: throw IllegalStateException("CSV file not found")
        csvFile = File(csvPath)

        // append 모드로 파일을 열어 기록 추가
        csvWriter = BufferedWriter(FileWriter(csvFile, true))
    }

    private fun logActivityStart() {
        val cal = Calendar.getInstance()
        val time = System.currentTimeMillis()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val startTimePref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getLong("start_time", time)

        val logLine = "$time,${(time - startTimePref) / 1000.0}," +
                "$year,$month,$day,$hour,$minute,$second,$selectedActivity,Start,"
        csvWriter.write(logLine)
        csvWriter.newLine()
        csvWriter.flush()
    }

    private fun logActivityEnd() {
        val cal = Calendar.getInstance()
        val time = System.currentTimeMillis()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val startTimePref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getLong("start_time", time)

        val logLine = "$time,${(time - startTimePref) / 1000.0}," +
                "$year,$month,$day,$hour,$minute,$second,$selectedActivity,End,"
        csvWriter.write(logLine)
        csvWriter.newLine()
        csvWriter.flush()
    }

    private fun showSurveyDialog() {
        val dialog = SurveyDialogFragment.newInstance(selectedActivity) { confirm ->
            // CSV 파일의 마지막 줄에 Confirm 값 추가
            val lines = csvFile.readLines().toMutableList()
            if (lines.isNotEmpty()) {
                val lastLine = lines.last().split(",").toMutableList()
                while (lastLine.size < 11) lastLine.add("") // 열 맞추기
                lastLine[10] = if (confirm) "yes" else "no"
                lines[lines.size - 1] = lastLine.joinToString(",")
                csvFile.writeText(lines.joinToString("\n") + "\n")
            }
            val prefs = getSharedPreferences("count_prefs", MODE_PRIVATE)
            val subjectId = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("subject_id", "1")!!

            if (confirm) {
                val old = prefs.getInt("${subjectId}_$selectedActivity", 0)
                prefs.edit()
                    .putInt("${subjectId}_$selectedActivity", (old - 1))
                    .apply()
            }
            // 활동 기록 화면으로 돌아가기
            val intent = Intent(this, ActivityRecordingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            finish()
        }
        dialog.show(supportFragmentManager, "SurveyDialog")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::csvWriter.isInitialized) {
            csvWriter.close()
        }
        stopTimer()
    }
}
