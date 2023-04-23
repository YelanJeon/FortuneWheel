package com.lanhee.fortunewheel.screen.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lanhee.fortunewheel.R
import com.lanhee.fortunewheel.databinding.ActivityMainBinding
import com.lanhee.fortunewheel.inter.Shareable
import com.lanhee.fortunewheel.screen.listdialog.ListDialog
import com.lanhee.fortunewheel.screen.loaddialog.LoadDialog
import com.lanhee.fortunewheel.screen.inputdialog.InputDialog
import com.lanhee.fortunewheel.utils.ScreenCaptureHelper
import com.lanhee.fortunewheel.widget.RouletteView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), ScreenCaptureHelper.Captureable {
    private val rouletteView by lazy { binding.roulette }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContentView(binding.root)
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
                    MotionEvent.ACTION_DOWN ->{
                        if(viewModel.isSettingMode()) {
                            rouletteView.startRolling()
                        }}
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