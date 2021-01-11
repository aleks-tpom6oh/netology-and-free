package ru.netology.netologyvoiceassistant

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var textToSpeech: TextToSpeech
    var speechRequest = 0

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

        val speakButton = findViewById<Button>(R.id.speak_button)
        speakButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What do you want to know?")
            try {
                startActivityForResult(intent, 1)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(
                    applicationContext,
                    "Sorry your device not supported",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val answerOutput = findViewById<TextView>(R.id.answer_output)

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {  })
        textToSpeech.language = Locale.US

        findViewById<FloatingActionButton>(R.id.read_answer).setOnClickListener {
            val answer = answerOutput.text.toString()
            textToSpeech.speak(answer, TextToSpeech.QUEUE_ADD, null, speechRequest.toString())
            speechRequest += 1
        }

        Log.d("netology voice", "end of onCreate function")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                val result: ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val question: String? = result?.get(0)

                if (question != null) {
                    findViewById<TextView>(R.id.question_input).text = question
                    askWolfram(question)
                }
            }
        }
    }

    fun askWolfram(question: String) {
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

                                        val answer = findViewById<TextView>(R.id.answer_output).text.toString()
                                        textToSpeech.speak(answer, TextToSpeech.QUEUE_ADD, null, speechRequest.toString())
                                        speechRequest += 1
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