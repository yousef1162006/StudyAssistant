package com.example.studyassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.studyassistant.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSchedule = findViewById<Button>(R.id.btnSchedule)
        val btnTasks = findViewById<Button>(R.id.btnTasks)
        val btnNotes = findViewById<Button>(R.id.btnNotes)

        btnSchedule.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        btnTasks.setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }

        btnNotes.setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
        }
    }
}