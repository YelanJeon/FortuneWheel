package com.lanhee.fortunewheel.screen

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import com.lanhee.fortunewheel.databinding.DlgChangeItemBinding
import com.lanhee.fortunewheel.utils.Utils

class ChangeDialog(context: Context) : Dialog(context) {
    var defaultText: String? = null
    val binding by lazy { DlgChangeItemBinding.inflate(layoutInflater) }
    var listener: OnChangeInput? = null

    interface OnChangeInput {
        fun onChanged(text: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        window?.let {
            it.attributes.width = (Utils.getScreenWidth(context) * 0.8f).toInt()
        }

        defaultText?.let {
            binding.etChangeInput.hint = defaultText
        }

        binding.etChangeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun afterTextChanged(text: Editable?) {
                binding.btnApply.isEnabled = !text.isNullOrEmpty()
            }
        })

        binding.btnApply.setOnClickListener {
            listener?.onChanged(binding.etChangeInput.text.toString())
            dismiss()
        }
    }
}