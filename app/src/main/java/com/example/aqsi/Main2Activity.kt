package com.example.aqsi

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.db.RouteSheetEntity
import com.example.aqsi.utils.FTPUtils
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Main2Activity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
//        val actionSettings: Button = findViewById(R.id.action_settings)
//        actionSettings.setOnClickListener { Toast.makeText(this, "Toast todo", Toast.LENGTH_LONG).show() }
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        CoroutineScope(Dispatchers.IO).launch {
            FTPUtils.init(this@Main2Activity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }

//    private fun scanCode(){
//        val integrator = IntentIntegrator(this)
//        integrator.setCaptureActivity(CaptureAct::class.java)
//        integrator.setOrientationLocked(true)
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
//        integrator.setPrompt("Scanning code todo")
//        integrator.initiateScan()
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//        if (result != null && result.contents != null) {
//            val builder = AlertDialog.Builder(this)
//            builder.setMessage(result.contents)
//            builder.setTitle("TITLE todo")
//            builder.setPositiveButton("scan again todo", { dialog, which -> scanCode() })
//            builder.setNegativeButton("finish todo", {dialog, which ->
//                dialog.dismiss()
//            scanField.setText(result.contents)})
//            val dialog = builder.create()
//            dialog.show()
//        } else {
//            Toast.makeText(this, "Toast todo", Toast.LENGTH_LONG).show()
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}