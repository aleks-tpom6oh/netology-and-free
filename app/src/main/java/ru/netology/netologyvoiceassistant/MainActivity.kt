package ru.netology.netologyvoiceassistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myText: String = "Hello, world!"
        val myNumber: Int = 42
        val myFloatingNumber: Double = 3.14

        findViewById<TextView>(R.id.text_output).text = myText + " " + myNumber + " " + myFloatingNumber
    }
}