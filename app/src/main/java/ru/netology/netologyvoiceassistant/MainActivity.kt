package ru.netology.netologyvoiceassistant

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("netology voice", "start of onCreate function")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        Log.d("netology voice", "end of onCreate function")
    }

    private fun initViews() {
        setSupportActionBar(findViewById(R.id.topAppBar))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                // TODO : add wolfram request
                return true
            }
            R.id.action_voice -> {
                // TODO : add voice recognition
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}