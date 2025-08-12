package com.example.dataannotation_phone

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.util.Calendar

class SubjectSettingActivity : AppCompatActivity() {

    private lateinit var subjectIdEditText: EditText
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var csvFile: File
    private lateinit var csvWriter: BufferedWriter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_setting)

        subjectIdEditText = findViewById(R.id.subjectIdEditText)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // 이전에 저장된 피험자 번호를 불러와 EditText에 채워넣기
        val savedSubjectId = sharedPrefs.getString("subject_id", "")
        subjectIdEditText.setText(savedSubjectId)

        // CSV 파일이 이미 생성되었는지 확인하는 boolean값
        val csvCreated = sharedPrefs.getBoolean("csv_created", false)
        startButton.isEnabled = !csvCreated

        startButton.setOnClickListener {
            val subjectId = subjectIdEditText.text.toString().ifEmpty { "1" }
            sharedPrefs.edit().putString("subject_id", subjectId).apply()
            createCsvFile(subjectId)

            val countPrefs = getSharedPreferences("count_prefs", MODE_PRIVATE).edit()
            // 각 피험자별 key: "${subjectId}_ActivityName"
            countPrefs.putInt("${subjectId}_Shower", 2)
            countPrefs.putInt("${subjectId}_Brushing Teeth", 3)
            countPrefs.putInt("${subjectId}_Washing Hands", 10)
            countPrefs.putInt("${subjectId}_Vacuum Cleaner", 3)
            countPrefs.putInt("${subjectId}_Wiping", 10)
            countPrefs.apply()

            // CSV 파일 생성 후 상태를 저장하여 시작 버튼 비활성화 유지
            sharedPrefs.edit().putBoolean("csv_created", true).apply()
            startButton.isEnabled = false
        }

        resetButton.setOnClickListener {
            // 초기화 버튼을 누르면 CSV 파일 생성 상태를 초기화하고, 시작 버튼 활성화
            sharedPrefs.edit().putBoolean("csv_created", false).apply()
            startButton.isEnabled = true
        }
    }

    private fun createCsvFile(subjectId: String) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        // CSV 파일 이름 생성
        val fileName = "Subject${subjectId}_${year}_${month}_${day}_${hour}_${minute}_${second}_annotation.csv"
        val baseDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HCILab/$subjectId")
        if (!baseDir.exists()) baseDir.mkdirs()
        csvFile = File(baseDir, fileName)

        // CSV 헤더 작성
        csvWriter = BufferedWriter(FileWriter(csvFile))
        csvWriter.write("UnixTime,Time,Year,Month,Day,Hour,Min,Sec,Activity,Event,Confirm")
        csvWriter.newLine()

        // 시작 시간 기록
        val startTime = System.currentTimeMillis()
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().apply {
            putLong("start_time", startTime)
            // CSV 파일 경로 저장 (다른 액티비티에서 동일한 CSV 파일을 사용하기 위함)
            putString("csv_path", csvFile.absolutePath)
            apply()
        }

        val logLine = "$startTime,${(startTime - startTime) / 1000.0}," +
                "$year,$month,$day,$hour,$minute,$second,,Session Start,"
        csvWriter.write(logLine)
        csvWriter.newLine()
        csvWriter.flush()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::csvWriter.isInitialized) {
            csvWriter.close()
        }
    }
}
