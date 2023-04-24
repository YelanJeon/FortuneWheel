package com.lanhee.fortunewheel.screen.inputdialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.lanhee.fortunewheel.databinding.DlgInputBinding
import com.lanhee.fortunewheel.utils.Utils

class InputDialog : DialogFragment() {
    var title: String? = null
    var hintText: String? = null
    var listener: OnInputText? = null

    private val binding by lazy { DlgInputBinding.inflate(layoutInflater) }
    private lateinit var viewModel: InputDialogViewModel

    interface OnInputText {
        fun onChanged(text: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, InputDialogViewModel.Factory())[InputDialogViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.inputText.observe(viewLifecycleOwner) {
            binding.btnApply.isEnabled = !it.isNullOrEmpty()
        }

        title?.let { binding.tvDlgTitle.text = it }
        hintText?.let {binding.etInput.hint = it }

        binding.etInput.addTextChangedListener {
            viewModel.setInputText(it.toString())
        }

        binding.btnApply.setOnClickListener {
            listener?.onChanged(viewModel.inputText.value!!)
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val width = (Utils.getScreenWidth(requireContext()) * 0.8f).toInt()
            it.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }
}