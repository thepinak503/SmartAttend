package pinak.smartattend

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pinak.smartattend.data.AppDatabase
import pinak.smartattend.data.Student
import pinak.smartattend.ui.AttendanceViewModel
import pinak.smartattend.ui.theme.SmartAttendTheme
import pinak.smartattend.util.ExcelExporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartAttendTheme {
                val navController = rememberNavController()
                val viewModel: AttendanceViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("dashboard") { DashboardScreen(navController, viewModel) }
                    composable("mark_attendance") { MarkAttendanceScreen(navController, viewModel) }
                    composable("add_student") { AddStudentScreen(navController, viewModel) }
                    composable("view_students") { StudentsListScreen(navController, viewModel) }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer)) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = stringResource(R.string.login_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.login_subtitle),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.label_username)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.label_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Default.Visibility
                        else Icons.Default.VisibilityOff

                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        if (username == "pinak" && password == "pinak@123") {
                            navController.navigate("dashboard") 
                        } else {
                            Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.btn_login), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val students by viewModel.allStudents.collectAsState()
    val scrollState = rememberScrollState()

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val dao = AppDatabase.getDatabase(context).attendanceDao()
                        val data = dao.getStudentsWithAttendance()
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                                ExcelExporter.exportToExcel(context, data, outputStream)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.dashboard_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_student") },
                modifier = Modifier.semantics { contentDescription = context.getString(R.string.content_desc_add_student) }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(stringResource(R.string.welcome_back), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(stringResource(R.string.total_students, students.size))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            DashboardButton(
                text = stringResource(R.string.btn_mark_attendance),
                icon = Icons.Default.CheckCircle,
                onClick = { navController.navigate("mark_attendance") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            DashboardButton(
                text = stringResource(R.string.btn_view_students),
                icon = Icons.Default.People,
                onClick = { navController.navigate("view_students") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DashboardButton(
                text = stringResource(R.string.btn_export_report),
                icon = Icons.Default.Share,
                onClick = {
                    val fileName = "Attendance_Report_${System.currentTimeMillis()}.xlsx"
                    createDocumentLauncher.launch(fileName)
                }
            )

            if (students.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Database is empty. Load real student data below:", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        viewModel.seedFakeData()
                        Toast.makeText(context, "Loading TECA student list...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(stringResource(R.string.btn_seed_data), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(50.dp)) // Extra space at bottom
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp).semantics { role = Role.Button },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(navController: NavHostController, viewModel: AttendanceViewModel) {
    val students by viewModel.allStudents.collectAsState()
    val attendanceStates = remember { mutableStateMapOf<Int, String>() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mark_attendance_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (students.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_students_found))
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(students) { student ->
                        AttendanceItem(
                            student = student,
                            currentState = attendanceStates[student.id] ?: "Present",
                            onStateChange = { newState -> attendanceStates[student.id] = newState }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.submitAttendance(attendanceStates, context)
                            Toast.makeText(context, "Attendance Saved", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_submit_attendance))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsListScreen(navController: NavHostController, viewModel: AttendanceViewModel) {
    val students by viewModel.allStudents.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.students_list_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        }
    ) { padding ->
        if (students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_students_found))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                items(students) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Roll: ${student.rollNo} | Class: ${student.className}", fontSize = 14.sp)
                                Text("Parent: ${student.parentMobile}", fontSize = 14.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { 
                                viewModel.sendWhatsAppMessage(context, student.parentMobile, "Hello, regarding student ${student.name} (${student.rollNo})...")
                            }) {
                                Icon(Icons.Default.Send, contentDescription = stringResource(R.string.content_desc_whatsapp), tint = Color(0xFF25D366))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceItem(student: Student, currentState: String, onStateChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Roll No: ${student.rollNo}", fontSize = 14.sp, color = Color.Gray)
            }
            
            Row {
                AttendanceOption("P", currentState == "Present", { onStateChange("Present") })
                Spacer(modifier = Modifier.width(8.dp))
                AttendanceOption("A", currentState == "Absent", { onStateChange("Absent") })
            }
        }
    }
}

@Composable
fun AttendanceOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) {
        if (text == "P") Color(0xFF4CAF50) else Color(0xFFF44336)
    } else {
        Color.LightGray
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .semantics { 
                role = Role.RadioButton
                stateDescription = if (isSelected) "Selected" else "Not selected"
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(navController: NavHostController, viewModel: AttendanceViewModel) {
    var name by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var parentMobile by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_student_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.label_student_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = rollNo,
                onValueChange = { rollNo = it },
                label = { Text(stringResource(R.string.label_roll_number)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text(stringResource(R.string.label_class_section)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text(stringResource(R.string.label_subject)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = parentMobile,
                onValueChange = { parentMobile = it },
                label = { Text(stringResource(R.string.label_parent_mobile)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && rollNo.isNotBlank()) {
                        scope.launch {
                            viewModel.addStudent(name, rollNo, className, subject, parentMobile)
                            Toast.makeText(context, "Student Added", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_save_student))
            }
        }
    }
}
