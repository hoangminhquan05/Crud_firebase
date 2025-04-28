package com.example.lab6

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lab6.ui.theme.Lab6Theme
import com.google.firebase.firestore.FirebaseFirestore

class CourseDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab6Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val courseList = remember { mutableStateListOf<Course>() }
                    val context = LocalContext.current

                    LaunchedEffect(Unit) {
                        fetchCoursesFromFirebase(courseList, context)
                    }

                    FirebaseUI(context, courseList)
                }
            }
        }
    }
}

@Composable
fun FirebaseUI(context: android.content.Context, courseList: SnapshotStateList<Course>) {
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf(Course()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            itemsIndexed(courseList) { index, item ->
                CourseCard(
                    context = context,
                    course = item,
                    onEditClick = { course ->
                        selectedCourse = course
                        showEditDialog = true
                    },
                    onDeleteClick = { course ->
                        deleteCourseFromFirebase(course, context, courseList)
                    }
                )
            }
        }
    }

    if (showEditDialog) {
        EditCourseDialog(
            course = selectedCourse,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedCourse ->
                updateCourseInFirebase(updatedCourse, context, courseList)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun CourseCard(
    context: android.content.Context,
    course: Course,
    onEditClick: (Course) -> Unit,
    onDeleteClick: (Course) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = course.courseName ?: "",
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.Blue,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )

            Text(
                text = course.courseDuration ?: "",
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.Black,
                style = TextStyle(fontSize = 15.sp)
            )

            Text(
                text = course.courseDescription ?: "",
                color = Color.Black,
                style = TextStyle(fontSize = 15.sp)
            )

            if (expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { onEditClick(course) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Blue
                        )
                    }

                    IconButton(
                        onClick = { onDeleteClick(course) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditCourseDialog(
    course: Course,
    onDismiss: () -> Unit,
    onConfirm: (Course) -> Unit
) {
    var courseName by remember { mutableStateOf(course.courseName ?: "") }
    var courseDuration by remember { mutableStateOf(course.courseDuration ?: "") }
    var courseDescription by remember { mutableStateOf(course.courseDescription ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Chỉnh sửa khóa học",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Tên khóa học") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = courseDuration,
                    onValueChange = { courseDuration = it },
                    label = { Text("Thời lượng") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = courseDescription,
                    onValueChange = { courseDescription = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onConfirm(
                            Course(
                                id = course.id,
                                courseName = courseName,
                                courseDuration = courseDuration,
                                courseDescription = courseDescription
                            )
                        )
                    }) {
                        Text("Lưu")
                    }
                }
            }
        }
    }
}

fun fetchCoursesFromFirebase(courseList: SnapshotStateList<Course>, context: android.content.Context) {
    val db = FirebaseFirestore.getInstance()

    db.collection("Courses").get()
        .addOnSuccessListener { querySnapshot ->
            courseList.clear()
            for (document in querySnapshot.documents) {
                val course = document.toObject(Course::class.java)?.copy(id = document.id)
                if (course != null) {
                    courseList.add(course)
                }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Lấy dữ liệu thất bại", Toast.LENGTH_SHORT).show()
        }
}

fun updateCourseInFirebase(
    course: Course,
    context: android.content.Context,
    courseList: SnapshotStateList<Course>
) {
    if (course.id.isEmpty()) return

    val db = FirebaseFirestore.getInstance()

    db.collection("Courses").document(course.id)
        .set(course)
        .addOnSuccessListener {
            val index = courseList.indexOfFirst { it.id == course.id }
            if (index != -1) {
                courseList[index] = course
            }
            Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Cập nhật thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun deleteCourseFromFirebase(
    course: Course,
    context: android.content.Context,
    courseList: SnapshotStateList<Course>
) {
    if (course.id.isEmpty()) return

    val db = FirebaseFirestore.getInstance()

    db.collection("Courses").document(course.id)
        .delete()
        .addOnSuccessListener {
            courseList.remove(course)
            Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Xóa thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun addDataToFirebase(
    courseName: String,
    courseDuration: String,
    courseDescription: String,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    val course = Course(
        courseName = courseName,
        courseDuration = courseDuration,
        courseDescription = courseDescription
    )

    db.collection("Courses")
        .add(course)
        .addOnSuccessListener { documentReference ->
            Toast.makeText(context, "Thêm thành công", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Thêm thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}