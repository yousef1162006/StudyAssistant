package com.example.studyassistant

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import com.example.studyassistant.R

data class NoteItem(
    val text: String,
    val imageUris: ArrayList<String> // قائمة الصور بدلاً من صورة واحدة
)

class NotesActivity : AppCompatActivity() {

    companion object {
        val notesList = ArrayList<NoteItem>()

        fun saveNotesToPrefs(context: Context) {
            val prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (item in notesList) {
                val obj = JSONObject()
                obj.put("text", item.text)

                // حفظ مصفوفة مسارات الصور داخل JSON
                val imgJsonArray = JSONArray()
                for (path in item.imageUris) {
                    imgJsonArray.put(path)
                }
                obj.put("images", imgJsonArray)

                jsonArray.put(obj)
            }
            prefs.edit().putString("notes_data", jsonArray.toString()).apply()
        }

        fun loadNotesFromPrefs(context: Context) {
            val prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE)
            val jsonString = prefs.getString("notes_data", null)
            notesList.clear()
            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val text = obj.getString("text")

                    val imageUris = ArrayList<String>()
                    val imgJsonArray = obj.optJSONArray("images")
                    if (imgJsonArray != null) {
                        for (j in 0 until imgJsonArray.length()) {
                            imageUris.add(imgJsonArray.getString(j))
                        }
                    } else {
                        // توافق مع الإصدار القديم لو وُجدت صوره مفردة
                        val oldImg = obj.optString("image", "")
                        if (oldImg.isNotEmpty()) imageUris.add(oldImg)
                    }

                    notesList.add(NoteItem(text, imageUris))
                }
            }
        }
    }

    private lateinit var listViewNotes: ListView
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        loadNotesFromPrefs(this)

        listViewNotes = findViewById(R.id.listViewNotes)
        val btnOpenAddNote = findViewById<Button>(R.id.btnOpenAddNote)

        adapter = NoteAdapter(this, notesList)
        listViewNotes.adapter = adapter

        btnOpenAddNote.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        listViewNotes.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, ViewNoteActivity::class.java)
            intent.putExtra("note_index", position)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    inner class NoteAdapter(context: Context, private val items: ArrayList<NoteItem>) :
        ArrayAdapter<NoteItem>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = items[position]

            val cardLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 20, 24, 20)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(Color.parseColor("#E3F2FD"))
            }

            val tvTitle = TextView(context).apply {
                text = "📝 ملاحظة دراسية"
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#0D47A1"))
            }

            val previewText = if (item.text.length > 40) item.text.substring(0, 40) + "..." else item.text
            val imgCountText = if (item.imageUris.isNotEmpty()) " [📎 ${item.imageUris.size} صورة]" else ""

            val tvContent = TextView(context).apply {
                text = previewText + imgCountText
                textSize = 14f
                setTextColor(Color.parseColor("#212121"))
                setPadding(0, 6, 0, 4)
            }

            cardLayout.addView(tvTitle)
            cardLayout.addView(tvContent)

            return cardLayout
        }
    }
}