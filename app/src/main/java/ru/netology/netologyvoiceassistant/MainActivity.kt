package ru.netology.netologyvoiceassistant

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    lateinit var requestInput: TextView

    lateinit var searchesAdapter: SimpleAdapter

    val searches = mutableListOf<HashMap<String, String>>()

    lateinit var waEngine: WAEngine

    lateinit var stopButton: FloatingActionButton

    lateinit var textToSpeech: TextToSpeech

    val TTS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("netology voice", "start of onCreate function")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.topAppBar))
        initViews()
        initWolframEngine()
        initTts()

        Log.d("netology voice", "end of onCreate function")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                val question = requestInput.text.toString()
                askWolfram(question)
                return true
            }
            R.id.action_voice -> {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What do you want to know?")
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
                try {
                    startActivityForResult(intent, TTS_REQUEST_CODE)
                } catch (a: ActivityNotFoundException) {
                    Toast.makeText(applicationContext, "TTS not found", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        requestInput = findViewById<TextView>(R.id.request_input)

        val searchesList = findViewById<ListView>(R.id.searches_list)
        searchesAdapter = SimpleAdapter(
            applicationContext,
            searches,
            R.layout.item_search,
            arrayOf("Request", "Response"),
            intArrayOf(R.id.request, R.id.response)
        )
        searchesList.adapter = searchesAdapter
        searchesList.setOnItemClickListener { _, _, position, _ ->
            val request = searches[position]["Request"]
            val response = searches[position]["Response"]
            textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, request)
        }

        stopButton = findViewById<FloatingActionButton>(R.id.stop_button)
        stopButton.setOnClickListener {
            textToSpeech.stop()
            stopButton.visibility = View.GONE
        }
    }

    fun initWolframEngine() {
        waEngine = WAEngine()
        waEngine.appID = "5XAUXY-AHEX64Q37J"
        waEngine.addFormat("plaintext")
    }

    fun askWolfram(request: String) {
        Toast.makeText(applicationContext, "Let me think...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            val query = waEngine.createQuery().apply { input = request }
            val queryResult = waEngine.performQuery(query)
            val response = if (queryResult.isError) {
                queryResult.errorMessage
            } else if (!queryResult.isSuccess) {
                "Sorry, I don't understand, can you rephrase?"
            } else {
                val str = StringBuilder()
                for (pod in queryResult.pods) {
                    if (!pod.isError) {
                        for (subpod in pod.subpods) {
                            for (element in subpod.contents) {
                                if (element is WAPlainText) {
                                    str.append(element.text)
                                }
                            }
                        }
                    }
                }
                str.toString()
            }
            withContext(Dispatchers.Main) {
                searches.add(0, HashMap<String, String>().apply {
                    put("Request", request)
                    put("Response", response)
                })
                searchesAdapter.notifyDataSetChanged()
                textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, request)
            }
        }
    }

    fun initTts() {
        textToSpeech = TextToSpeech(this) {}
        textToSpeech.language = Locale.US
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                stopButton.post { stopButton.visibility = View.VISIBLE }
            }

            override fun onDone(utteranceId: String?) {
                stopButton.post { stopButton.visibility = View.GONE }
            }

            override fun onError(utteranceId: String?) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TTS_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)?.let { question ->
                requestInput.text = question
                askWolfram(question)
            }
        }
    }

}