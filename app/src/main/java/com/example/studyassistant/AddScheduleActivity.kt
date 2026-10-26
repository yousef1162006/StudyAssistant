package com.example.studyassistant

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import com.example.studyassistant.R

class AddScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_schedule)

        val spinnerDays = findViewById<Spinner>(R.id.spinnerDays)
        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val etSubjectName = findViewById<EditText>(R.id.etSubjectName)
        val etInstructorName = findViewById<EditText>(R.id.etInstructorName)
        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etStartTime = findViewById<EditText>(R.id.etStartTime)
        val etEndTime = findViewById<EditText>(R.id.etEndTime)
        val btnSaveSchedule = findViewById<Button>(R.id.btnSaveSchedule)

        val days = arrayOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
        val types = arrayOf("محاضرة", "سكشن")

        // 🌟 Adapter أيام الأسبوع (خلفية بيضاء ونصوص سوداء وزرقاء واضحة)
        val daysAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, days) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#0D47A1")) // أزرق غامق بارز
                view.textSize = 16f
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setBackgroundColor(Color.WHITE) // خلفية بيضاء نقية لمنع الشاشة السوداء
                view.setTextColor(Color.parseColor("#000000")) // نص أسود فاحم واضح جداً
                view.textSize = 16f
                return view
            }
        }
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDays.adapter = daysAdapter

        // 🌟 Adapter نوع الموعد
        val typesAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#0D47A1"))
                view.textSize = 16f
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setBackgroundColor(Color.WHITE) // خلفية بيضاء نقية
                view.setTextColor(Color.parseColor("#000000")) // نص أسود واضح جداً
                view.textSize = 16f
                return view
            }
        }
        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typesAdapter

        // تحديد وقت البداية والدقائق على 00 دائماً
        etStartTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, _ ->
                etStartTime.setText(String.format("%02d:00", h))
            }, cal.get(Calendar.HOUR_OF_DAY), 0, true).show()
        }

        // تحديد وقت النهاية والدقائق على 00 دائماً
        etEndTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, _ ->
                etEndTime.setText(String.format("%02d:00", h))
            }, cal.get(Calendar.HOUR_OF_DAY), 0, true).show()
        }

        btnSaveSchedule.setOnClickListener {
            val day = spinnerDays.selectedItem.toString()
            val type = spinnerType.selectedItem.toString()
            val subject = etSubjectName.text.toString().trim()
            val instructor = etInstructorName.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val startTime = etStartTime.text.toString().trim()
            val endTime = etEndTime.text.toString().trim()

            if (subject.isNotEmpty() && instructor.isNotEmpty() && location.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                val newItem = ScheduleItem(day, type, subject, instructor, location, startTime, endTime)
                ScheduleActivity.scheduleList.add(newItem)
                ScheduleActivity.saveScheduleToPrefs(this)

                Toast.makeText(this, "تمت الإضافة بنجاح!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "برجاء ملء كافة الحقول وتحديد الأوقات", Toast.LENGTH_SHORT).show()
            }
        }
    }
}