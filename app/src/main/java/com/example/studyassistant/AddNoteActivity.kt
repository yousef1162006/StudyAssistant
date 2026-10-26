package com.example.studyassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import com.example.studyassistant.R

class AddNoteActivity : AppCompatActivity() {

    private val selectedImagePaths = ArrayList<String>()
    private lateinit var tvImagesStatus: TextView

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    saveImageToInternalStorage(uri)?.let { selectedImagePaths.add(it) }
                }
            } else if (data?.data != null) {
                val uri = data.data!!
                saveImageToInternalStorage(uri)?.let { selectedImagePaths.add(it) }
            }
            tvImagesStatus.text = "📷 تم إرفاق ${selectedImagePaths.size} صورة بنجاح"
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "note_img_${System.currentTimeMillis()}_${Math.random()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val etNoteText = findViewById<EditText>(R.id.etNoteText)
        val btnPickImages = findViewById<Button>(R.id.btnPickImage)
        tvImagesStatus = findViewById(R.id.tvImagesStatus)
        val btnAddNote = findViewById<Button>(R.id.btnAddNote)

        btnPickImages.text = "📷 إرفاق صور متعددة من المعرض"

        btnPickImages.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickImagesLauncher.launch(Intent.createChooser(intent, "اختر الصور"))
        }

        btnAddNote.setOnClickListener {
            val text = etNoteText.text.toString().trim()

            if (text.isNotEmpty() || selectedImagePaths.isNotEmpty()) {
                val newItem = NoteItem(text, selectedImagePaths)
                NotesActivity.notesList.add(newItem)
                NotesActivity.saveNotesToPrefs(this)

                Toast.makeText(this, "تم حفظ الملاحظة بنجاح!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "من فضلك اكتب ملاحظة أو أضف صورة على الأقل", Toast.LENGTH_SHORT).show()
            }
        }
    }
}