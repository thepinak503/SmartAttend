package pinak.smartattend.data

import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    val allStudents: Flow<List<Student>> = attendanceDao.getAllStudents()

    suspend fun insertStudent(student: Student) {
        attendanceDao.insertStudent(student)
    }

    suspend fun insertStudents(students: List<Student>) {
        attendanceDao.insertStudents(students)
    }

    suspend fun insertAttendance(log: AttendanceLog) {
        attendanceDao.insertAttendance(log)
    }

    suspend fun getStudentsWithAttendance(): List<StudentWithAttendance> {
        return attendanceDao.getStudentsWithAttendance()
    }

    suspend fun deleteAllStudents() {
        attendanceDao.deleteAllStudents()
    }
}
