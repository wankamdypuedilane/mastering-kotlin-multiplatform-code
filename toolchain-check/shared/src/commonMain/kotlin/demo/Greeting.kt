package demo

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

fun greeting(name: String): String = "Hello, $name"

@Composable
fun Hello() { Text(greeting("KMP")) }
