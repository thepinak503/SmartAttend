package pinak.smartattend.util

import android.content.Context
import android.widget.Toast
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pinak.smartattend.data.StudentWithAttendance
import java.io.OutputStream

object ExcelExporter {
    fun exportToExcel(context: Context, data: List<StudentWithAttendance>, outputStream: OutputStream) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Attendance Report")

        // Header
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Roll No")
        headerRow.createCell(1).setCellValue("Name")
        headerRow.createCell(2).setCellValue("Class")
        headerRow.createCell(3).setCellValue("Subject")
        headerRow.createCell(4).setCellValue("Attendance History")

        // Data
        data.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item.student.rollNo)
            row.createCell(1).setCellValue(item.student.name)
            row.createCell(2).setCellValue(item.student.className)
            row.createCell(3).setCellValue(item.student.subject)
            
            val logs = item.attendanceLogs.joinToString(", ") { "${it.date}: ${it.status}" }
            row.createCell(4).setCellValue(logs)
        }

        try {
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()
            Toast.makeText(context, "Excel file saved successfully!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
