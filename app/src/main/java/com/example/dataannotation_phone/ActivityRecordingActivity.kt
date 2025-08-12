package com.example.dataannotation_phone

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.LinearLayout
class ActivityRecordingActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // XML 레이아웃 파일을 로드
        setContentView(R.layout.activity_recording)

        val buttonShower = findViewById<Button>(R.id.button_shower)
        val buttonBrush = findViewById<Button>(R.id.button_brush)
        val buttonWashHands = findViewById<Button>(R.id.button_wash_hands)
        val buttonVacuum = findViewById<Button>(R.id.button_vacuum)
        val buttonWipe = findViewById<Button>(R.id.button_wipe)

        // 각 버튼 클릭 시 PhotoCaptureActivity로 전환
        buttonShower.setOnClickListener {
            startPhotoCaptureActivity("Shower")
        }
        buttonBrush.setOnClickListener {
            startPhotoCaptureActivity("Brushing Teeth")
        }
        buttonWashHands.setOnClickListener {
            startPhotoCaptureActivity("Washing Hands")
        }
        buttonVacuum.setOnClickListener {
            startPhotoCaptureActivity("Vacuum Cleaner")
        }
        buttonWipe.setOnClickListener {
            startPhotoCaptureActivity("Wiping")
        }
    }

    private fun startPhotoCaptureActivity(activityName: String) {
        val intent = Intent(this, PhotoCaptureActivity::class.java)
        intent.putExtra("activity", activityName)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
    }
}
