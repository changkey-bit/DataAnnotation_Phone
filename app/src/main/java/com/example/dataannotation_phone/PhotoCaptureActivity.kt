package com.example.dataannotation_phone

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PhotoCaptureActivity : AppCompatActivity() {

    private lateinit var photoFile: File
    private lateinit var selectedActivity: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedActivity = intent.getStringExtra("activity") ?: "Unknown"
        photoFile = createImageFile()

        // 카메라 인텐트 실행
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                this@PhotoCaptureActivity,
                "com.example.dataannotation_phone.fileprovider",
                photoFile
            ))
        }
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // 사진 촬영이 완료되면 바로 다음 액티비티로 전환
            val intent = Intent(this, ActivityStartEndActivity::class.java).apply {
                putExtra("activity", selectedActivity)
                putExtra("photo_path", photoFile.absolutePath)
            }
            startActivity(intent)
            finish()
        } else {
            // 사진 촬영이 실패하거나 취소되면 현재 액티비티 종료
            finish()
        }
    }

    private fun createImageFile(): File {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        val subjectId = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("subject_id", "1") ?: "1"
        val storageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HCILab/$subjectId")
        if (!storageDir.exists()) storageDir.mkdirs()

        val fileName = "Subject${subjectId}_${year}_${month}_${day}_${hour}_${minute}_${second}_${selectedActivity}.jpg"
        return File(storageDir, fileName)
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}
