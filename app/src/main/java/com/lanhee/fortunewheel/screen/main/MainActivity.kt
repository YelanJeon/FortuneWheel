package com.lanhee.fortunewheel.screen.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.lanhee.fortunewheel.R
import com.lanhee.fortunewheel.databinding.ActivityMainBinding
import com.lanhee.fortunewheel.screen.listdialog.ListDialog
import com.lanhee.fortunewheel.screen.loaddialog.LoadDialog
import com.lanhee.fortunewheel.screen.inputdialog.InputDialog
import com.lanhee.fortunewheel.utils.ScreenCaptureHelper
import com.lanhee.fortunewheel.widget.RouletteView
import java.lang.RuntimeException

class MainActivity : AppCompatActivity(), ScreenCaptureHelper.Captureable {
    private val rouletteView by lazy { binding.roulette }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainActivityViewModel by viewModels()

    private val firebaseAnalytics by lazy { Firebase.analytics }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContentView(binding.root)

        FirebaseApp.initializeApp(baseContext)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel.items.observe(this) {
            viewModel.setSettingMode(true)
            rouletteView.setItems(it)
        }

        viewModel.isSettingMode.observe(this) { isSettingMode ->
            if(isSettingMode) {
                rouletteView.reset()
                binding.btnRoll.setText(R.string.action_roll)
                binding.btnRoll.isEnabled = true
                binding.grpResultText.visibility = View.GONE
            }else {
                binding.btnRoll.setText(R.string.action_reset)
                binding.btnRoll.isEnabled = true
                binding.grpResultText.visibility = View.VISIBLE
            }
        }

        viewModel.selectedText.observe(this) {
            binding.tvResult.text = it
        }

        viewModel.intentToShare.observe(this) {
            startActivity(it)
        }

        rouletteView.listener = object : RouletteView.OnRouletteListener {
            override fun onRouletteClick(position: Int) {
                val defaultText = viewModel.items.value!![position]
                if(viewModel.isSettingMode()) {
                    val dialog = InputDialog()
                    dialog.apply {
                        title = resources.getString(R.string.title_changeItem)
                        hintText = defaultText
                        listener = object : InputDialog.OnInputText {
                            override fun onChanged(text: String) {
                                viewModel.setItem(text, position)
                            }
                        }
                    }
                    dialog.show(supportFragmentManager, "roullet Click")
                }else{
                    Toast.makeText(baseContext, defaultText, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onRouletteStop(position: Int) {
                viewModel.setSettingMode(false)
                viewModel.setSelectedText(rouletteView.getItems()[position])
            }
        }

        binding.btnRoll.setOnTouchListener { _, event ->
            event?.let { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if(viewModel.isSettingMode()) {
                            rouletteView.startRolling()
                        }
                    }
                    MotionEvent.ACTION_UP ->{
                        if(rouletteView.isRolling) {
                            binding.btnRoll.isEnabled = false
                            rouletteView.stopRolling()
                        }else{
                            viewModel.setSettingMode(true)
                        }
                    }
                }
            }
            true
        }

        viewModel.setDefaultSettings()

        askNotificationPermission()
        registerFirebaseToken()

    }

    private fun askNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                //FCM SDK can pos notification!
            }else if(shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                //유저가 이전 권한 설정 창에서 거부를 누른 경우
            }else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun registerFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                Toast.makeText(baseContext, "token init successful", Toast.LENGTH_SHORT).show()
            }else{
                //토큰 발급에 실패한 경우
                Toast.makeText(baseContext, "token init fail", Toast.LENGTH_SHORT).show()
                return@addOnCompleteListener
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(!rouletteView.isRolling) {
            when (item.itemId) {
                R.id.action_list -> {
                    val dialog = ListDialog()
                    dialog.defaultList = rouletteView.getItems()
                    dialog.listener = object : ListDialog.OnListApply {
                        override fun onListApply(items: Array<String>) {
                            viewModel.setItems(items)
                        }
                    }
                    dialog.show(supportFragmentManager, "list")
                }

                R.id.action_save -> {
                    val dialog = InputDialog()
                    dialog.apply {
                        title = baseContext.resources.getString(R.string.title_save)
                        hintText = baseContext.resources.getString(R.string.text_save_hint)
                        listener = object : InputDialog.OnInputText {
                            override fun onChanged(text: String) {
                                viewModel.saveItems(text)
                            }
                        }
                    }
                    dialog.show(supportFragmentManager, "save")
                }

                R.id.action_load -> {
                    val dialog = LoadDialog()
                    dialog.apply {
                        listener = object : LoadDialog.OnLoadSelect {
                            override fun onSelect(items: Array<String>) {
                                if (!viewModel.isSettingMode()) {
                                    viewModel.setSettingMode(true)
                                }
                                viewModel.setItems(items)
                            }
                        }
                    }
                    dialog.show(supportFragmentManager, "load")
                }

                R.id.action_share -> {
                    viewModel.captureAndShare(this@MainActivity)
                }
            }
        }
        return true
    }

    override fun getCaptureView(): View {
        return window.decorView.rootView
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveListToPrefences()
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.setDefaultSettings()
    }
}