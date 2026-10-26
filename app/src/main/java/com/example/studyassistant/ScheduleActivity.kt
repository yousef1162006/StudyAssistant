package com.example.studyassistant

import android.app.AlertDialog
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import com.example.studyassistant.R
data class ScheduleItem(
    val day: String,
    val type: String,
    val subject: String,
    val instructorName: String,
    val location: String,
    val startTime: String,
    val endTime: String
) : Comparable<ScheduleItem> {
    override fun compareTo(other: ScheduleItem): Int {
        return this.startTime.compareTo(other.startTime)
    }
}

class ScheduleActivity : AppCompatActivity() {

    companion object {
        val scheduleList = ArrayList<ScheduleItem>()

        fun saveScheduleToPrefs(context: Context) {
            val prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (item in scheduleList) {
                val obj = JSONObject()
                obj.put("day", item.day)
                obj.put("type", item.type)
                obj.put("subject", item.subject)
                obj.put("instructor", item.instructorName)
                obj.put("location", item.location)
                obj.put("startTime", item.startTime)
                obj.put("endTime", item.endTime)
                jsonArray.put(obj)
            }
            prefs.edit().putString("schedule_data", jsonArray.toString()).apply()
        }

        fun loadScheduleFromPrefs(context: Context) {
            val prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE)
            val jsonString = prefs.getString("schedule_data", null)
            scheduleList.clear()
            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    scheduleList.add(
                        ScheduleItem(
                            obj.getString("day"),
                            obj.getString("type"),
                            obj.getString("subject"),
                            obj.getString("instructor"),
                            obj.getString("location"),
                            obj.getString("startTime"),
                            obj.getString("endTime")
                        )
                    )
                }
            }
        }
    }

    private lateinit var listViewSchedule: ListView
    private lateinit var tvCurrentDayTitle: TextView
    private val displayedList = ArrayList<ScheduleItem>()
    private var currentDay = "السبت"
    private lateinit var dayButtons: Array<Button>
    private lateinit var customAdapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        loadScheduleFromPrefs(this)

        listViewSchedule = findViewById(R.id.listViewSchedule)
        tvCurrentDayTitle = findViewById(R.id.tvCurrentDayTitle)
        val btnOpenAddSchedule = findViewById<Button>(R.id.btnOpenAddSchedule)

        val btnSat = findViewById<Button>(R.id.btnSat)
        val btnSun = findViewById<Button>(R.id.btnSun)
        val btnMon = findViewById<Button>(R.id.btnMon)
        val btnTue = findViewById<Button>(R.id.btnTue)
        val btnWed = findViewById<Button>(R.id.btnWed)
        val btnThu = findViewById<Button>(R.id.btnThu)
        val btnFri = findViewById<Button>(R.id.btnFri)

        dayButtons = arrayOf(btnSat, btnSun, btnMon, btnTue, btnWed, btnThu, btnFri)
        val daysNames = arrayOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

        customAdapter = ScheduleAdapter(this, displayedList)
        listViewSchedule.adapter = customAdapter

        for (i in dayButtons.indices) {
            dayButtons[i].setOnClickListener {
                currentDay = daysNames[i]
                updateDaySelection(i, daysNames)
            }
        }

        btnOpenAddSchedule.setOnClickListener {
            startActivity(Intent(this, AddScheduleActivity::class.java))
        }

        updateDaySelection(0, daysNames)
    }

    private fun updateDaySelection(selectedIndex: Int, daysNames: Array<String>) {
        for (i in dayButtons.indices) {
            if (i == selectedIndex) {
                dayButtons[i].setBackgroundColor(Color.parseColor("#0D47A1"))
            } else {
                dayButtons[i].setBackgroundColor(Color.parseColor("#212121"))
            }
        }
        tvCurrentDayTitle.text = "مواعيد يوم: $currentDay"
        filterAndDisplayList()
    }

    private fun filterAndDisplayList() {
        displayedList.clear()
        val filtered = scheduleList.filter { it.day == currentDay }.sorted()
        displayedList.addAll(filtered)
        customAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        scheduleList.sort()
        filterAndDisplayList()
    }

    inner class ScheduleAdapter(context: Context, private val items: ArrayList<ScheduleItem>) :
        ArrayAdapter<ScheduleItem>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = items[position]

            val cardLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 20, 24, 20)
                // استخدام LayoutParams العامة للـ ListView بدون أي مشاكل
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val isLecture = item.type.contains("محاضرة")

            if (isLecture) {
                // تصميم مميز للمحاضرة (أزرق فاتح راقي جداً)
                cardLayout.setBackgroundColor(Color.parseColor("#E3F2FD"))
            } else {
                // تصميم مميز للسكشن (رمادي فخم)
                cardLayout.setBackgroundColor(Color.parseColor("#F5F5F5"))
            }

            val tvTitle = TextView(context).apply {
                text = if (isLecture) "🎓 محاضرة: ${item.subject}" else "💻 سكشن: ${item.subject}"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (isLecture) Color.parseColor("#0D47A1") else Color.parseColor("#212121"))
            }

            val tvDetails = TextView(context).apply {
                text = "المرشد: ${item.instructorName}  |  المكان: ${item.location}"
                textSize = 14f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 6, 0, 4)
            }

            val tvTime = TextView(context).apply {
                text = "⏰ الوقت: ${item.startTime} ⟵ ${item.endTime}"
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (isLecture) Color.parseColor("#1565C0") else Color.parseColor("#616161"))
            }

            cardLayout.addView(tvTitle)
            cardLayout.addView(tvDetails)
            cardLayout.addView(tvTime)

            cardLayout.setOnClickListener {
                val options = arrayOf("حذف الموعد", "إغلاق")
                AlertDialog.Builder(context)
                    .setTitle("إدارة الموعد")
                    .setItems(options) { _, which ->
                        if (which == 0) {
                            scheduleList.remove(item)
                            saveScheduleToPrefs(context)
                            filterAndDisplayList()
                            Toast.makeText(context, "تم حذف الموعد بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    }.show()
            }

            return cardLayout
        }
    }
}