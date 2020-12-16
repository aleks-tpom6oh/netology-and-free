package ru.netology.netologyvoiceassistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("netology voice", "start of onCreate function")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.topAppBar))

        val questionInput = findViewById<TextView>(R.id.question_input)
        val searchButton = findViewById<Button>(R.id.search_button)

        searchButton.setOnClickListener {
            askWolfram(questionInput.text.toString())
        }

        Log.d("netology voice", "end of onCreate function")
    }

    private fun askWolfram(question: String) {
        val wolframAppId = "DEMO"

        val engine = WAEngine()
        engine.appID = wolframAppId
        engine.addFormat("plaintext")

        val query = engine.createQuery()
        query.input = question

        val answerText = findViewById<TextView>(R.id.answer_output)
        answerText.text = "Let me think..."

        Thread(Runnable {
            val queryResult = engine.performQuery(query)

            answerText.post {
                if (queryResult.isError) {
                    Log.e("wolfram error", queryResult.errorMessage)
                    answerText.text = queryResult.errorMessage
                } else if (!queryResult.isSuccess) {
                    Log.e("wolfram error", "Sorry, I don't understand, can you rephrase?")
                    answerText.text = "Sorry, I don't understand, can you rephrase?"
                } else {
                    for (pod in queryResult.pods) {
                        if (!pod.isError) {
                            for (subpod in pod.subpods) {
                                for (element in subpod.contents) {
                                    if (element is WAPlainText) {
                                        Log.d("wolfram", element.text)
                                        answerText.text = element.text
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }).start()
    }
}