package com.lanhee.fortunewheel.screen.inputdialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.lanhee.fortunewheel.databinding.DlgInputBinding
import com.lanhee.fortunewheel.utils.Utils

class InputDialog(context: Context) : Dialog(context) {
    var title: String? = null
    var hintText: String? = null
    val binding by lazy { DlgInputBinding.inflate(layoutInflater) }
    var listener: OnInputText? = null

    interface OnInputText {
        fun onChanged(text: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        window?.let {
            it.attributes.width = (Utils.getScreenWidth(context) * 0.8f).toInt()
        }

        title?.let { binding.tvDlgTitle.text = it }

        hintText?.let {binding.etInput.hint = it }

        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun afterTextChanged(text: Editable?) {
                binding.btnApply.isEnabled = !text.isNullOrEmpty()
            }
        })

        binding.btnApply.setOnClickListener {
            listener?.onChanged(binding.etInput.text.toString())
            dismiss()
        }
    }
}