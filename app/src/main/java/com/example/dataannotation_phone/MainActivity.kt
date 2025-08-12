package com.example.dataannotation_phone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val subjectSettingButton = findViewById<Button>(R.id.subjectSettingButton)
        val activityRecordingButton = findViewById<Button>(R.id.activityRecordingButton)
        val checkDataButton = findViewById<Button>(R.id.checkData)

        subjectSettingButton.setOnClickListener {
            if (checkCameraPermission()) {
                startActivity(Intent(this@MainActivity, SubjectSettingActivity::class.java))
            } else {
                requestCameraPermission()
            }
        }

        activityRecordingButton.setOnClickListener {
            if (checkCameraPermission()) {
                startActivity(Intent(this@MainActivity, ActivityRecordingActivity::class.java))
            } else {
                requestCameraPermission()
            }
        }

        checkDataButton.setOnClickListener {
            if (checkCameraPermission()) {
                startActivity(Intent(this@MainActivity, CheckDataActivity::class.java))
            } else {
                requestCameraPermission()
            }
        }

        // 앱 시작 시 카메라 권한 확인
        if (!checkCameraPermission()) {
            requestCameraPermission()
        }
    }

    // 카메라 권한 확인 함수
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 카메라 권한 요청 함수
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "카메라 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다. 설정에서 허용해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
