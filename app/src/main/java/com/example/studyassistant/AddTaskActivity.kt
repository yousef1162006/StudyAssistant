package com.example.studyassistant

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import com.example.studyassistant.R

class AddTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val spinnerTaskDay = findViewById<Spinner>(R.id.spinnerTaskDay)
        val spinnerTaskType = findViewById<Spinner>(R.id.spinnerTaskType)
        val spinnerDeadlineDay = findViewById<Spinner>(R.id.spinnerDeadlineDay)
        val etSubjectName = findViewById<EditText>(R.id.etSubjectName)
        val etTaskGoal = findViewById<EditText>(R.id.etTaskGoal)
        val etTaskTime = findViewById<EditText>(R.id.etTaskTime)
        val layoutDeadlineContainer = findViewById<LinearLayout>(R.id.layoutDeadlineContainer)
        val btnSaveTask = findViewById<Button>(R.id.btnSaveTask)

        val days = arrayOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
        val types = arrayOf("مهمه", "مذاكرة")

        fun createAdapter(items: Array<String>): ArrayAdapter<String> {
            return object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as TextView
                    view.setTextColor(Color.parseColor("#0D47A1"))
                    view.textSize = 16f
                    return view
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent) as TextView
                    view.setBackgroundColor(Color.WHITE)
                    view.setTextColor(Color.parseColor("#000000"))
                    view.textSize = 16f
                    return view
                }
            }.apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        spinnerTaskDay.adapter = createAdapter(days)
        spinnerDeadlineDay.adapter = createAdapter(days)
        spinnerTaskType.adapter = createAdapter(types)

        spinnerTaskType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) { // مهمة
                    layoutDeadlineContainer.visibility = View.VISIBLE
                } else { // مذاكرة
                    layoutDeadlineContainer.visibility = View.GONE
                    etTaskTime.text.clear()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // اختيار الساعة (تبدأ الدقائق من 00 دائماً)
        etTaskTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, _ ->
                etTaskTime.setText(String.format("%02d:00", h))
            }, cal.get(Calendar.HOUR_OF_DAY), 0, true).show()
        }

        btnSaveTask.setOnClickListener {
            val displayDay = spinnerTaskDay.selectedItem.toString()
            val type = spinnerTaskType.selectedItem.toString()
            val subject = etSubjectName.text.toString().trim()
            val goal = etTaskGoal.text.toString().trim()
            val deadlineDay = spinnerDeadlineDay.selectedItem.toString()
            val time = etTaskTime.text.toString().trim()

            if (subject.isNotEmpty() && goal.isNotEmpty()) {
                val fullDeadline = if (type == "مهمه") {
                    if (time.isEmpty()) {
                        Toast.makeText(this, "برجاء تحديد ساعة تسليم المهمة", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    "$deadlineDay - $time"
                } else {
                    ""
                }

                val newItem = TaskItem(displayDay, type, subject, goal, fullDeadline)
                TasksActivity.tasksList.add(newItem)
                TasksActivity.saveTasksToPrefs(this)

                Toast.makeText(this, "تمت الإضافة بنجاح!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "برجاء ملء اسم المادة والهدف على الأقل", Toast.LENGTH_SHORT).show()
            }
        }
    }
}