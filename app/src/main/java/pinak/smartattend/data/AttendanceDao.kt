package pinak.smartattend.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<Student>>

    @Insert
    suspend fun insertAttendance(log: AttendanceLog)

    @Query("SELECT * FROM attendance_logs WHERE date = :date")
    suspend fun getAttendanceForDate(date: String): List<AttendanceLog>

    @Transaction
    @Query("SELECT * FROM students")
    suspend fun getStudentsWithAttendance(): List<StudentWithAttendance>

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}

data class StudentWithAttendance(
    @Embedded val student: Student,
    @Relation(
        parentColumn = "id",
        entityColumn = "studentId"
    )
    val attendanceLogs: List<AttendanceLog>
)
