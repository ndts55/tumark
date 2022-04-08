package me.ndts.tumark

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ndts.tumark.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var tuIdEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { this.onFabClick() }

        tuIdEditText = binding.contentMain.mainLayout.findViewById(R.id.tudIdentifier)
        passwordEditText = binding.contentMain.mainLayout.findViewById(R.id.password)

        val context = this.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            tuIdEditText.setText(context.readTuId())
            passwordEditText.setText(context.readPassword())
        }
        context.enqueueGradeWorker()
    }

    private fun onFabClick() {
        // check tuid and password
        val tuId = tuIdEditText.text.toString()
        val password = passwordEditText.text.toString()
        // if empty don't do anything
        if (tuId.isEmpty() || password.isEmpty()) {
            return
        }
        // otherwise save their values and start checking the grades
        val context = this.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            context.writeTuId(tuId)
            context.writePassword(password)
        }

        CoroutineScope(Dispatchers.IO).launch {
            tumarkRun(context)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}