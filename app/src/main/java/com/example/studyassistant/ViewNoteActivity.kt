package com.example.studyassistant

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import com.example.studyassistant.R

class ViewNoteActivity : AppCompatActivity() {

    private var noteIndex: Int = -1
    private val currentImagePaths = ArrayList<String>()
    private lateinit var imagesContainerLayout: LinearLayout
    private lateinit var etEditNoteText: EditText

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    saveImageToInternalStorage(uri)?.let { currentImagePaths.add(it) }
                }
            } else if (data?.data != null) {
                val uri = data.data!!
                saveImageToInternalStorage(uri)?.let { currentImagePaths.add(it) }
            }
            displayImages()
            Toast.makeText(this, "تم إضافة الصور بنجاح", Toast.LENGTH_SHORT).show()
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
        setContentView(R.layout.activity_view_note)

        noteIndex = intent.getIntExtra("note_index", -1)
        etEditNoteText = findViewById(R.id.etEditNoteText)
        imagesContainerLayout = findViewById(R.id.imagesContainerLayout)

        val btnAddMoreImages = findViewById<Button>(R.id.btnChangeImage)
        val btnSaveEditNote = findViewById<Button>(R.id.btnSaveEditNote)
        val btnDeleteNote = findViewById<Button>(R.id.btnDeleteNote)

        if (noteIndex != -1 && noteIndex < NotesActivity.notesList.size) {
            val note = NotesActivity.notesList[noteIndex]
            etEditNoteText.setText(note.text)
            currentImagePaths.clear()
            currentImagePaths.addAll(note.imageUris)
            displayImages()
        }

        btnAddMoreImages.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickImagesLauncher.launch(Intent.createChooser(intent, "اختر صور إضافية"))
        }

        btnSaveEditNote.setOnClickListener {
            val updatedText = etEditNoteText.text.toString().trim()
            if (noteIndex != -1) {
                NotesActivity.notesList[noteIndex] = NoteItem(updatedText, currentImagePaths)
                NotesActivity.saveNotesToPrefs(this)
                Toast.makeText(this, "تم تحديث الملاحظة بنجاح!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnDeleteNote.setOnClickListener {
            if (noteIndex != -1) {
                NotesActivity.notesList.removeAt(noteIndex)
                NotesActivity.saveNotesToPrefs(this)
                Toast.makeText(this, "تم حذف الملاحظة", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayImages() {
        imagesContainerLayout.removeAllViews()

        for (path in currentImagePaths) {
            val imageCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                setPadding(12, 12, 12, 12)
                setBackgroundColor(Color.parseColor("#F5F5F5"))
            }

            // عرض الصورة مصغرة داخل الكارت
            val imgView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                try {
                    setImageURI(Uri.fromFile(File(path)))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 🌟 عند الضغط على الصورة، تفتح بالشاشة الكاملة مع زر رجوع مخصص بالأعلى
                setOnClickListener {
                    val fullScreenDialog = Dialog(this@ViewNoteActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

                    val rootLayout = LinearLayout(this@ViewNoteActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(Color.BLACK)
                        setPadding(16, 24, 16, 16)
                    }

                    // زر رجوع للخلف في أعلى شاشة العرض الكاملة
                    val btnBack = Button(this@ViewNoteActivity).apply {
                        text = "⬅️ رجوع للخلف"
                        setBackgroundColor(Color.parseColor("#0D47A1"))
                        setTextColor(Color.WHITE)
                        textSize = 15f
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, 16)
                        }
                        setOnClickListener {
                            fullScreenDialog.dismiss()
                        }
                    }

                    // عرض الصورة بحجم كبير وملء الشاشة
                    val fullImageView = ImageView(this@ViewNoteActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        try {
                            setImageURI(Uri.fromFile(File(path)))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    rootLayout.addView(btnBack)
                    rootLayout.addView(fullImageView)

                    fullScreenDialog.setContentView(rootLayout)
                    fullScreenDialog.show()
                }
            }

            // زر حذف هذه الصورة وحدها
            val btnDeleteThisImage = Button(this).apply {
                text = "🗑️ حذف هذه الصورة"
                setBackgroundColor(Color.parseColor("#D32F2F"))
                setTextColor(Color.WHITE)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 0)
                }
                setOnClickListener {
                    currentImagePaths.remove(path)
                    displayImages()
                    Toast.makeText(context, "تم حذف هذه الصورة", Toast.LENGTH_SHORT).show()
                }
            }

            imageCard.addView(imgView)
            imageCard.addView(btnDeleteThisImage)
            imagesContainerLayout.addView(imageCard)
        }
    }
}