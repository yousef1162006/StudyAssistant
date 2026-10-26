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

data class TaskItem(
    val day: String,
    val type: String, // "مهمه" أو "مذاكرة"
    val subject: String,
    val goal: String,
    val time: String // يحمل (يوم التسليم + الساعة) للمهمة، وفارغ للمذاكرة
) : Comparable<TaskItem> {
    override fun compareTo(other: TaskItem): Int {
        return this.time.compareTo(other.time)
    }
}

class TasksActivity : AppCompatActivity() {

    companion object {
        val tasksList = ArrayList<TaskItem>()

        fun saveTasksToPrefs(context: Context) {
            val prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (item in tasksList) {
                val obj = JSONObject()
                obj.put("day", item.day)
                obj.put("type", item.type)
                obj.put("subject", item.subject)
                obj.put("goal", item.goal)
                obj.put("time", item.time)
                jsonArray.put(obj)
            }
            prefs.edit().putString("tasks_data", jsonArray.toString()).apply()
        }

        fun loadTasksFromPrefs(context: Context) {
            val prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE)
            val jsonString = prefs.getString("tasks_data", null)
            tasksList.clear()
            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    tasksList.add(
                        TaskItem(
                            obj.getString("day"),
                            obj.getString("type"),
                            obj.getString("subject"),
                            obj.getString("goal"),
                            obj.getString("time")
                        )
                    )
                }
            }
        }
    }

    private lateinit var listViewTasks: ListView
    private lateinit var tvCurrentDayTitle: TextView
    private val displayedList = ArrayList<TaskItem>()
    private var currentDay = "السبت"
    private lateinit var dayButtons: Array<Button>
    private lateinit var customAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        loadTasksFromPrefs(this)

        listViewTasks = findViewById(R.id.listViewTasks)
        tvCurrentDayTitle = findViewById(R.id.tvCurrentDayTitle)
        val btnOpenAddTask = findViewById<Button>(R.id.btnOpenAddTask)

        val btnSat = findViewById<Button>(R.id.btnSat)
        val btnSun = findViewById<Button>(R.id.btnSun)
        val btnMon = findViewById<Button>(R.id.btnMon)
        val btnTue = findViewById<Button>(R.id.btnTue)
        val btnWed = findViewById<Button>(R.id.btnWed)
        val btnThu = findViewById<Button>(R.id.btnThu)
        val btnFri = findViewById<Button>(R.id.btnFri)

        dayButtons = arrayOf(btnSat, btnSun, btnMon, btnTue, btnWed, btnThu, btnFri)
        val daysNames = arrayOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

        customAdapter = TaskAdapter(this, displayedList)
        listViewTasks.adapter = customAdapter

        for (i in dayButtons.indices) {
            dayButtons[i].setOnClickListener {
                currentDay = daysNames[i]
                updateDaySelection(i, daysNames)
            }
        }

        btnOpenAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
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
        tvCurrentDayTitle.text = "مهام يوم: $currentDay"
        filterAndDisplayList()
    }

    private fun filterAndDisplayList() {
        displayedList.clear()
        val filtered = tasksList.filter { it.day == currentDay }.sorted()
        displayedList.addAll(filtered)
        customAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        tasksList.sort()
        filterAndDisplayList()
    }

    inner class TaskAdapter(context: Context, private val items: ArrayList<TaskItem>) :
        ArrayAdapter<TaskItem>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = items[position]

            val cardLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 20, 24, 20)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val isTask = item.type == "مهمه"

            if (isTask) {
                cardLayout.setBackgroundColor(Color.parseColor("#E3F2FD")) // أزرق فاتح للمهمة
            } else {
                cardLayout.setBackgroundColor(Color.parseColor("#F5F5F5")) // رمادي فخم للمذاكرة
            }

            val tvTitle = TextView(context).apply {
                text = if (isTask) "📌 مهمة: ${item.subject}" else "📖 مذاكرة: ${item.subject}"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (isTask) Color.parseColor("#0D47A1") else Color.parseColor("#212121"))
            }

            val tvGoal = TextView(context).apply {
                text = "الهدف: ${item.goal}"
                textSize = 14f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 6, 0, 4)
            }

            cardLayout.addView(tvTitle)
            cardLayout.addView(tvGoal)

            // عرض معاد التسليم (اليوم والساعة) إذا كانت المهمة تحتوي عليه
            if (isTask && item.time.isNotEmpty()) {
                val tvTime = TextView(context).apply {
                    text = "⏰ موعد التسليم: ${item.time}"
                    textSize = 13f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#1565C0"))
                }
                cardLayout.addView(tvTime)
            }

            cardLayout.setOnClickListener {
                val options = arrayOf("إنجاز وحذف العنصر", "إغلاق")
                AlertDialog.Builder(context)
                    .setTitle("إدارة العنصر")
                    .setItems(options) { _, which ->
                        if (which == 0) {
                            tasksList.remove(item)
                            saveTasksToPrefs(context)
                            filterAndDisplayList()
                            Toast.makeText(context, "عاش! تم إنجاز العنصر وحذفه بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    }.show()
            }

            return cardLayout
        }
    }
}