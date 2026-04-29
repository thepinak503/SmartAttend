package pinak.smartattend.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pinak.smartattend.data.AppDatabase
import pinak.smartattend.data.AttendanceLog
import pinak.smartattend.data.AttendanceRepository
import pinak.smartattend.data.Student
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AttendanceRepository
    val allStudents: StateFlow<List<Student>>

    init {
        val dao = AppDatabase.getDatabase(application).attendanceDao()
        repository = AttendanceRepository(dao)
        allStudents = repository.allStudents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Auto-seed data on startup if the list is empty
        viewModelScope.launch {
            if (repository.allStudents.first().isEmpty()) {
                seedFakeData()
            }
        }
    }

    fun seedFakeData() {
        viewModelScope.launch {
            try {
                // Delete all and insert new ones to ensure a clean state with real data
                repository.deleteAllStudents()

                val realData = listOf(
                    "TECA102 AGARWAL RISHABH PRAMOD", "TECA103 AHIRE SANIKA KISAN", "TECA104 BABAR PARAS MILIND",
                    "TECA105 BARKUL PRITESH PRASHANT", "TECA106 BEDGE SIDDHI SANDEEP", "TECA107 BHAWAR MAYUR NAVNATH",
                    "TECA108 BHITE SANDESH RAJU", "TECA109 BHOR MAYURESH SANJAY", "TECA111 BORDE PREM NARENDRA",
                    "TECA112 BORKAR PRAJWAL YOGESH", "TECA113 BORSE ADITYA MANOJ", "TECA115 CHAUDHARI VINEET BHARAT KUMAR",
                    "TECA116 DANDE SAMYAK PRASHANT", "TECA117 DAWBHAT SANKET BHAUSAHEB", "TECA118 DERE GAURI RAJESH",
                    "TECA119 DHABU PINAK AMOD", "TECA120 DHANOKAR TANVI DHANANJAY", "TECA121 GAIKWAD NIHARIKA DILIP",
                    "TECA122 GAWADE SIDDHI DINESH", "TECA123 GAWALI RUPALI ANNASAHEB", "TECA124 GIRASE SAURABH DHANSING",
                    "TECA125 HAJARE SHREYASH EKNATH", "TECA126 HOLAM SIDDHI SURESH", "TECA127 JADHAV NACHIKET RANJEET",
                    "TECA128 THOPATE SOHAM APPASAHEB", "TECA131 KANADE SHASHANK SANMUKH", "TECA132 KHADSE SAKSHI ASHOK",
                    "TECA133 JANGLE ADITYA SANDIP", "TECA134 MANE JANHAVI SAYAJI", "TECA135 MANE VIVEK SAMBHAJI",
                    "TECA136 MANE YASH ANIL", "TECA137 MANKAR SHRIJAY MORESHWAR", "TECA138 MOHITE NISHANT RAHUL",
                    "TECA140 MULEY SAMARPIT APPASAHEB", "TECA141 MULIK ATHARV RAJAN", "TECA142 NANDINI SURESH NAGENDRE",
                    "TECA143 NARWADE HARSHAL SUNIL", "TECA144 NEHA SUNIL SHIRSAT", "TECA145 NEHARKAR SAHIL MUKESH",
                    "TECA146 PACHARNE OMKAR RAYCHAND", "TECA147 PARMAR MOKSHADA LAKHESINGH", "TECA148 PATHAN TOHID JAVED",
                    "TECA149 PATIL ATHARV SUNIL", "TECA150 PATIL DEVENDRA MAHESH", "TECA151 PATIL HARSHAL PRAKASH",
                    "TECA152 PATIL SWAMI YOGESH", "TECA153 PATIL VEDIKA ATUL", "TECA154 RAJULWAR VISHVESH RAJESH",
                    "TECA155 RATHOD AKASH SUNIL", "TECA156 RAY LATIKA MANOJ", "TECA157 SAHARE SAHIL HANSRAJ",
                    "TECA158 SHAIKH IBRAHIM FASIUDDIN", "TECA159 SHAIKH SANIYA JALIL", "TECA160 SHINDE SRUSHTI ANNASAHEB",
                    "TECA161 SURWASE BHAGYASHREE KISAN", "TECA162 THAKUR YASH SANJAY", "TECA163 THOSAR PRANALI ARUN",
                    "TECA164 TIWARI VAISHNAVI KAMLESH", "TECA166 VAIBHAV KOUL", "TECA167 TIRTHA ZOLDEO",
                    "TECA168 VAISHNAVI BABASAHEB NARALE", "TECA169 WAGH RUTUJA SANDEEP", "TECA170 YASH KISHOR CHAUDHARY",
                    "TECA171 ZOPE MAYUR UMAKANT", "TECA172 JADHAV HARSH DEEPAK", "TECA173 OBBEN KACHROO"
                )

                val studentsToInsert = realData.map { entry ->
                    val rollNo = entry.substringBefore(" ")
                    val name = entry.substringAfter(" ")
                    Student(
                        name = name,
                        rollNo = rollNo,
                        className = "TECA",
                        subject = "All",
                        parentMobile = "910000000000" // Dummy placeholder for privacy
                    )
                }
                repository.insertStudents(studentsToInsert)
            } catch (e: Exception) {
                // Fail gracefully
            }
        }
    }

    fun addStudent(name: String, rollNo: String, className: String, subject: String, parentMobile: String) {
        viewModelScope.launch {
            repository.insertStudent(Student(name = name, rollNo = rollNo, className = className, subject = subject, parentMobile = parentMobile))
        }
    }

    fun submitAttendance(attendanceMap: Map<Int, String>, context: Context) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            attendanceMap.forEach { (studentId, status) ->
                repository.insertAttendance(AttendanceLog(studentId = studentId, date = date, status = status))
                
                if (status == "Absent") {
                    val student = allStudents.value.find { it.id == studentId }
                    student?.let {
                        sendWhatsAppMessage(context, it.parentMobile, "Alert: Student ${it.name} (${it.rollNo}) is Absent today in TECA.")
                    }
                }
            }
        }
    }

    fun sendWhatsAppMessage(context: Context, mobile: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=$mobile&text=${Uri.encode(message)}"
            intent.data = url.toUri()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }
}
