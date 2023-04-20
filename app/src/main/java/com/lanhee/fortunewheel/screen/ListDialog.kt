package com.lanhee.fortunewheel.screen

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import com.lanhee.fortunewheel.databinding.DlgListBinding
import com.lanhee.fortunewheel.utils.Utils

class ListDialog(context: Context) : Dialog(context) {
    var defaultList: Array<String>? = null
    val binding by lazy { DlgListBinding.inflate(layoutInflater) }
    var listener: OnListApply? = null

    interface OnListApply {
        fun onListApply(items: Array<String>)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        window?.let {
            it.attributes.width = (Utils.getScreenWidth(context) * 0.8f).toInt()
            it.attributes.height = (Utils.getScreenHeight(context) * 0.4f).toInt()
        }

        defaultList?.let { defaultList ->
            defaultList.forEach {item ->
                binding.chgList.addView(createChip(item))
                checkApplyEnabled()
            }
        }

        binding.etListInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun afterTextChanged(text: Editable?) {
                binding.btnListAdd.isEnabled = !text.isNullOrEmpty()
            }
        })

        binding.btnListAdd.setOnClickListener {
            val chip = createChip(binding.etListInput.text.toString())
            binding.chgList.addView(chip)

            binding.etListInput.text = null
            checkApplyEnabled()
        }

        binding.btnApply.setOnClickListener {
            val result = Array(binding.chgList.childCount) { index ->
                (binding.chgList.getChildAt(index) as Chip).text.toString()
            }
            listener?.onListApply(result)
            dismiss()
        }


    }

    private fun createChip(text: String): Chip {
        val chip = Chip(context)
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            (it.parent as ViewGroup).removeView(it)
            checkApplyEnabled()
        }
        chip.text = text
        return chip
    }

    fun checkApplyEnabled() {
        binding.btnApply.isEnabled = binding.chgList.childCount > 1
    }

    override fun onStart() {
        super.onStart()
    }



}