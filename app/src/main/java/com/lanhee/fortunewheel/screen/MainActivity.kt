package com.lanhee.fortunewheel.screen

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.lanhee.fortunewheel.R
import com.lanhee.fortunewheel.databinding.ActivityMainBinding
import com.lanhee.fortunewheel.inter.OnRouletteListener
import com.lanhee.fortunewheel.utils.ScreenCaptureHelper
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var isSettingMode = true;
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    companion object {
        //preferences용 상수
        const val _PREF_NAME_DEFAULT = "defaultSetting"
        const val _P_SIZE = "size"
        const val _P_ITEM = "item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setDefaultSetting()

        binding.roulette.listener = object : OnRouletteListener {
            override fun onRouletteClick(position: Int) {
                val defaultText = binding.roulette.getItems()[position]
                if(isSettingMode) {
                    val dialog = ChangeDialog(this@MainActivity)
                    dialog.defaultText = defaultText
                    dialog.listener = object : ChangeDialog.OnChangeInput {
                        override fun onChanged(text: String) {
                            binding.roulette.setItem(text, position)
                        }
                    }
                    dialog.show()
                }else{
                    Toast.makeText(baseContext, defaultText, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onRouletteStop(position: Int) {
                binding.btnRoll.setText(R.string.action_reset)
                binding.btnRoll.isEnabled = true
                isSettingMode = false
                binding.grpResultText.visibility = View.VISIBLE
                binding.tvResult.text = binding.roulette.getItems()[position]
            }
        }

        binding.btnRoll.setOnTouchListener { view, event ->
            event?.let { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN ->{
                        if(isSettingMode) {
                            binding.roulette.startRolling()
                        }}
                    MotionEvent.ACTION_UP ->{
                        if(binding.roulette.isRolling) {
                            binding.btnRoll.isEnabled = false
                            binding.roulette.stopRolling()
                        }else{
                            binding.roulette.reset()
                            isSettingMode = true
                            binding.btnRoll.setText(R.string.action_roll)
                            binding.grpResultText.visibility = View.GONE
                        }
                    }
                }
            }
            true
        }
    }

    private fun setDefaultSetting() {
        var defaultList = loadListToPreferences()
        if(defaultList.isEmpty()) {
            defaultList = arrayOf("Y", "N")
        }
        binding.roulette.setItems(defaultList)
    }

    private fun loadListToPreferences(): Array<String> {
        val sharedPref = getSharedPreferences(_PREF_NAME_DEFAULT, MODE_PRIVATE)
        val result = Array(sharedPref.getInt(_P_SIZE, 0)) { index ->
            sharedPref.getString(_P_ITEM+index, null)?:""
        }
        return result
    }

    private fun saveListToPreferences() {
        val sharedPref = getSharedPreferences(_PREF_NAME_DEFAULT, MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(_P_SIZE, binding.roulette.getItems().size)
        binding.roulette.getItems().forEachIndexed { index, item ->
            editor.putString(_P_ITEM+index, item)
        }
        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_list -> {
                val dialog = ListDialog(this@MainActivity)
                dialog.defaultList = binding.roulette.getItems()
                dialog.listener = object : ListDialog.OnListApply {
                    override fun onListApply(items: Array<String>) {
                        binding.roulette.setItems(items)
                    }
                }
                dialog.show()
            }
            R.id.action_save,
            R.id.action_load -> {
                Toast.makeText(baseContext, item.title, Toast.LENGTH_SHORT).show()
            }
            R.id.action_share -> {
                ScreenCaptureHelper(window.decorView.rootView).capture(object :
                    ScreenCaptureHelper.OnCaptureView {
                    override fun onCapture(bitmap: Bitmap) {
                        share(bitmapToFile(bitmap))
                    }

                    private fun bitmapToFile(bitmap: Bitmap): File {
                        val now = SimpleDateFormat("yyyyMMdd_hhmmss").format(Date(System.currentTimeMillis()))
                        val mPath = cacheDir.absolutePath+"/$now.jpg"

                        val imageFile = File(mPath)
                        val outputStream = FileOutputStream(imageFile)
                        outputStream.use {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            outputStream.flush()
                        }
                        return imageFile
                    }

                    private fun share(file: File) {
                        val intent = Intent(Intent.ACTION_SEND)
                        val fileUri : Uri? = FileProvider.getUriForFile(baseContext, "$packageName.fileprovider", file)
                        fileUri?.let {
                            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                            intent.type = "image/*"
                            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            startActivity(intent)
                        }

                    }
                })
            }
        }
        return true
    }


    override fun onPause() {
        super.onPause()
        saveListToPreferences()
    }

    override fun onRestart() {
        super.onRestart()
        setDefaultSetting()
    }
}