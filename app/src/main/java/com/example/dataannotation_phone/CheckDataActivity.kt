package com.example.dataannotation_phone

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.TimeUtils.formatDuration
import com.example.dataannotation_phone.databinding.ActivityCheckDataBinding
import java.io.File

class CheckDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding 초기화
        binding = ActivityCheckDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 최초 데이터 세팅
        updateData()
    }

    override fun onResume() {
        super.onResume()
        // 포그라운드 복귀 시마다 최신 데이터 반영
        updateData()
    }

    // 화면의 텍스트뷰(time, count)에 CSV 및 SharedPreferences 값 세팅
    private fun updateData() {
        // CSV 파일에서 각 활동별 누적 지속시간 계산
        val totalMap = calculateActivityDurations()

        // time 컬럼에 MM:SS 포맷으로 세팅
        binding.timeShower.text         = totalMap["Shower"]?.let { formatDuration(it) } ?: "00:00"
        binding.timeToothbrushing.text = totalMap["Brushing Teeth"]?.let { formatDuration(it) } ?: "00:00"
        binding.timeWashingHands.text  = totalMap["Washing Hands"]?.let { formatDuration(it) } ?: "00:00"
        binding.timeVacuumCleaner.text = totalMap["Vacuum Cleaner"]?.let { formatDuration(it) } ?: "00:00"
        binding.timeWiping.text        = totalMap["Wiping"]?.let { formatDuration(it) } ?: "00:00"

        // SharedPreferences에서 subjectId 불러오기
        val subjectId = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("subject_id", "1")!!

        // count 컬럼에 횟수 세팅
        val countPrefs = getSharedPreferences("count_prefs", MODE_PRIVATE)
        binding.countShower.text          = "${countPrefs.getInt("${subjectId}_Shower", 0)}회"
        binding.countToothbrushing.text  = "${countPrefs.getInt("${subjectId}_Brushing Teeth", 0)}회"
        binding.countWashingHands.text   = "${countPrefs.getInt("${subjectId}_Washing Hands", 0)}회"
        binding.countVacuumCleaner.text  = "${countPrefs.getInt("${subjectId}_Vacuum Cleaner", 0)}회"
        binding.countWiping.text         = "${countPrefs.getInt("${subjectId}_Wiping", 0)}회"
    }

    /**
     * CSV 파일을 읽어, "Start"/"End" 이벤트를 기반으로
     * 활동별 총 지속시간(밀리초)을 계산해 Map으로 반환
     */
    private fun calculateActivityDurations(): Map<String, Long> {
        val csvPath = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("csv_path", null)
            ?: return emptyMap()

        val file = File(csvPath)
        if (!file.exists()) return emptyMap()

        val startMap = mutableMapOf<String, Long>()
        val totalMap = mutableMapOf<String, Long>()

        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            val tokens = line.split(",")
            if (tokens.size < 10) return@forEachLine

            val timeMs   = tokens[0].toLongOrNull() ?: return@forEachLine
            val activity = tokens[8].trim()
            val event    = tokens[9].trim()

            when (event) {
                "Start" -> startMap[activity] = timeMs
                "End"   -> {
                    startMap[activity]?.let { start ->
                        val duration = timeMs - start
                        totalMap[activity] = (totalMap[activity] ?: 0L) + duration
                        startMap.remove(activity)
                    }
                }
            }
        }

        return totalMap
    }

    // 밀리초(ms) 단위 지속시간을 "MM:SS" 문자열로 포맷
    private fun formatDuration(durationMs: Long): String {
        val totalSec = durationMs / 1_000
        val mm = totalSec / 60
        val ss = totalSec % 60
        return String.format("%02d:%02d", mm, ss)
    }
}