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
import androidx.room.Room
import com.lanhee.fortunewheel.R
import com.lanhee.fortunewheel.data.SaveDao
import com.lanhee.fortunewheel.data.SaveData
import com.lanhee.fortunewheel.databinding.ActivityMainBinding
import com.lanhee.fortunewheel.inter.OnRouletteListener
import com.lanhee.fortunewheel.utils.AppDatabase
import com.lanhee.fortunewheel.utils.ScreenCaptureHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var isSettingMode = true;
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    val rouletteView by lazy { binding.roulette }

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

        rouletteView.listener = object : OnRouletteListener {
            override fun onRouletteClick(position: Int) {
                val defaultText = rouletteView.getItems()[position]
                if(isSettingMode) {
                    val dialog = InputDialog(this@MainActivity)
                    dialog.apply {
                        title = resources.getString(R.string.title_changeItem)
                        hintText = defaultText
                        listener = object : InputDialog.OnInputText {
                            override fun onChanged(text: String) {
                                rouletteView.setItem(text, position)
                            }
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
                binding.tvResult.text = rouletteView.getItems()[position]
            }
        }

        binding.btnRoll.setOnTouchListener { view, event ->
            event?.let { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN ->{
                        if(isSettingMode) {
                            rouletteView.startRolling()
                        }}
                    MotionEvent.ACTION_UP ->{
                        if(rouletteView.isRolling) {
                            binding.btnRoll.isEnabled = false
                            rouletteView.stopRolling()
                        }else{
                            rouletteView.reset()
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
        rouletteView.setItems(defaultList)
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
        editor.putInt(_P_SIZE, rouletteView.getItems().size)
        rouletteView.getItems().forEachIndexed { index, item ->
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
                dialog.defaultList = rouletteView.getItems()
                dialog.listener = object : ListDialog.OnListApply {
                    override fun onListApply(items: Array<String>) {
                        rouletteView.setItems(items)
                    }
                }
                dialog.show()
            }
            R.id.action_save -> {
                val dialog = InputDialog(this@MainActivity)
                dialog.apply {
                    title = resources.getString(R.string.title_save)
                    hintText = resources.getString(R.string.text_save_hint)
                    listener = object : InputDialog.OnInputText {
                        override fun onChanged(text: String) {
                            save(text)
                        }
                    }
                }
                dialog.show()
            }
            R.id.action_load -> {
                val dialog = LoadDialog(this@MainActivity)
                dialog.apply {
                    listener = object: LoadDialog.OnLoadSelect {
                        override fun onSelect(items: Array<String>) {
                            if(!isSettingMode) {
                                isSettingMode = true
                                this@MainActivity.binding.btnRoll.setText(R.string.action_roll)
                                this@MainActivity.binding.grpResultText.visibility = View.GONE
                            }
                            rouletteView.setItems(items)
                        }
                    }
                }
                dialog.show()
            }
            R.id.action_share -> {
                captureAndShare()
            }
        }
        return true
    }

    private fun save(title: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val saveData = SaveData(title = title, items = rouletteView.getItems())

            val db = AppDatabase.getInstance(applicationContext)
            db?.let { db ->
                db.saveDao().insert(saveData)
            }
        }
    }

    private fun captureAndShare() {
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


    override fun onPause() {
        super.onPause()
        saveListToPreferences()
    }

    override fun onRestart() {
        super.onRestart()
        setDefaultSetting()
    }
}