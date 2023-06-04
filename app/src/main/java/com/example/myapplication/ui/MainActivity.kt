package com.example.myapplication.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.myapplication.ui.language.LanguageDialog
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.extensions.PERMISSION_CAMERA
import com.example.myapplication.extensions.fullScreen
import com.example.myapplication.extensions.isCameraPermissionGranted
import com.example.myapplication.ui.`object`.CustomDialog
import com.example.myapplication.ui.`object`.ObjectClass
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object{
        var List:List<ObjectClass>? = null
    }

    private lateinit var navController: NavController

    private val binding: ActivityMainBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fullScreen()
        getPermissions()
        navController = findNavController(R.id.nav_host_fragment)
        binding.filter.setOnClickListener { CustomDialog().show(supportFragmentManager,"") }
        binding.language.setOnClickListener { LanguageDialog().show(supportFragmentManager,"") }
    }


    private fun getPermissions(isGranted:()->Unit = {}){
        if(!isCameraPermissionGranted()) requestPermissions(arrayOf(PERMISSION_CAMERA), 101)
        else isGranted()
    }

    override fun onRequestPermissionsResult(  requestCode: Int, permissions: Array<out String>, grantResults: IntArray  ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED) getPermissions()
    }
}