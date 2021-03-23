package ru.netology.netologyvoiceassistant

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    lateinit var requestInput: TextView

    lateinit var searchesAdapter: SimpleAdapter

    val searches = mutableListOf<HashMap<String, String>>()

    lateinit var waEngine: WAEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("netology voice", "start of onCreate function")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.topAppBar))
        initViews()
        initWolframEngine()

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
                // TODO: Add Voice recognition
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
    }

    fun initWolframEngine() {
        waEngine = WAEngine()
        waEngine.appID = "DEMO"
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
            }
        }
    }

}