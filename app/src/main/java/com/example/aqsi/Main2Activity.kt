package com.example.aqsi

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.aqsi.utils.FTPUtils
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import steelkiwi.com.library.DotsLoaderView
import java.util.*


class Main2Activity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: Main2ActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(this).get(Main2ActivityViewModel::class.java)
        setContentView(R.layout.activity_main2)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val progress: DotsLoaderView = findViewById(R.id.dotsLoaderView)
        val progressLayout: RelativeLayout = findViewById(R.id.progress_layout)
        val loadBtn: Button = findViewById(R.id.loadRouteSheet)
        viewModel.state.observe(this, Observer { state ->
            when (state) {
                is State.Loading -> {
                    progressLayout.visibility = View.VISIBLE
                    progress.show()
                    loadBtn.setBackgroundColor(resources.getColor(R.color.light_gray))
                    CoroutineScope(Dispatchers.IO).launch {
//                        AppDatabase(this@Main2Activity).routeSheetDao().deleteAll()//todo
                        FTPUtils.init(this@Main2Activity)
                        viewModel.state.postValue(State.Ready())
                    }
                }
                is State.Ready -> {
                    progressLayout.visibility = View.GONE
                    progress.hide()
                    loadBtn.setBackgroundColor(resources.getColor(R.color.blue))
                }
            }
        })
        viewModel.state.postValue(State.Loading())
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}