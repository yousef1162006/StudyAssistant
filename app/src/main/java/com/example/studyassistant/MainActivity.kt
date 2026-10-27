package com.example.studyassistant

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class LectureItem(
    val id: String,        // معرف فريد للحذف الدقيق
    val type: String,      // محاضرة أو سكشن
    val instructor: String, // اسم المدرس مع اللقب
    val subject: String,   // اسم المادة
    val location: String,  // المكان
    val day: String,       // اليوم
    val startTime: String, // وقت البداية
    val endTime: String,   // وقت النهاية
    val sortMinutes: Int   // دقائق اليوم للترتيب الزمني
) : Comparable<LectureItem> {
    override fun compareTo(other: LectureItem): Int {
        return this.sortMinutes.compareTo(other.sortMinutes)
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var groupType: RadioGroup
    private lateinit var rbLecture: RadioButton
    private lateinit var rbSection: RadioButton
    private lateinit var etSubject: EditText
    private lateinit var etInstructor: EditText
    private lateinit var etLocation: EditText
    private lateinit var spinnerDay: Spinner
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var btnAdd: Button
    private lateinit var layoutScheduleContainer: LinearLayout
    private lateinit var tvEmptyNotice: TextView
    private lateinit var tvSelectedDayTitle: TextView

    private var startMinutesOfDay = 0
    private var currentSelectedDay = "السبت"
    private val daysList = arrayOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. ربط العناصر
        groupType = findViewById(R.id.groupType)
        rbLecture = findViewById(R.id.rbLecture)
        rbSection = findViewById(R.id.rbSection)
        etSubject = findViewById(R.id.etSubject)
        etInstructor = findViewById(R.id.etInstructor)
        etLocation = findViewById(R.id.etLocation)
        spinnerDay = findViewById(R.id.spinnerDay)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        btnAdd = findViewById(R.id.btnAdd)
        layoutScheduleContainer = findViewById(R.id.layoutScheduleContainer)
        tvEmptyNotice = findViewById(R.id.tvEmptyNotice)
        tvSelectedDayTitle = findViewById(R.id.tvSelectedDayTitle)

        // 2. تغيير التسمية تلقائياً (دكتور / معيد)
        groupType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbLecture) {
                etInstructor.hint = "اسم الدكتور"
            } else {
                etInstructor.hint = "اسم المعيد"
            }
        }

        // 3. TimePicker يبدأ من 9 صباحاً والدقائق 0
        etStartTime.setOnClickListener {
            showTimePicker(isStart = true)
        }
        etEndTime.setOnClickListener {
            showTimePicker(isStart = false)
        }

        // إعداد السبنر
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, daysList)
        spinnerDay.adapter = adapter

        // أزرار شريط الأيام السفلي
        setupDayButtons()

        // عرض الجدول لليوم الافتراضي
        displayScheduleForDay(currentSelectedDay)

        // زر الإضافة
        btnAdd.setOnClickListener {
            addLectureItem()
        }
    }

    private fun showTimePicker(isStart: Boolean) {
        val defaultHour = 9
        val defaultMinute = 0

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour >= 12) "مساءً" else "صباحاً"
                val formattedHour = when {
                    selectedHour == 0 -> 12
                    selectedHour > 12 -> selectedHour - 12
                    else -> selectedHour
                }
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", formattedHour, selectedMinute, amPm)

                if (isStart) {
                    etStartTime.setText(formattedTime)
                    startMinutesOfDay = selectedHour * 60 + selectedMinute
                } else {
                    etEndTime.setText(formattedTime)
                }
            },
            defaultHour,
            defaultMinute,
            false
        )
        timePickerDialog.show()
    }

    private fun setupDayButtons() {
        val daysMap = mapOf(
            R.id.btnSat to "السبت",
            R.id.btnSun to "الأحد",
            R.id.btnMon to "الإثنين",
            R.id.btnTue to "الثلاثاء",
            R.id.btnWed to "الأربعاء",
            R.id.btnThu to "الخميس"
        )

        for ((id, dayName) in daysMap) {
            findViewById<Button>(id).setOnClickListener {
                currentSelectedDay = dayName
                tvSelectedDayTitle.text = "جدول يوم: $dayName"
                displayScheduleForDay(dayName)
            }
        }
    }

    private fun addLectureItem() {
        val subject = etSubject.text.toString().trim()
        val rawInstructor = etInstructor.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val day = spinnerDay.selectedItem.toString()
        val startTime = etStartTime.text.toString().trim()
        val endTime = etEndTime.text.toString().trim()

        if (subject.isEmpty() || rawInstructor.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "يرجى إكمال الحقول وتحديد الوقت", Toast.LENGTH_SHORT).show()
            return
        }

        val isLecture = rbLecture.isChecked
        val typeTitle = if (isLecture) "محاضرة" else "سكشن"
        val instructorTitle = if (isLecture) "د. $rawInstructor" else "م. $rawInstructor"
        val uniqueId = System.currentTimeMillis().toString()

        val newItem = LectureItem(
            id = uniqueId,
            type = typeTitle,
            instructor = instructorTitle,
            subject = subject,
            location = location,
            day = day,
            startTime = startTime,
            endTime = endTime,
            sortMinutes = startMinutesOfDay
        )

        saveItemToStorage(newItem)

        etSubject.text.clear()
        etInstructor.text.clear()
        etLocation.text.clear()
        etStartTime.text.clear()
        etEndTime.text.clear()

        currentSelectedDay = day
        tvSelectedDayTitle.text = "جدول يوم: $day"
        displayScheduleForDay(day)

        Toast.makeText(this, "تمت الإضافة والترتيب بنجاح!", Toast.LENGTH_SHORT).show()
    }

    private fun saveItemToStorage(item: LectureItem) {
        val prefs = getSharedPreferences("SmartSchedulePrefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("items_list", "[]")
        val jsonArray = JSONArray(jsonString)

        val obj = JSONObject().apply {
            put("id", item.id)
            put("type", item.type)
            put("instructor", item.instructor)
            put("subject", item.subject)
            put("location", item.location)
            put("day", item.day)
            put("startTime", item.startTime)
            put("endTime", item.endTime)
            put("sortMinutes", item.sortMinutes)
        }

        jsonArray.put(obj)
        prefs.edit().putString("items_list", jsonArray.toString()).apply()
    }

    private fun deleteItemFromStorage(itemId: String) {
        val prefs = getSharedPreferences("SmartSchedulePrefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("items_list", "[]")
        val jsonArray = JSONArray(jsonString)
        val newArray = JSONArray()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            // استخدام optString للسلامة التامة وعدم حدوث كراش
            val currentId = obj.optString("id", i.toString())
            if (currentId != itemId) {
                newArray.put(obj)
            }
        }

        prefs.edit().putString("items_list", newArray.toString()).apply()
        displayScheduleForDay(currentSelectedDay)
        Toast.makeText(this, "تم حذف المحاضرة بنجاح", Toast.LENGTH_SHORT).show()
    }

    private fun displayScheduleForDay(dayName: String) {
        val prefs = getSharedPreferences("SmartSchedulePrefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("items_list", "[]")
        val jsonArray = JSONArray(jsonString)

        val listForDay = mutableListOf<LectureItem>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val item = LectureItem(
                id = obj.optString("id", i.toString()),
                type = obj.optString("type", "محاضرة"),
                instructor = obj.optString("instructor", ""),
                subject = obj.optString("subject", ""),
                location = obj.optString("location", ""),
                day = obj.optString("day", dayName),
                startTime = obj.optString("startTime", ""),
                endTime = obj.optString("endTime", ""),
                sortMinutes = obj.optInt("sortMinutes", 0)
            )
            if (item.day == dayName) {
                listForDay.add(item)
            }
        }

        listForDay.sort()

        layoutScheduleContainer.removeAllViews()

        if (listForDay.isEmpty()) {
            tvEmptyNotice.text = "لا توجد محاضرات أو سكاشن مسجلة في يوم $dayName."
            layoutScheduleContainer.addView(tvEmptyNotice)
            return
        }

        for ((index, item) in listForDay.withIndex()) {
            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 12)
                layoutParams = params
                setBackgroundColor(Color.parseColor("#1E1E1E"))
            }

            val headerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            val badgeText = TextView(this).apply {
                text = "${index + 1}. " + if (item.type == "محاضرة") "🟦 [ محاضرة رسمية ]" else "🟪 [ سكشن عملي ]"
                setTextColor(if (item.type == "محاضرة") Color.parseColor("#33B5E5") else Color.parseColor("#AA66CC"))
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val btnDelete = Button(this).apply {
                text = "🗑️ حذف"
                textSize = 12f
                setBackgroundColor(Color.parseColor("#CC0000"))
                setTextColor(Color.WHITE)
                val p = LinearLayout.LayoutParams(140, 90)
                layoutParams = p
                setOnClickListener {
                    deleteItemFromStorage(item.id)
                }
            }

            headerLayout.addView(badgeText)
            headerLayout.addView(btnDelete)
            cardLayout.addView(headerLayout)

            val detailsText = TextView(this).apply {
                text = "📚 المادة: ${item.subject}\n" +
                        "👨‍🏫 المسؤول: ${item.instructor}\n" +
                        (if (item.location.isNotEmpty()) "📍 المكان: ${item.location}\n" else "") +
                        "⏰ الوقت: من ${item.startTime} إلى ${item.endTime}"
                setTextColor(Color.parseColor("#DDDDDD"))
                textSize = 13.5f
                setPadding(0, 8, 0, 0)
            }
            cardLayout.addView(detailsText)

            layoutScheduleContainer.addView(cardLayout)
        }
    }
}